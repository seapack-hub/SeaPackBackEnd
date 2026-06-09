package org.seaPack.controller.ai;

import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final AIProperties aiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RagService ragService;

    @PostMapping("/aiModel")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest request) {

        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        List<ChatRequest.MessageDTO> messagesToSent = new ArrayList<>();
        if (request.getNamespace() != null && !request.getNamespace().trim().isEmpty()) {

            String lastUserQuestion = "";
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                lastUserQuestion = request.getMessages().get(request.getMessages().size() - 1).getContent();
            } else {
                lastUserQuestion = "你好";
            }

            String context = ragService.getRelevantContext(request.getNamespace(), lastUserQuestion);

            if (context != null && !context.isEmpty()) {
                String systemPrompt = "你是一个智能助手。请根据以下检索到的上下文信息回答问题：\n\n" +
                        "------ 上下文开始 ------\n" +
                        context + "\n" +
                        "------ 上下文结束 ------\n\n" +
                        "如果上下文中没有答案，请根据你的通用知识回答。";

                ChatRequest.MessageDTO systemMsg = new ChatRequest.MessageDTO();
                systemMsg.setRole("system");
                systemMsg.setContent(systemPrompt);
                messagesToSent.add(systemMsg);
            }
        }

        if (request.getMessages() != null) {
            messagesToSent.addAll(request.getMessages());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getChatModel());
        List<ChatRequest.MessageDTO> messagesBody = new ArrayList<>();
        if (messagesToSent != null && !messagesToSent.isEmpty()) {
            messagesBody.addAll(messagesToSent);
        }
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            messagesBody.addAll(request.getMessages());
        }
        body.put("messages", messagesBody);
        body.put("stream", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        StreamingResponseBody stream = out -> {
            restTemplate.execute(url, HttpMethod.POST, clientHttpRequest -> {
                clientHttpRequest.getHeaders().putAll(entity.getHeaders());

                for (var converter : restTemplate.getMessageConverters()) {
                    if (converter.canWrite(Map.class, MediaType.APPLICATION_JSON)) {
                        ((HttpMessageConverter<Object>) converter).write(entity.getBody(), MediaType.APPLICATION_JSON, clientHttpRequest);
                        return;
                    }
                }
                throw new IllegalStateException("No HttpMessageConverter found for Map");

            }, clientHttpResponse -> {
                if (clientHttpResponse.getBody() != null) {
                    InputStream inputStream = clientHttpResponse.getBody();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        out.flush();
                    }
                }
                return null;
            });
        };

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_EVENT_STREAM);
        responseHeaders.setCacheControl("no-cache");

        return new ResponseEntity<>(stream, responseHeaders, HttpStatus.OK);
    }
}