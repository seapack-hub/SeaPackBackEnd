package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.ChatRequest;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.model.ai.ExecutionSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.HttpURLConnection;
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

    @Autowired
    private LlmSseHelper llmSseHelper;

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
     */
    public void testChatStream(ChatRequest request, Long userId, SseEmitter emitter, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        StringBuilder fullContent = new StringBuilder();
        int[] tokenUsage = {0, 0};
        String modelName;

        AtomicBoolean cancelled = new AtomicBoolean(false);
        if (userId != null) {
            cancelFlags.put(userId, cancelled);
        }

        try {
            // 1. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                SseEvent.sendError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            modelName = config.getChatModel();
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            // 2. 构建消息列表
            List<Map<String, String>> messagesToSend = new ArrayList<>();
            if (request.getMessages() != null) {
                for (ChatRequest.MessageDTO msg : request.getMessages()) {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", msg.getRole());
                    msgMap.put("content", msg.getContent());
                    messagesToSend.add(msgMap);
                }
            }

            // 3. 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messagesToSend);
            requestBody.put("stream", true);

            // 4. 发送 step_start 事件
            SseEvent.send(emitter, SseEvent.TYPE_STEP_START, SseEvent.stepStart(1, "llm_call", "LLM 调用"));

            // 5. 流式调用 AI API
            boolean userCancelled = false;
            HttpURLConnection connection = llmSseHelper.createConnection(url, config.getApiKey(), requestBody);

            llmSseHelper.readChunks(connection, cancelled, chunk -> {
                if (chunk.isDone()) return;
                if (chunk.hasDeltaContent()) {
                    fullContent.append(chunk.getDeltaContent());
                    SseEvent.send(emitter, SseEvent.TYPE_CONTENT, SseEvent.content(chunk.getDeltaContent()));
                }
                if (chunk.hasUsage()) {
                    tokenUsage[0] = chunk.getPromptTokens() != null ? chunk.getPromptTokens() : tokenUsage[0];
                    tokenUsage[1] = chunk.getCompletionTokens() != null ? chunk.getCompletionTokens() : tokenUsage[1];
                }
            });
            connection.disconnect();

            if (cancelled.get()) {
                userCancelled = true;
            }

            long totalDuration = System.currentTimeMillis() - startTime;

            if (userCancelled) {
                // 6a. 用户终止
                SseEvent.send(emitter, SseEvent.TYPE_STOP, SseEvent.stop("用户已终止对话", totalDuration));
                SseEvent.send(emitter, SseEvent.TYPE_STEP_DONE,
                        SseEvent.stepDone(1, "llm_call", "LLM 调用", "skip", totalDuration));
                SseEvent.send(emitter, SseEvent.TYPE_DONE, Map.of(
                        "durationMs", totalDuration,
                        "totalDurationMs", totalDuration,
                        "model", modelName
                ));
            } else {
                // 6b. 正常完成
                SseEvent.send(emitter, SseEvent.TYPE_STEP_DONE,
                        SseEvent.stepDone(1, "llm_call", "LLM 调用", "success", totalDuration));

                Map<String, Object> doneData = new HashMap<>();
                doneData.put("tokens", Map.of("prompt", tokenUsage[0], "completion", tokenUsage[1]));
                doneData.put("durationMs", totalDuration);
                doneData.put("model", modelName);
                doneData.put("totalDurationMs", totalDuration);
                doneData.put("tokensPrompt", tokenUsage[0]);
                doneData.put("tokensCompletion", tokenUsage[1]);
                SseEvent.send(emitter, SseEvent.TYPE_DONE, doneData);
            }

            // 7. 关闭 SSE
            try { response.flushBuffer(); } catch (Exception ignored) {}
            try { response.getOutputStream().close(); } catch (Exception ignored) {}
            emitter.complete();

            // 8. 异步保存执行记录
            try {
                String status = userCancelled ? "cancelled" : "success";
                saveTestSession(request, fullContent.toString(), (int) totalDuration,
                        tokenUsage[0], tokenUsage[1], modelName, status, null, userId);
            } catch (Exception e) {
                log.error("保存 LLM 测试会话失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("LLM 测试对话失败", e);
            SseEvent.sendError(emitter, "LLM 测试对话失败: " + e.getMessage());
        } finally {
            if (userId != null) {
                cancelFlags.remove(userId);
            }
        }
    }

    // ===== 以下方法保留不变（DB 入库 + 辅助方法） =====

    private void saveTestSession(ChatRequest request, String reply, int durationMs,
                                 int promptTokens, int completionTokens, String modelName,
                                 String status, String errorMessage, Long userId) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("chat");
        session.setBizId(0L);
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

    private String extractLastUserMessage(ChatRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return request.getQuestion() != null ? request.getQuestion() : "";
        }
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            ChatRequest.MessageDTO msg = request.getMessages().get(i);
            if ("user".equals(msg.getRole())) {
                return msg.getContent();
            }
        }
        return "";
    }

    private String extractHistoryMessages(ChatRequest request) {
        if (request.getMessages() == null || request.getMessages().size() <= 1) {
            return null;
        }
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
