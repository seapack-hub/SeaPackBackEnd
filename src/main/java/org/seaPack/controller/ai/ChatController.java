package org.seaPack.controller.ai;

import dev.langchain4j.service.TokenStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.ChatRequest;
import org.seaPack.service.ai.RagService;
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

/**
 * AI 对话控制器
 * <p>提供基于 SSE(Server-Sent Events) 的流式对话接口，
 * 支持 RAG 上下文检索增强，可对接多种 AI 模型提供商。</p>
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "${cors.allowed-origins:*}")
@RequiredArgsConstructor
public class ChatController {

    private final AIProperties aiProperties;
    private final RestTemplate restTemplate;

    @Autowired
    private RagService ragService;

    /**
     * AI 模型对话（流式返回 SSE 事件流）
     * <p>支持多轮对话，若请求中指定了 namespace 则自动检索知识库上下文注入 system prompt。
     * 流式响应以 text/event-stream 格式逐 token 返回。</p>
     * @param request 对话请求体（含消息列表、可选命名空间等）
     * @return SSE 流式响应
     */
    @PostMapping("/aiModel")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest request) {

        String providerName = aiProperties.getActiveProvider(); // 获取当前激活的 AI 提供商名称
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName); // 获取对应提供商的配置

        if (config == null) { // 配置不存在时抛出异常
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions"; // 拼接 OpenAI 兼容的聊天补全 API 地址

        List<ChatRequest.MessageDTO> messagesToSent = new ArrayList<>(); // 准备最终发送的消息列表
        if (request.getNamespace() != null && !request.getNamespace().trim().isEmpty()) { // 请求指定了知识库命名空间

            String lastUserQuestion = ""; // 提取用户最后一条消息作为检索问句
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                lastUserQuestion = request.getMessages().get(request.getMessages().size() - 1).getContent();
            } else {
                lastUserQuestion = "你好"; // 无消息时使用默认问句
            }

            String context = ragService.getRelevantContext(request.getNamespace(), lastUserQuestion); // 从 RAG 知识库检索相关上下文

            if (context != null && !context.isEmpty()) { // 检索到上下文则构造 system prompt
                String systemPrompt = "你是一个智能助手。请根据以下检索到的上下文信息回答问题：\n\n" + // 组装 system prompt
                        "------ 上下文开始 ------\n" +
                        context + "\n" +
                        "------ 上下文结束 ------\n\n" +
                        "如果上下文中没有答案，请根据你的通用知识回答。";

                ChatRequest.MessageDTO systemMsg = new ChatRequest.MessageDTO(); // 创建 system 角色消息
                systemMsg.setRole("system"); // 设置角色为 system
                systemMsg.setContent(systemPrompt); // 设置 system prompt 内容
                messagesToSent.add(systemMsg); // 加入消息列表
            }
        }

        if (request.getMessages() != null) { // 将用户原始对话消息追加到消息列表
            messagesToSent.addAll(request.getMessages());
        }

        Map<String, Object> body = new HashMap<>(); // 构造发送给 AI API 的请求体
        body.put("model", config.getChatModel()); // 设置模型名称
        body.put("messages", messagesToSent); // 设置消息列表
        body.put("stream", true); // 开启流式响应

        HttpHeaders headers = new HttpHeaders(); // 构造 HTTP 请求头
        headers.setContentType(MediaType.APPLICATION_JSON); // 设置 Content-Type 为 application/json
        headers.set("Authorization", "Bearer " + config.getApiKey()); // 设置 API 鉴权 Header

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers); // 组装请求实体

        StreamingResponseBody stream = out -> { // 创建流式响应体，将 AI API 的响应逐字节转发给客户端
            restTemplate.execute(url, HttpMethod.POST, clientHttpRequest -> { // 执行 HTTP POST 请求到 AI API
                clientHttpRequest.getHeaders().putAll(entity.getHeaders()); // 设置请求头

                for (var converter : restTemplate.getMessageConverters()) { // 遍历 RestTemplate 的消息转换器
                    if (converter.canWrite(Map.class, MediaType.APPLICATION_JSON)) { // 找到能写 Map 为 JSON 的转换器
                        ((HttpMessageConverter<Object>) converter).write(entity.getBody(), MediaType.APPLICATION_JSON, clientHttpRequest); // 将请求体写入 HTTP 请求
                        return; // 写入成功后退出
                    }
                }
                throw new IllegalStateException("No HttpMessageConverter found for Map"); // 找不到合适的转换器则抛异常

            }, clientHttpResponse -> { // 处理 AI API 的响应
                if (clientHttpResponse.getBody() != null) { // 响应体不为空时
                    InputStream inputStream = clientHttpResponse.getBody(); // 获取输入流
                    byte[] buffer = new byte[4096]; // 创建 4KB 缓冲区
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) { // 循环读取 AI API 的流式响应
                        out.write(buffer, 0, bytesRead); // 将数据写入客户端输出流
                        out.flush(); // 立即刷新，实现流式推送
                    }
                }
                return null;
            });
        };

        HttpHeaders responseHeaders = new HttpHeaders(); // 构造 SSE 响应头
        responseHeaders.setContentType(MediaType.TEXT_EVENT_STREAM); // 设置响应类型为 SSE
        responseHeaders.setCacheControl("no-cache"); // 禁止缓存

        return new ResponseEntity<>(stream, responseHeaders, HttpStatus.OK); // 返回流式响应
    }
}