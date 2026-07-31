package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.*;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.mapper.ai.SceneAgentMapper;
import org.seaPack.mapper.ai.SceneMapper;
import org.seaPack.mapper.ai.SceneOrchestrationMapper;
import org.seaPack.mapper.ai.SceneOrchestrationStepMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.ExecutionSession;
import org.seaPack.model.ai.Scene;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.model.ai.SceneAgent;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
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
    private final SceneOrchestrationMapper orchestrationMapper;
    private final SceneOrchestrationStepMapper orchestrationStepMapper;
    private final SceneMapper sceneMapper;
    private final SceneAgentMapper sceneAgentMapper;
    private final AgentMapper agentMapper;
    private final TokenStatsService tokenStatsService;

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
            case "orchestration" -> handleOrchestration(request, userId, authToken, emitter, response);
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
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                for (ChatRequest.MessageDTO msg : request.getMessages()) {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", msg.getRole());
                    msgMap.put("content", msg.getContent());
                    messagesToSend.add(msgMap);
                }
            }
            // messages 为空时，用 history + question 构建
            if (messagesToSend.isEmpty()) {
                if (request.getHistory() != null) {
                    for (Map<String, String> h : request.getHistory()) {
                        Map<String, String> msgMap = new HashMap<>();
                        msgMap.put("role", h.get("role"));
                        msgMap.put("content", h.get("content") != null ? h.get("content") : "");
                        messagesToSend.add(msgMap);
                    }
                }
                String question = request.getQuestion() != null ? request.getQuestion() : "";
                if (!question.isBlank()) {
                    messagesToSend.add(Map.of("role", "user", "content", question));
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

            // 记录本次 LLM 调用的 Token 消耗到统计表
            try {
                long llmDuration = System.currentTimeMillis() - startTime;
                TokenUsageLog tokenLog = new TokenUsageLog();
                tokenLog.setCallTime(new Date());
                tokenLog.setModelName(modelName);
                tokenLog.setTokensInput(tokenUsage[0]);
                tokenLog.setTokensOutput(tokenUsage[1]);
                tokenLog.setDurationMs((int) llmDuration);
                tokenLog.setStatus("success");
                tokenLog.setUserId(userId);
                tokenLog.setBizType("chat");
                tokenLog.setSceneId(request.getSceneId());
                tokenLog.setRequestId(request.getRequestId());
                tokenStatsService.recordCall(tokenLog);
            } catch (Exception e) {
                log.error("记录 Token 统计失败: {}", e.getMessage(), e);
            }

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
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatRequest.MessageDTO msg : request.getMessages()) {
                Map<String, String> msgMap = new HashMap<>();
                msgMap.put("role", msg.getRole());
                msgMap.put("content", msg.getContent());
                messagesToSend.add(msgMap);
            }
        }
        // messages 为空时，用 history + question 构建
        if (messagesToSend.isEmpty()) {
            if (request.getHistory() != null) {
                for (Map<String, String> h : request.getHistory()) {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", h.get("role"));
                    msgMap.put("content", h.get("content") != null ? h.get("content") : "");
                    messagesToSend.add(msgMap);
                }
            }
            String question = request.getQuestion() != null ? request.getQuestion() : "";
            if (!question.isBlank()) {
                messagesToSend.add(Map.of("role", "user", "content", question));
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
    //  Mode 4: 编排对话（LLM 动态路由）
    // ========================================================================

    /**
     * 编排对话（智能路由，LLM 动态选择 Agent）
     * <p>路由阶段只发2个事件：routing（开始）→ route_result（结果），
     * 然后根据结果分流到编排执行 / Agent 对话 / 通用 LLM。</p>
     */
    @SuppressWarnings("unchecked")
    private void handleOrchestration(AiDialogRequest request, Long userId, String authToken,
                                      SseEmitter emitter, HttpServletResponse response) {
        Long orchestrationId = request.getOrchestrationId();
        Long sceneId = request.getSceneId();
        Long agentId = request.getAgentId();
        String userMessage = request.getQuestion() != null ? request.getQuestion()
                : extractLastMessage(request.getMessages());

        // 1. 发送 routing 事件（路由开始）
        SseEvent.send(emitter, SseEvent.TYPE_ROUTING, Map.of(
                "orchestrationId", orchestrationId != null ? orchestrationId : "",
                "sceneId", sceneId != null ? sceneId : "",
                "agentId", agentId != null ? agentId : "",
                "message", "正在分析请求，确定执行策略..."
        ));

        // 2. 路由逻辑（内部不再发 SSE 事件）
        SceneOrchestration orchestration = null;
        Scene scene = null;

        // 查编排
        if (orchestrationId != null) {
            orchestration = orchestrationMapper.selectById(orchestrationId);
        }
        if (orchestration == null && sceneId != null) {
            scene = sceneMapper.selectById(sceneId);
            if (scene != null) {
                orchestration = findDefaultOrchestration(scene.getId());
            }
        }
        if (orchestration == null && orchestrationId != null) {
            scene = sceneMapper.selectById(orchestrationId);
            if (scene != null) {
                orchestration = findDefaultOrchestration(scene.getId());
            }
        }

        // 有编排 + 有步骤 → 编排执行
        if (orchestration != null) {
            if (scene == null && orchestration.getSceneId() != null) {
                scene = sceneMapper.selectById(orchestration.getSceneId());
            }
            List<SceneOrchestrationStep> steps = orchestrationStepMapper.selectByOrchestrationId(orchestration.getId());
            if (steps != null && !steps.isEmpty()) {
                SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                        "route", "orchestration",
                        "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "",
                        "stepCount", steps.size(),
                        "message", "使用编排 [" + orchestration.getName() + "]（" + steps.size() + " 个步骤）"
                ));
                log.info("路由到编排执行: orchestration={}, steps={}", orchestration.getName(), steps.size());
                orchestrationExecuteService.execute(buildOrchRequest(request), userId, emitter);
                return;
            }
        }

        // 收集候选 Agent
        if (scene == null && orchestration != null && orchestration.getSceneId() != null) {
            scene = sceneMapper.selectById(orchestration.getSceneId());
        }
        List<Agent> candidates = collectCandidateAgents(scene, agentId);

        // 无候选 → 通用 LLM
        if (candidates.isEmpty()) {
            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "llm",
                    "message", "无可用 Agent，使用通用 LLM 对话"
            ));
            log.info("无候选 Agent，路由到通用 LLM 对话");
            handleLlmStream(request, userId, emitter, response);
            return;
        }

        // 1 个候选 → 直接用
        if (candidates.size() == 1) {
            Agent onlyAgent = candidates.get(0);
            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "agent",
                    "agents", List.of(Map.of("id", onlyAgent.getId(),
                            "name", onlyAgent.getName() != null ? onlyAgent.getName() : "")),
                    "strategy", "sequential",
                    "message", "使用 Agent [" + onlyAgent.getName() + "]"
            ));
            log.info("仅 1 个候选 Agent [{}]，直接使用", onlyAgent.getName());

            SseEvent.send(emitter, SseEvent.TYPE_AGENT_SELECT, Map.of(
                    "agents", List.of(Map.of("id", onlyAgent.getId(),
                            "name", onlyAgent.getName() != null ? onlyAgent.getName() : "",
                            "reason", "唯一候选 Agent")),
                    "strategy", "sequential"
            ));

            request.setAgentId(onlyAgent.getId());
            if (scene != null) request.setSceneId(scene.getId());
            agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
            return;
        }

        // 多候选 → LLM 选择
        log.info("候选 Agent {} 个，调用 LLM 动态选择", candidates.size());
        Map<String, Object> llmSelectResult = agentSelectByLLM(userMessage, candidates, emitter);

        // LLM 选择失败 → 默认 Agent
        if (llmSelectResult == null) {
            Agent defaultAgent = candidates.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                    .findFirst().orElse(candidates.get(0));

            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "agent",
                    "agents", List.of(Map.of("id", defaultAgent.getId(),
                            "name", defaultAgent.getName() != null ? defaultAgent.getName() : "")),
                    "strategy", "sequential",
                    "fallback", true,
                    "message", "Agent 选择失败，使用默认 Agent [" + defaultAgent.getName() + "]"
            ));
            log.warn("LLM Agent 选择失败，降级到默认 Agent");

            SseEvent.send(emitter, SseEvent.TYPE_AGENT_SELECT, Map.of(
                    "agents", List.of(Map.of("id", defaultAgent.getId(),
                            "name", defaultAgent.getName() != null ? defaultAgent.getName() : "",
                            "reason", "LLM 选择失败，使用默认 Agent")),
                    "strategy", "sequential",
                    "fallback", true
            ));

            request.setAgentId(defaultAgent.getId());
            if (scene != null) request.setSceneId(scene.getId());
            agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
            return;
        }

        // 解析 LLM 选择结果
        List<Map<String, Object>> selectedAgents = (List<Map<String, Object>>) llmSelectResult.get("agents");
        String strategy = (String) llmSelectResult.getOrDefault("strategy", "sequential");

        // LLM 选 0 个 → 通用 LLM
        if (selectedAgents == null || selectedAgents.isEmpty()) {
            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "llm",
                    "message", "Agent 选择结果为空，使用通用 LLM 对话"
            ));
            log.info("LLM 判断不需要 Agent，路由到通用 LLM 对话");
            handleLlmStream(request, userId, emitter, response);
            return;
        }

        // 发送 agent_select
        SseEvent.send(emitter, SseEvent.TYPE_AGENT_SELECT, llmSelectResult);

        // LLM 选 1 个 → Agent 对话
        if (selectedAgents.size() == 1) {
            Map<String, Object> selected = selectedAgents.get(0);
            Long selectedId = ((Number) selected.get("id")).longValue();
            String selectedName = selected.get("name") != null ? (String) selected.get("name") : "";

            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "agent",
                    "agents", List.of(Map.of("id", selectedId, "name", selectedName)),
                    "strategy", "sequential",
                    "message", "使用 Agent [" + selectedName + "]"
            ));
            log.info("LLM 选中 1 个 Agent: id={}", selectedId);

            request.setAgentId(selectedId);
            if (scene != null) request.setSceneId(scene.getId());
            agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
        } else {
            // LLM 选多个 → 动态编排
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < selectedAgents.size(); i++) {
                if (i > 0) names.append(", ");
                Object n = selectedAgents.get(i).get("name");
                names.append(n != null ? n : "Agent");
            }

            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "dynamic_orchestration",
                    "agents", selectedAgents,
                    "strategy", strategy,
                    "message", "动态编排 " + selectedAgents.size() + " 个 Agent: [" + names + "]，策略: " + strategy
            ));
            log.info("LLM 选中 {} 个 Agent，启动动态编排", selectedAgents.size());

            List<SceneOrchestrationStep> dynamicSteps = buildDynamicSteps(selectedAgents);
            orchestrationExecuteService.executeDynamic(dynamicSteps, strategy, userMessage,
                    request.getHistory(), request.getSceneId(), request.getConversationId(), request.getRequestId(),
                    userId, emitter);
        }
    }

    // ========================================================================
    //  LLM Agent 选择
    // ========================================================================

    /**
     * 调用 LLM 分析用户意图，从候选 Agent 中选择合适的 Agent
     *
     * @param userMessage 用户消息
     * @param candidates  候选 Agent 列表
     * @param emitter     SSE 发射器（用于发送进度）
     * @return 选择结果 { agents: [{id, name, reason}], strategy: "sequential"|"parallel" }，失败返回 null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> agentSelectByLLM(String userMessage, List<Agent> candidates,
                                                  SseEmitter emitter) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                log.error("AI 配置错误：未找到提供商 [{}]", providerName);
                return null;
            }

            String modelName = config.getChatModel();
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            // 2. 构建候选 Agent 描述
            StringBuilder agentListBuilder = new StringBuilder();
            for (int i = 0; i < candidates.size(); i++) {
                Agent a = candidates.get(i);
                agentListBuilder.append("- Agent ").append(i + 1)
                        .append(" (id=").append(a.getId())
                        .append(", name=\"").append(a.getName() != null ? a.getName() : "未命名").append("\"");
                if (a.getDescription() != null && !a.getDescription().isBlank()) {
                    agentListBuilder.append(", desc=\"").append(a.getDescription()).append("\"");
                }
                agentListBuilder.append(")\n");
            }

            // 3. 构建 System Prompt
            String systemPrompt = "你是一个 Agent 路由器。根据用户消息，从候选 Agent 列表中选择合适的 Agent。\n\n"
                    + "候选 Agent：\n" + agentListBuilder.toString() + "\n"
                    + "请返回 JSON 格式（不要包含其他文字）：\n"
                    + "{\n"
                    + "  \"agents\": [{\"id\": <agent_id>, \"name\": \"<agent_name>\", \"reason\": \"选择原因\"}],\n"
                    + "  \"strategy\": \"sequential\" 或 \"parallel\"\n"
                    + "}\n\n"
                    + "规则：\n"
                    + "1. 如果用户需求只需要一个 Agent，只返回一个\n"
                    + "2. 如果用户需求需要多个 Agent 协作，返回多个，并指定执行策略：\n"
                    + "   - sequential：Agent 之间有依赖，上一步输出是下一步输入\n"
                    + "   - parallel：Agent 之间无依赖，可并行执行\n"
                    + "3. 如果没有任何 Agent 能满足需求，返回空数组 agents: []\n"
                    + "4. 只返回 JSON，不要包含其他文字";

            // 4. 构建消息
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));

            // 5. 调用 LLM（非流式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1); // 低温度，确保选择稳定

            Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);

            // 6. 解析响应
            String content = "";
            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.get("content") != null) {
                    content = message.get("content");
                }
            }

            if (content.isBlank()) {
                log.warn("LLM Agent 选择返回空内容");
                return null;
            }

            // 7. 提取 JSON（可能被 markdown 代码块包裹）
            String jsonStr = content.trim();
            if (jsonStr.contains("```")) {
                // 去掉 markdown 代码块标记
                jsonStr = jsonStr.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);
            long duration = System.currentTimeMillis() - startTime;
            log.info("LLM Agent 选择完成: 耗时={}ms, 选中={}个 Agent", duration,
                    result.get("agents") != null ? ((List<?>) result.get("agents")).size() : 0);
            return result;

        } catch (Exception e) {
            log.error("LLM Agent 选择调用失败: {}", e.getMessage(), e);
            return null;
        }
    }

    // ========================================================================
    //  候选 Agent 收集
    // ========================================================================

    /**
     * 收集候选 Agent 列表
     * <p>优先从场景关联的 Agent 中收集，如果未指定场景则使用请求中的 agentId</p>
     *
     * @param scene   场景（可为 null）
     * @param agentId 请求中指定的 Agent ID（可为 null）
     * @return 候选 Agent 列表（已过滤禁用的）
     */
    private List<Agent> collectCandidateAgents(Scene scene, Long agentId) {
        List<Agent> candidates = new ArrayList<>();

        if (scene != null) {
            // 从场景关联的 Agent 中收集
            List<SceneAgent> sceneAgents = sceneAgentMapper.selectBySceneId(scene.getId());
            if (sceneAgents != null && !sceneAgents.isEmpty()) {
                for (SceneAgent sa : sceneAgents) {
                    Agent agent = agentMapper.selectById(sa.getAgentId());
                    if (agent != null && agent.getStatus() != null && agent.getStatus() == 1) {
                        candidates.add(agent);
                    }
                }
            }
        }

        if (candidates.isEmpty() && agentId != null) {
            // 场景无 Agent 或无场景，使用请求中指定的 Agent
            Agent agent = agentMapper.selectById(agentId);
            if (agent != null && agent.getStatus() != null && agent.getStatus() == 1) {
                candidates.add(agent);
            }
        }

        return candidates;
    }

    // ========================================================================
    //  动态步骤构建
    // ========================================================================

    /**
     * 根据 LLM 选择结果动态构建编排步骤
     *
     * @param selectedAgents LLM 选择的 Agent 列表 [{id, name, reason}]
     * @return 动态构建的步骤列表
     */
    private List<SceneOrchestrationStep> buildDynamicSteps(List<Map<String, Object>> selectedAgents) {
        List<SceneOrchestrationStep> steps = new ArrayList<>();
        for (int i = 0; i < selectedAgents.size(); i++) {
            Map<String, Object> item = selectedAgents.get(i);
            Long agentId = ((Number) item.get("id")).longValue();
            String name = item.get("name") != null ? (String) item.get("name") : ("Agent " + agentId);

            SceneOrchestrationStep step = new SceneOrchestrationStep();
            step.setStepIndex(i + 1);
            step.setStepName(name);
            step.setAgentId(agentId);
            // 第一步用用户原始输入，后续步骤引用上一步输出
            if (i == 0) {
                step.setInputMapping(null); // 使用默认用户输入
            } else {
                step.setInputMapping("${step_" + i + ".output}");
            }
            step.setStatus(1);
            steps.add(step);
        }
        return steps;
    }

    /**
     * 查找场景下第一个启用的编排（按 sort_order 升序）
     */
    private SceneOrchestration findDefaultOrchestration(Long sceneId) {
        List<SceneOrchestration> orchestrations = orchestrationMapper.selectBySceneId(sceneId);
        if (orchestrations == null || orchestrations.isEmpty()) {
            return null;
        }
        return orchestrations.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                .findFirst()
                .orElse(null);
    }

    /**
     * 构建编排执行请求
     */
    private OrchestrationExecuteRequest buildOrchRequest(AiDialogRequest request) {
        OrchestrationExecuteRequest orchRequest = new OrchestrationExecuteRequest();
        orchRequest.setOrchestrationId(request.getOrchestrationId() != null
                ? request.getOrchestrationId() : request.getSceneId());
        orchRequest.setMessage(request.getQuestion() != null ? request.getQuestion()
                : extractLastMessage(request.getMessages()));
        orchRequest.setHistory(request.getHistory());
        orchRequest.setContext(request.getContext());
        // 透传会话定位参数（落库：scene_id / conversation_id / request_id）
        orchRequest.setSceneId(request.getSceneId());
        orchRequest.setConversationId(request.getConversationId());
        orchRequest.setRequestId(request.getRequestId());
        return orchRequest;
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
     * 按消息ID查询单条执行记录（点击消息气泡查看完整链路）
     */
    public ExecutionSession getSessionByRequestId(String requestId) {
        return executionSessionMapper.selectByRequestId(requestId);
    }

    /**
     * 按对话ID查询该会话的所有轮次（对话历史回显）
     */
    public List<ExecutionSession> getSessionsByConversationId(String conversationId) {
        return executionSessionMapper.selectListByConversationId(conversationId);
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
        session.setBizName("通用对话");
        session.setSceneId(request.getSceneId());
        session.setConversationId(request.getConversationId());
        session.setRequestId(request.getRequestId());
        session.setUserMessage(request.getQuestion() != null ? request.getQuestion()
                : extractLastMessage(request.getMessages()));
        session.setOutputResult(reply);
        // trace_snapshot：通用 LLM 简化链路（route = "llm"）
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("route", "llm");
        trace.put("model", modelName);
        trace.put("tokensPrompt", promptTokens);
        trace.put("tokensCompletion", completionTokens);
        try {
            session.setTraceSnapshot(objectMapper.writeValueAsString(trace));
        } catch (Exception e) {
            log.warn("序列化 LLM 链路快照失败: {}", e.getMessage());
        }
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
