package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.*;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.model.ai.ExecutionSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 统一 AI 对话调度服务
 * <p>按 mode 分发到 4 种对话模式，统一管理取消标志和会话记录。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDialogService {

    private final AIProperties aiProperties;
    private final LlmSseHelper llmSseHelper;
    private final AgentTestChatService agentTestChatService;
    private final OrchestrationExecuteService orchestrationExecuteService;
    private final ExecutionSessionMapper executionSessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 统一取消标志 key=userId, value=是否已取消 */
    private final Map<Long, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /**
     * 统一取消 - 通知所有正在进行的对话终止
     */
    public void cancelStream(Long userId) {
        if (userId == null) return;
        log.info("用户请求终止对话, userId={}", userId);
        // 设置 AiDialogService 自身的取消标志
        AtomicBoolean flag = cancelFlags.get(userId);
        if (flag != null) {
            flag.set(true);
        }
    }

    /**
     * 注册取消标志
     */
    private AtomicBoolean registerCancelFlag(Long userId) {
        if (userId == null) return null;
        AtomicBoolean flag = new AtomicBoolean(false);
        cancelFlags.put(userId, flag);
        return flag;
    }

    /**
     * 清理取消标志
     */
    private void removeCancelFlag(Long userId) {
        if (userId != null) {
            cancelFlags.remove(userId);
        }
    }

    // ========================================================================
    //  流式入口
    // ========================================================================

    /**
     * 流式对话（按 mode 分发）
     *
     * @param request  统一请求
     * @param userId   当前用户 ID
     * @param emitter  SSE 发射器
     * @param response HTTP 响应（用于 flush/close）
     */
    public void handleStream(AiDialogRequest request, Long userId, String authToken,
                              SseEmitter emitter, HttpServletResponse response) {
        String mode = request.getMode();
        switch (mode) {
            case "streaming_llm" -> handleLlmStream(request, userId, emitter, response);
            case "agent_stream" -> handleAgentStream(request, userId, authToken, emitter, response);
            case "orchestration" -> handleOrchestration(request, emitter);
            default -> SseEvent.sendError(emitter, "未知对话模式: " + mode);
        }
    }

    /**
     * 非流式对话
     */
    public Map<String, Object> handleSync(AiDialogRequest request) {
        String mode = request.getMode();
        if ("llm_chat".equals(mode)) {
            return handleLlmChat(request);
        }
        throw new IllegalArgumentException("非流式模式不支持: " + mode);
    }

    // ========================================================================
    //  Mode 1: 流式 LLM 对话
    // ========================================================================

    /**
     * 流式 LLM 对话（原 LLMTestChatService.testChatStream 的核心逻辑）
     */
    private void handleLlmStream(AiDialogRequest request, Long userId,
                                  SseEmitter emitter, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        StringBuilder fullContent = new StringBuilder();
        int[] tokenUsage = {0, 0};

        // 注册取消标志
        AtomicBoolean cancelFlag = registerCancelFlag(userId);

        try {
            // 1. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                SseEvent.sendError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }
            String modelName = config.getChatModel();
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

            // 5. 流式调用 LLM
            boolean userCancelled = false;
            HttpURLConnection connection = llmSseHelper.createConnection(url, config.getApiKey(), requestBody);

            llmSseHelper.readChunks(connection, cancelFlag, chunk -> {
                if (chunk.isDone()) {
                    return; // [DONE] 由 readChunks 内部处理
                }
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

            // 检查是否被取消标志中断
            if (cancelFlag != null && cancelFlag.get()) {
                userCancelled = true;
            }

            long totalDuration = System.currentTimeMillis() - startTime;

            if (userCancelled || (cancelFlag != null && cancelFlag.get())) {
                // 6a. 用户终止
                userCancelled = true;
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
                saveLlmSession(request, fullContent.toString(), (int) totalDuration,
                        tokenUsage[0], tokenUsage[1], modelName, status, null, userId);
            } catch (Exception e) {
                log.error("保存 LLM 对话会话失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("LLM 流式对话失败", e);
            SseEvent.sendError(emitter, "LLM 对话失败: " + e.getMessage());
        } finally {
            removeCancelFlag(userId);
        }
    }

    // ========================================================================
    //  Mode 2: 非流式 LLM 对话
    // ========================================================================

    /**
     * 非流式 LLM 对话
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleLlmChat(AiDialogRequest request) {
        // 1. 获取 AI 配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        String modelName = config.getChatModel();
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

        // 3. 构建请求体（非流式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messagesToSend);
        requestBody.put("stream", false);

        // 4. 调用 LLM
        try {
            Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);

            String content = "";
            int promptTokens = 0;
            int completionTokens = 0;

            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.get("content") != null) {
                    content = message.get("content");
                }
            }

            Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
            if (usage != null) {
                promptTokens = usage.get("prompt_tokens") != null ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
                completionTokens = usage.get("completion_tokens") != null ? ((Number) usage.get("completion_tokens")).intValue() : 0;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("tokensPrompt", promptTokens);
            result.put("tokensCompletion", completionTokens);
            result.put("model", modelName);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("LLM 对话失败: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    //  Mode 3: Agent 流式对话
    // ========================================================================

    /**
     * Agent 流式对话（委托给 AgentTestChatService）
     */
    private void handleAgentStream(AiDialogRequest request, Long userId, String authToken,
                                    SseEmitter emitter, HttpServletResponse response) {
        if (request.getAgentId() == null) {
            SseEvent.sendError(emitter, "Agent ID 不能为空");
            return;
        }

        // 注册取消标志（AiDialogService 统一管理）
        AtomicBoolean cancelFlag = registerCancelFlag(userId);
        try {
            agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
        } finally {
            removeCancelFlag(userId);
        }
    }

    // ========================================================================
    //  Mode 4: 编排对话
    // ========================================================================

    /**
     * 编排对话（委托给 OrchestrationExecuteService）
     */
    private void handleOrchestration(AiDialogRequest request, SseEmitter emitter) {
        if (request.getOrchestrationId() == null) {
            SseEvent.sendError(emitter, "编排 ID 不能为空");
            return;
        }

        // 转换为 OrchestrationExecuteRequest
        OrchestrationExecuteRequest orchRequest = new OrchestrationExecuteRequest();
        orchRequest.setOrchestrationId(request.getOrchestrationId());
        orchRequest.setMessage(request.getQuestion() != null ? request.getQuestion()
                : extractLastMessage(request.getMessages()));
        orchRequest.setHistory(request.getHistory());
        orchRequest.setContext(request.getContext());

        orchestrationExecuteService.execute(orchRequest, emitter);
    }

    // ========================================================================
    //  辅助方法
    // ========================================================================

    /**
     * 提取最后一条消息内容
     */
    private String extractLastMessage(List<ChatRequest.MessageDTO> messages) {
        if (messages == null || messages.isEmpty()) return "";
        return messages.get(messages.size() - 1).getContent();
    }

    /**
     * 保存 LLM 执行会话
     */
    private void saveLlmSession(AiDialogRequest request, String reply, int durationMs,
                                 int promptTokens, int completionTokens, String modelName,
                                 String status, String errorMessage, Long userId) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("chat");
        session.setBizId(0L);
        session.setBizName("LLM 对话");
        session.setModuleKey("ai_assistant");
        session.setUserMessage(request.getQuestion() != null ? request.getQuestion()
                : extractLastMessage(request.getMessages()));
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
}
