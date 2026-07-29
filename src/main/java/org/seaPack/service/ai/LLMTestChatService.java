package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.ChatRequest;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.model.ai.ExecutionSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LLM 测试对话服务
 * <p>提供 LLM 模式的 SSE 流式对话，逐 token 发送 content 事件，
 * 完成时发送 done 事件（含 token 统计），并保存执行记录到 ai_execution_session 表。</p>
 */
@Slf4j
@Service
public class LLMTestChatService {

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 用户对话取消标志 key=userId, value=是否已取消 */
    private final Map<Long, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /**
     * 取消指定用户的 LLM 流式对话
     */
    public void cancelStream(Long userId) {
        if (userId != null) {
            AtomicBoolean flag = cancelFlags.get(userId);
            if (flag != null) {
                flag.set(true);
            }
        }
    }

    /**
     * 执行 LLM SSE 流式测试对话
     * <p>流程：构建请求 → 流式调用 AI API → 逐 token 发送 content 事件 → 发送 done 事件（含 token 统计）→ 异步入库</p>
     *
     * @param request  对话请求（消息列表、命名空间等）
     * @param userId   当前用户 ID
     * @param emitter  SSE 发射器
     * @param response HTTP 响应（用于关闭输出流）
     */
    public void testChatStream(ChatRequest request, Long userId, SseEmitter emitter, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        StringBuilder fullContent = new StringBuilder();
        int[] tokenUsage = {0, 0}; // [0] = prompt_tokens, [1] = completion_tokens
        String modelName;

        // 注册取消标志
        AtomicBoolean cancelled = new AtomicBoolean(false);
        if (userId != null) {
            cancelFlags.put(userId, cancelled);
        }

        try {
            // ===== 1. 获取 AI 配置 =====
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            modelName = config.getChatModel();
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            // ===== 2. 构建消息列表（与 ChatController.chat() 逻辑一致） =====
            List<Map<String, String>> messagesToSend = new ArrayList<>();

            if (request.getMessages() != null) {
                for (ChatRequest.MessageDTO msg : request.getMessages()) {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", msg.getRole());
                    msgMap.put("content", msg.getContent());
                    messagesToSend.add(msgMap);
                }
            }

            // ===== 3. 构建请求体 =====
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messagesToSend);
            requestBody.put("stream", true);

            // ===== 4. 发送 step_start 事件 =====
            sendSseEvent(emitter, "step_start", Map.of(
                    "stepIndex", 1,
                    "stepType", "llm_call",
                    "stepName", "LLM 调用"
            ));

            // ===== 5. 流式调用 AI API =====
            HttpURLConnection connection = createStreamingConnection(url, config.getApiKey(), requestBody);
            boolean userCancelled = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 检查用户是否取消
                    if (cancelled.get()) {
                        log.info("用户手动终止 LLM 对话, userId={}", userId);
                        userCancelled = true;
                        break;
                    }

                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }

                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> chunk = objectMapper.readValue(data, Map.class);

                            // 提取 delta 内容
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> choice = choices.get(0);
                                @SuppressWarnings("unchecked")
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                if (delta != null && delta.get("content") != null) {
                                    String content = delta.get("content").toString();
                                    fullContent.append(content);
                                    // 发送 content 事件
                                    sendSseEvent(emitter, "content", Map.of("text", content));
                                }
                            }

                            // 提取 usage（最后一个 chunk 通常会携带）
                            @SuppressWarnings("unchecked")
                            Map<String, Object> usage = (Map<String, Object>) chunk.get("usage");
                            if (usage != null) {
                                tokenUsage[0] = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                                tokenUsage[1] = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
                            }
                        } catch (Exception e) {
                            log.warn("解析 LLM 响应块失败: {}", e.getMessage());
                        }
                    }
                }
            }
            connection.disconnect();

            long totalDuration = System.currentTimeMillis() - startTime;

            if (userCancelled) {
                // ===== 6a. 用户终止：发送 stop + step_done(skip) + done =====
                sendSseEvent(emitter, "stop", Map.of(
                        "message", "用户已终止对话",
                        "durationMs", totalDuration
                ));
                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", 1,
                        "stepType", "llm_call",
                        "stepName", "LLM 调用",
                        "status", "skip",
                        "durationMs", totalDuration
                ));
                sendSseEvent(emitter, "done", Map.of(
                        "durationMs", totalDuration,
                        "totalDurationMs", totalDuration,
                        "model", modelName
                ));
            } else {
                // ===== 6b. 正常完成：发送 step_done + done（含 token 统计） =====
                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", 1,
                        "stepType", "llm_call",
                        "stepName", "LLM 调用",
                        "status", "success",
                        "durationMs", totalDuration
                ));

                Map<String, Object> doneData = new HashMap<>();
                doneData.put("tokens", Map.of(
                        "prompt", tokenUsage[0],
                        "completion", tokenUsage[1]
                ));
                doneData.put("durationMs", totalDuration);
                doneData.put("model", modelName);
                doneData.put("totalDurationMs", totalDuration);
                doneData.put("tokensPrompt", tokenUsage[0]);
                doneData.put("tokensCompletion", tokenUsage[1]);
                sendSseEvent(emitter, "done", doneData);
            }

            // ===== 7. 关闭 SSE 连接 =====
            try {
                response.flushBuffer();
            } catch (Exception ignored) {
            }
            try {
                response.getOutputStream().close();
            } catch (Exception ignored) {
            }
            emitter.complete();

            if (userCancelled) {
                log.info("LLM 测试对话已终止: duration={}ms", totalDuration);
            } else {
                log.info("LLM 测试对话完成: tokens={}/{}, duration={}ms, model={}",
                        tokenUsage[0], tokenUsage[1], totalDuration, modelName);
            }

            // ===== 8. 异步保存执行记录 =====
            try {
                String status = userCancelled ? "cancelled" : "success";
                saveTestSession(request, fullContent.toString(), (int) totalDuration,
                        tokenUsage[0], tokenUsage[1], modelName, status, null, userId);
            } catch (Exception e) {
                log.error("保存 LLM 测试会话失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("LLM 测试对话失败", e);
            sendSseError(emitter, "LLM 测试对话失败: " + e.getMessage());
        } finally {
            // 清理取消标志
            if (userId != null) {
                cancelFlags.remove(userId);
            }
        }
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>(data);
            event.put("type", type);
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("发送 SSE 事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送 SSE 错误事件并关闭连接
     */
    private void sendSseError(SseEmitter emitter, String message) {
        try {
            Map<String, Object> event = Map.of(
                    "type", "error",
                    "message", message
            );
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
            emitter.complete();
        } catch (Exception e) {
            log.warn("发送 SSE 错误事件失败: {}", e.getMessage());
            emitter.complete();
        }
    }

    /**
     * 创建流式 HTTP 连接（向 AI API 发送请求）
     */
    private HttpURLConnection createStreamingConnection(String url, String apiKey, Map<String, Object> requestBody) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(300000); // 5 分钟读取超时

        byte[] body = objectMapper.writeValueAsBytes(requestBody);
        connection.getOutputStream().write(body);
        connection.getOutputStream().flush();

        return connection;
    }

    /**
     * 保存执行会话记录到数据库
     */
    private void saveTestSession(ChatRequest request, String reply, int durationMs,
                                 int promptTokens, int completionTokens, String modelName,
                                 String status, String errorMessage, Long userId) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("chat");
        session.setBizId(0L); // LLM 模式无特定业务 ID
        session.setBizName("LLM 对话");
        session.setModuleKey("ai_assistant");
        session.setUserMessage(extractLastUserMessage(request));
        session.setHistoryMessages(extractHistoryMessages(request));
        session.setOutputResult(reply);
        session.setTotalDurationMs(durationMs);
        session.setTokensPrompt(promptTokens);
        session.setTokensCompletion(completionTokens);
        session.setTokensTotal(promptTokens + completionTokens);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    /**
     * 提取最后一条用户消息
     */
    private String extractLastUserMessage(ChatRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return request.getQuestion() != null ? request.getQuestion() : "";
        }
        // 从后往前找最后一条 role=user 的消息
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            ChatRequest.MessageDTO msg = request.getMessages().get(i);
            if ("user".equals(msg.getRole())) {
                return msg.getContent();
            }
        }
        return "";
    }

    /**
     * 提取历史消息（除最后一条用户消息外的所有消息），序列化为 JSON
     */
    private String extractHistoryMessages(ChatRequest request) {
        if (request.getMessages() == null || request.getMessages().size() <= 1) {
            return null;
        }
        // 找到最后一条 user 消息的索引，去掉它作为当前消息
        int lastUserIdx = -1;
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            if ("user".equals(request.getMessages().get(i).getRole())) {
                lastUserIdx = i;
                break;
            }
        }
        if (lastUserIdx <= 0) {
            return null;
        }
        try {
            List<ChatRequest.MessageDTO> history = request.getMessages().subList(0, lastUserIdx);
            return objectMapper.writeValueAsString(history);
        } catch (Exception e) {
            log.warn("序列化历史消息失败: {}", e.getMessage());
            return null;
        }
    }
}
