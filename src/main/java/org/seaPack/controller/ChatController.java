package org.seaPack.controller;

import lombok.RequiredArgsConstructor;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ChatRequest;
import org.seaPack.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor // Lombok 自动生成 final 字段的构造函数 (用于注入)
public class ChatController {

    private final AIProperties aiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RagService ragService;

    @PostMapping("/aiModel")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest request) {

        // 1. 获取配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 2. 构建 URL
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        // --- RAG 核心增强逻辑开始 ---
        // 获取前端传来的命名空间 (需要在 ChatRequest DTO 中添加 namespace 字段)
        List<ChatRequest.MessageDTO> messagesToSent = new ArrayList<>();;
        //List<Map<String, String>> messages = request.getMessages();
        // 如果前端传了 namespace，执行检索
        if (request.getNamespace() != null && !request.getNamespace().trim().isEmpty()) {

            // 获取最后一条用户消息作为问题（因为 RAG 服务目前是单轮问答逻辑）
            // 如果你的 RagService.chat 支持历史消息，这里需要调整
            String lastUserQuestion = "";
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                // 简单取最后一条，或者你可以把所有 history 拼起来
                lastUserQuestion = request.getMessages().get(request.getMessages().size() - 1).getContent();
            } else {
                lastUserQuestion = "你好"; // 防御性编程
            }

            // 调用 RagService 进行检索 (注意：这里同步调用会阻塞流，但为了简单实现先这样)
            // 建议：RagService.chat 方法最好拆分为 retrieve 和 generate，或者只复用其中的检索逻辑
            // 这里为了演示，我们假设直接获取 Context
            String context = ragService.getRelevantContext(request.getNamespace(), lastUserQuestion);

            if (context != null && !context.isEmpty()) {
                // 构造 System Prompt
                String systemPrompt = "你是一个智能助手。请根据以下检索到的上下文信息回答问题：\n\n" +
                        "------ 上下文开始 ------\n" +
                        context + "\n" +
                        "------ 上下文结束 ------\n\n" +
                        "如果上下文中没有答案，请根据你的通用知识回答。";

                // 将 System Prompt 放在第一位
                ChatRequest.MessageDTO systemMsg = new ChatRequest.MessageDTO();
                systemMsg.setRole("system");
                systemMsg.setContent(systemPrompt);
                messagesToSent.add(systemMsg);
            }
        }

        // 将前端传来的历史消息追加到后面
        if (request.getMessages() != null) {
            messagesToSent.addAll(request.getMessages());
        }

        // 3. 构建请求体 (利用 Map.of 简化代码，JDK 9+)
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getChatModel());
        body.put("messages", request.getMessages()); // 直接使用前端传来的 List<MessageDTO>
        body.put("stream", true); // 开启流式，这样前端才能一个字一个字蹦出来

        // 4. 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        // 5. 构建请求实体
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 6. 流式转发核心逻辑
        StreamingResponseBody stream = out -> {
            restTemplate.execute(url, HttpMethod.POST, clientHttpRequest -> {
                // --- 1. 复制请求头 ---
                // 直接从 entity 中获取，不再手动 set
                clientHttpRequest.getHeaders().putAll(entity.getHeaders());

                // --- 2. 写入请求体 ---
                // 修复报错的关键：遍历转换器，找到支持 Map 类型的那个 (通常是 Jackson)
                for (var converter : restTemplate.getMessageConverters()) {
                    if (converter.canWrite(Map.class, MediaType.APPLICATION_JSON)) {
                        // 这里的泛型问题通过传入 Map.class 解决
                        ((HttpMessageConverter<Object>) converter).write(entity.getBody(), MediaType.APPLICATION_JSON, clientHttpRequest);
                        return; // 写完即走
                    }
                }
                throw new IllegalStateException("No HttpMessageConverter found for Map");

            }, clientHttpResponse -> {
                // --- 3. 处理响应流 (保持不变) ---
                if (clientHttpResponse.getBody() != null) {
                    InputStream inputStream = clientHttpResponse.getBody();
                    byte[] buffer = new byte[4096]; // 稍微调大一点缓冲区
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        out.flush();
                    }
                }
                return null;
            });
        };

        // 7. 返回响应 (注意设置 Content-Type 为 text/event-stream)
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_EVENT_STREAM); // SSE 标准类型
        responseHeaders.setCacheControl("no-cache");

        return new ResponseEntity<>(stream, responseHeaders, HttpStatus.OK);
    }
}
