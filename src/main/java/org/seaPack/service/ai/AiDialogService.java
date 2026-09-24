package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.config.AiProviderIdentities;
import org.seaPack.dto.ai.*;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.mapper.ai.SceneMapper;
import org.seaPack.mapper.ai.SceneOrchestrationMapper;
import org.seaPack.mapper.ai.SceneOrchestrationStepMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.ExecutionSession;
import org.seaPack.model.ai.Scene;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.service.ai.orchestration.IntentMatchService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
    private final TokenStatsService tokenStatsService;
    private final TokenQuotaService tokenQuotaService;
    private final IntentMatchService intentMatchService;

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
        // 额度校验：调用大模型前检查用户剩余额度，超限则拒绝并返回提示
        String quotaError = tokenQuotaService.checkQuota(userId);
        if (quotaError != null) {
            SseEvent.sendError(emitter, quotaError);
            sendDoneAndClose(emitter, response, quotaError);
            return;
        }

        String mode = request.getMode();
        switch (mode) {
            case "streaming_llm" -> handleLlmStream(request, userId, emitter, response);
            case "agent_stream" -> handleAgentStream(request, userId, authToken, emitter, response);
            case "orchestration" -> handleOrchestration(request, userId, authToken, emitter, response);
            default -> {
                SseEvent.sendError(emitter, "未知对话模式: " + mode);
                sendDoneAndClose(emitter, response, "未知对话模式");
            }
        }
    }

    /**
     * 非流式对话
     */
    public Map<String, Object> handleSync(AiDialogRequest request, Long userId) {
        // 额度校验：非流式对话同样需要检查
        String quotaError = tokenQuotaService.checkQuota(userId);
        if (quotaError != null) {
            throw new RuntimeException(quotaError);
        }

        String mode = request.getMode();
        if ("llm_chat".equals(mode)) {
            return handleLlmChat(request, userId);
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
                sendDoneAndClose(emitter, response, "AI 配置错误");
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

            // 2.5 注入 provider 身份系统提示词（在消息列表最前面，确保模型知道自己是谁）
            // 优先从常量类获取，其次从配置文件 system-prompt 覆盖
            String providerIdentity = AiProviderIdentities.get(providerName);
            if (providerIdentity == null || providerIdentity.isBlank()) {
                providerIdentity = config.getSystemPrompt();
            }
            if (providerIdentity != null && !providerIdentity.isBlank()) {
                Map<String, String> identityMsg = new HashMap<>();
                identityMsg.put("role", "system");
                identityMsg.put("content", providerIdentity);
                messagesToSend.add(0, identityMsg);
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
            sendDoneAndClose(emitter, response, "LLM 对话失败");
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
    private Map<String, Object> handleLlmChat(AiDialogRequest request, Long userId) {
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
        
        // 2.5 注入 provider 身份系统提示词（在消息列表最前面，确保模型知道自己是谁）
        // 优先从常量类获取，其次从配置文件 system-prompt 覆盖
        String providerIdentity = AiProviderIdentities.get(providerName);
        if (providerIdentity == null || providerIdentity.isBlank()) {
            providerIdentity = config.getSystemPrompt();
        }
        if (providerIdentity != null && !providerIdentity.isBlank()) {
            Map<String, String> identityMsg = new HashMap<>();
            identityMsg.put("role", "system");
            identityMsg.put("content", providerIdentity);
            messagesToSend.add(0, identityMsg);
        }
        
        // 3. 构建请求体（非流式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messagesToSend);
        requestBody.put("stream", false);

        // 4. 调用 LLM
        try {
            long llmStart = System.currentTimeMillis();
            Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);
            long llmDuration = System.currentTimeMillis() - llmStart;

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

            // 记录本次 LLM 调用的 Token 消耗到统计表
            try {
                TokenUsageLog tokenLog = new TokenUsageLog();
                tokenLog.setCallTime(new Date());
                tokenLog.setModelName(modelName);
                tokenLog.setTokensInput(promptTokens);
                tokenLog.setTokensOutput(completionTokens);
                tokenLog.setDurationMs((int) llmDuration);
                tokenLog.setStatus("success");
                tokenLog.setUserId(userId);
                tokenLog.setBizType("chat");
                tokenLog.setRequestId(request.getRequestId());
                tokenStatsService.recordCall(tokenLog);
            } catch (Exception ex) {
                log.error("记录非流式 LLM Token 统计失败: {}", ex.getMessage(), ex);
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
            agentTestChatService.testChatStream(request, userId, emitter, authToken, response, cancelFlag);
        } finally {
            removeCancelFlag(userId);
        }
    }

    // ========================================================================
    //  Mode 4: 编排对话（LLM 动态路由）
    // ========================================================================

    /**
     * 编排对话（意图优先路由）
     * <p>使用 IntentMatchService 分析用户意图，智能选择执行路径：
     * 编排 / Agent / 动态编排 / 通用 LLM。</p>
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

        // 2. 加载场景数据
        Scene scene = null;
        if (sceneId != null) {
            scene = sceneMapper.selectById(sceneId);
        }

        // 3. 加载场景下所有启用的编排
        List<SceneOrchestration> orchestrations = Collections.emptyList();
        if (scene != null) {
            List<SceneOrchestration> all = orchestrationMapper.selectBySceneId(scene.getId());
            if (all != null) {
                orchestrations = all.stream()
                        .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                        .sorted(Comparator.comparingInt(o -> o.getSortOrder() != null ? o.getSortOrder() : 0))
                        .collect(Collectors.toList());
            }
        }

        // 4. 意图匹配（核心：LLM 分析用户意图，选择最优路径）
        IntentMatchService.MatchResult matchResult = intentMatchService.match(
                userMessage, sceneId, orchestrations,
                orchestrationId, agentId, userId, request.getRequestId());

        log.info("[意图路由] route={}, reason={}", matchResult.route, matchResult.reason);

        // 5. 根据路由结果分发执行
        switch (matchResult.route) {
            case IntentMatchService.ROUTE_ORCHESTRATION -> {
                // 匹配到编排 → 执行固定流程
                SceneOrchestration orch = matchResult.orchestration;
                if (scene == null && orch.getSceneId() != null) {
                    scene = sceneMapper.selectById(orch.getSceneId());
                }
                List<SceneOrchestrationStep> steps = orchestrationStepMapper.selectByOrchestrationId(orch.getId());
                if (steps != null && !steps.isEmpty()) {
                    SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                            "route", "orchestration",
                            "orchestrationName", orch.getName() != null ? orch.getName() : "",
                            "stepCount", steps.size(),
                            "message", matchResult.reason
                    ));
                    orchestrationExecuteService.execute(buildOrchRequest(request), userId, emitter);
                    return;
                }
                // 编排无步骤，降级到 Agent
                log.warn("[意图路由] 编排 [{}] 无步骤，降级到 Agent", orch.getName());
                fallBackToAgent(matchResult, request, scene, userId, authToken, emitter, response);
            }
            case IntentMatchService.ROUTE_AGENT -> {
                // 单 Agent 路由
                Agent selected = matchResult.agents.get(0);
                SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                        "route", "agent",
                        "agents", List.of(Map.of("id", selected.getId(),
                                "name", selected.getName() != null ? selected.getName() : "")),
                        "strategy", "sequential",
                        "message", matchResult.reason
                ));
                SseEvent.send(emitter, SseEvent.TYPE_AGENT_SELECT, Map.of(
                        "agents", List.of(Map.of("id", selected.getId(),
                                "name", selected.getName() != null ? selected.getName() : "",
                                "reason", matchResult.reason)),
                        "strategy", "sequential"
                ));
                request.setAgentId(selected.getId());
                if (scene != null) request.setSceneId(scene.getId());
                agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
            }
            case IntentMatchService.ROUTE_DYNAMIC -> {
                // 动态编排（多 Agent 协作）
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < matchResult.agents.size(); i++) {
                    if (i > 0) names.append(", ");
                    names.append(matchResult.agents.get(i).getName() != null
                            ? matchResult.agents.get(i).getName() : "Agent");
                }
                SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                        "route", "dynamic_orchestration",
                        "agents", matchResult.agents.stream().map(a ->
                                (Object) Map.of("id", a.getId(),
                                        "name", a.getName() != null ? a.getName() : "")).collect(Collectors.toList()),
                        "strategy", matchResult.strategy != null ? matchResult.strategy : "sequential",
                        "message", matchResult.reason
                ));
                List<Map<String, Object>> agentMaps = new ArrayList<>();
                for (Agent a : matchResult.agents) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("name", a.getName() != null ? a.getName() : ("Agent " + a.getId()));
                    agentMaps.add(m);
                }
                List<SceneOrchestrationStep> dynamicSteps = buildDynamicSteps(agentMaps);
                orchestrationExecuteService.executeDynamic(dynamicSteps,
                        matchResult.strategy != null ? matchResult.strategy : "sequential",
                        userMessage, request.getHistory(), request.getSceneId(),
                        request.getConversationId(), request.getRequestId(), userId, emitter);
            }
            default -> {
                // 通用 LLM
                SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                        "route", "llm",
                        "message", matchResult.reason
                ));
                handleLlmStream(request, userId, emitter, response);
            }
        }
    }

    /**
     * Agent 路由降级（编排无步骤或匹配失败时）
     */
    private void fallBackToAgent(IntentMatchService.MatchResult matchResult,
                                 AiDialogRequest request, Scene scene,
                                 Long userId, String authToken,
                                 SseEmitter emitter, HttpServletResponse response) {
        if (!matchResult.agents.isEmpty()) {
            Agent agent = matchResult.agents.get(0);
            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "agent",
                    "agents", List.of(Map.of("id", agent.getId(),
                            "name", agent.getName() != null ? agent.getName() : "")),
                    "strategy", "sequential",
                    "fallback", true,
                    "message", "编排执行降级，使用 Agent [" + agent.getName() + "]"
            ));
            request.setAgentId(agent.getId());
            if (scene != null) request.setSceneId(scene.getId());
            agentTestChatService.testChatStream(request, userId, emitter, authToken, response);
        } else {
            SseEvent.send(emitter, SseEvent.TYPE_ROUTE_RESULT, Map.of(
                    "route", "llm",
                    "message", "无可用 Agent，降级到通用 LLM"
            ));
            handleLlmStream(request, userId, emitter, response);
        }
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

    /**
     * 发送 done 事件并关闭 SSE 连接
     * <p>在异常路径中调用，确保前端能收到 done 事件并关闭连接。</p>
     */
    private void sendDoneAndClose(SseEmitter emitter, HttpServletResponse response, String errorMessage) {
        try {
            SseEvent.send(emitter, SseEvent.TYPE_DONE, Map.of(
                    "status", "error",
                    "error", errorMessage != null ? errorMessage : "未知错误"
            ));
        } catch (Exception ignored) {}
        try { response.flushBuffer(); } catch (Exception ignored) {}
        try { response.getOutputStream().close(); } catch (Exception ignored) {}
        try { emitter.complete(); } catch (Exception ignored) {}
    }
}
