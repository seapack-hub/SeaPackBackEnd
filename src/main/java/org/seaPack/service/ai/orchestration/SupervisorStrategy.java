package org.seaPack.service.ai.orchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.AgentMessageMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.AgentMessage;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.service.ai.AgentTestChatService;
import org.seaPack.service.ai.TokenStatsService;
import org.seaPack.service.ai.tool.OrchestrationToolContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Supervisor 执行模式（真实工具调用回路）
 * <p>
 * 总控 Agent 通过 Function Calling 动态调度 Worker Agent，注入两个编排级工具：
 * select_agent(agent_id, task) — 派工一个 Worker 执行任务（可多次调用）
 * aggregate_results(answer) — 汇总所有结果，输出最终答案
 * </p>
 * <p>
 * 边界设计：
 * <ul>
 * <li>防递归：Worker 执行不注入编排级工具（toolContext=null），Supervisor 自身不进 Worker 池</li>
 * <li>调度预算：maxRounds 钳制 [1,20]，每次派工消耗 1 次，耗尽后 select_agent 拒绝并提示立即聚合</li>
 * <li>Worker 失败编排继续：失败结果回填给 Supervisor，由其换人重试或直接聚合</li>
 * <li>输出截断：Worker 输出回填 Supervisor 时截断（6000 字符），保护其上下文窗口</li>
 * <li>每次派工记 trace（supervisor_dispatch + children）、Token 落库、SSE 事件携带编排步骤归属</li>
 * <li>最终输出优先级：aggregate_results 的 answer &gt; Supervisor 文本输出 &gt; Worker 输出拼接</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class SupervisorStrategy extends OrchestrationStrategyHandler {

    private static final String TOOL_SELECT_AGENT = "select_agent";
    private static final String TOOL_AGGREGATE = "aggregate_results";
    /** Worker 输出回填 Supervisor 时的最大长度（保护 Supervisor 上下文） */
    private static final int MAX_RESULT_LENGTH = 6000;
    /** 默认调度预算 */
    private static final int DEFAULT_MAX_ROUNDS = 5;
    /** 调度预算钳制范围 */
    private static final int MIN_MAX_ROUNDS = 1;
    private static final int MAX_MAX_ROUNDS = 20;

    private final AgentTestChatService agentTestChatService;
    private final AgentMapper agentMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final TokenStatsService tokenStatsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SupervisorStrategy(AgentTestChatService agentTestChatService,
            AgentMapper agentMapper,
            AgentMessageMapper agentMessageMapper,
            TokenStatsService tokenStatsService) {
        this.agentTestChatService = agentTestChatService;
        this.agentMapper = agentMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.tokenStatsService = tokenStatsService;
    }

    @Override
    public OrchestrationResult execute(ExecuteParams params) {
        return executeSupervisor(params.orchestration, params.steps, params.request,
                params.emitter, params.isCompleted, params.userId, params.authToken);
    }

    /**
     * Supervisor 执行过程的共享状态（供编排级工具回调读写）
     */
    private static class SupervisorContext {
        AgentTestChatService agentTestChatService;
        OrchestrationExecuteRequest request;
        SseEmitter emitter;
        AtomicBoolean isCompleted;
        String authToken;
        Long userId;
        Long supervisorAgentId;
        /** 可调度的 Worker（key=agentId，已排除 Supervisor 自身） */
        Map<Long, Agent> workerMap;
        /** 调度预算（最多派工次数） */
        int maxRounds;
        /** 已派工次数 */
        final AtomicInteger dispatchSeq = new AtomicInteger(0);
        /** aggregate_results 捕获的最终答案 */
        final String[] aggregatedAnswer = new String[1];
        /** Worker 输出拼接（最终输出兜底） */
        final List<String> workerOutputs = new ArrayList<>();
        /** 链路步骤 */
        final List<AgentTraceStep> stepInfos = new ArrayList<>();
        /** 累计 token（[prompt, completion]） */
        final int[] totalTokens = new int[2];
    }

    private OrchestrationResult executeSupervisor(
            SceneOrchestration orchestration,
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            SseEmitter emitter,
            AtomicBoolean isCompleted,
            Long userId,
            String authToken) {

        // 1. 定位 Supervisor Agent（未配置时回退取第一个步骤的 Agent）
        Long supervisorAgentId = orchestration.getSupervisorAgentId();
        if (supervisorAgentId == null) {
            supervisorAgentId = steps.stream()
                    .filter(s -> s.getAgentId() != null)
                    .map(SceneOrchestrationStep::getAgentId)
                    .findFirst()
                    .orElse(null);
        }
        if (supervisorAgentId == null) {
            sendSseError(emitter, "Supervisor 模式需要指定 supervisorAgentId 或至少一个 Agent 步骤");
            return new OrchestrationResult();
        }

        // 2. 收集可用 Worker Agent（防递归：排除 Supervisor 自身；去重）
        Map<Long, Agent> workerMap = new LinkedHashMap<>();
        for (SceneOrchestrationStep s : steps) {
            if (s.getAgentId() != null && !s.getAgentId().equals(supervisorAgentId)) {
                Agent worker = agentMapper.selectById(s.getAgentId());
                if (worker != null && worker.getStatus() != null && worker.getStatus() == 1) {
                    workerMap.put(worker.getId(), worker);
                }
            }
        }
        if (workerMap.isEmpty()) {
            sendSseError(emitter, "Supervisor 模式至少需要 1 个可用的 Worker Agent（需启用状态，且不能是 Supervisor 自身）");
            return new OrchestrationResult();
        }

        // 3. 调度预算：maxRounds 钳制 [1,20]
        int configuredRounds = orchestration.getMaxRounds() != null ? orchestration.getMaxRounds() : DEFAULT_MAX_ROUNDS;
        int maxRounds = Math.max(MIN_MAX_ROUNDS, Math.min(MAX_MAX_ROUNDS, configuredRounds));

        // 4. 组装共享状态
        SupervisorContext ctx = new SupervisorContext();
        ctx.agentTestChatService = agentTestChatService;
        ctx.request = request;
        ctx.emitter = emitter;
        ctx.isCompleted = isCompleted;
        ctx.authToken = authToken;
        ctx.userId = userId;
        ctx.supervisorAgentId = supervisorAgentId;
        ctx.workerMap = workerMap;
        ctx.maxRounds = maxRounds;

        // 5. 启动事件
        sendSseEvent(emitter, SseEvent.TYPE_ORCHESTRATION_START, Map.of(
                "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "Supervisor",
                "strategy", "supervisor",
                "totalSteps", steps.size(),
                "supervisorAgentId", supervisorAgentId,
                "workerCount", workerMap.size(),
                "maxRounds", maxRounds,
                "message", "Supervisor 模式启动，共 " + workerMap.size() + " 个 Worker Agent，调度预算 " + maxRounds + " 次"));

        // 6. 构建编排级工具 + 执行回调
        List<Map<String, Object>> supervisorTools = buildSupervisorTools();
        OrchestrationToolContext toolContext = new OrchestrationToolContext(supervisorTools,
                (toolName, arguments) -> {
                    if (TOOL_SELECT_AGENT.equals(toolName)) {
                        return handleSelectAgent(arguments, ctx);
                    }
                    if (TOOL_AGGREGATE.equals(toolName)) {
                        return handleAggregate(arguments, ctx);
                    }
                    return errJson("未知工具: " + toolName);
                },
                maxRounds + 2);

        // 7. 单次调用 Supervisor（工具回路在 LLM 调用层内完成）
        String supervisorUserMessage = buildSupervisorPrompt(workerMap, maxRounds) + "\n\n当前用户问题："
                + request.getMessage();
        AgentTestChatService.AgentStepResult supervisorResult = agentTestChatService.callAgentStep(
                supervisorAgentId, supervisorUserMessage, request.getHistory(),
                request.getSceneId(), request.getConversationId(), request.getRequestId(),
                emitter, isCompleted, authToken, 0, toolContext);

        // Supervisor 自身的 Token 落库（orchestrationStep=0）
        if (supervisorResult.success || supervisorResult.tokensPrompt > 0 || supervisorResult.tokensCompletion > 0) {
            recordTokenUsage(ctx, supervisorAgentId, 0,
                    supervisorResult.tokensPrompt, supervisorResult.tokensCompletion,
                    supervisorResult.modelName, supervisorResult.durationMs,
                    supervisorResult.success ? "success" : "fail");
        }
        ctx.totalTokens[0] += supervisorResult.tokensPrompt;
        ctx.totalTokens[1] += supervisorResult.tokensCompletion;

        // 记录 Supervisor 消息
        try {
            AgentMessage supervisorMsg = new AgentMessage();
            supervisorMsg.setSessionId(null);
            supervisorMsg.setRoundIndex(ctx.dispatchSeq.get());
            supervisorMsg.setSenderType("supervisor");
            supervisorMsg.setSenderAgentId(supervisorAgentId);
            supervisorMsg.setMessageType("instruction");
            supervisorMsg.setContent(supervisorResult.output);
            agentMessageMapper.insert(supervisorMsg);
        } catch (Exception e) {
            log.warn("保存 Supervisor 消息失败: {}", e.getMessage());
        }

        // 8. 最终输出优先级：aggregate_results 的 answer > Supervisor 文本输出 > Worker 输出拼接
        String finalOutput;
        if (ctx.aggregatedAnswer[0] != null && !ctx.aggregatedAnswer[0].isBlank()) {
            finalOutput = ctx.aggregatedAnswer[0];
        } else if (supervisorResult.success && supervisorResult.output != null && !supervisorResult.output.isBlank()) {
            finalOutput = supervisorResult.output;
        } else if (!ctx.workerOutputs.isEmpty()) {
            finalOutput = String.join("\n\n", ctx.workerOutputs);
            log.info("Supervisor 未聚合且无文本输出，兜底使用 Worker 输出拼接");
        } else {
            finalOutput = supervisorResult.output != null ? supervisorResult.output : "";
        }

        // Supervisor trace 步骤
        AgentTraceStep supervisorTrace = new AgentTraceStep();
        supervisorTrace.setStepIndex(0);
        supervisorTrace.setStepType("supervisor");
        supervisorTrace.setStepName("Supervisor 调度");
        supervisorTrace.setStatus(supervisorResult.success ? "success" : "fail");
        supervisorTrace.setDurationMs(supervisorResult.durationMs);
        supervisorTrace.setInput(request.getMessage());
        supervisorTrace.setOutput(finalOutput);
        Map<String, Object> supervisorMeta = new LinkedHashMap<>();
        supervisorMeta.put("maxRounds", maxRounds);
        supervisorMeta.put("dispatched", ctx.dispatchSeq.get());
        supervisorMeta.put("aggregated", ctx.aggregatedAnswer[0] != null);
        supervisorMeta.put("tokensPrompt", supervisorResult.tokensPrompt);
        supervisorMeta.put("tokensCompletion", supervisorResult.tokensCompletion);
        supervisorMeta.put("model", supervisorResult.modelName);
        if (!supervisorResult.success) {
            supervisorMeta.put("errorMessage", supervisorResult.errorMessage);
        }
        supervisorTrace.setMetadata(supervisorMeta);
        supervisorTrace.setChildren(supervisorResult.steps);
        ctx.stepInfos.add(supervisorTrace);

        if (!supervisorResult.success) {
            sendSseEvent(emitter, SseEvent.TYPE_STEP_ERROR, Map.of(
                    "stepIndex", 0,
                    "errorMessage", "Supervisor 执行失败: " + supervisorResult.errorMessage));
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = finalOutput;
        result.tokensPrompt = ctx.totalTokens[0];
        result.tokensCompletion = ctx.totalTokens[1];
        result.steps = ctx.stepInfos;
        return result;
    }

    // ===== 编排级工具回调 =====

    /**
     * select_agent：派工一个 Worker 执行任务
     * <p>
     * 校验（非法调度/防递归）→ 预算检查 → 执行 Worker（不注入编排级工具）→ 回填结果。
     * Worker 失败不终止编排，错误回填给 Supervisor 决定换人或聚合。
     * </p>
     */
    private String handleSelectAgent(Map<String, Object> args, SupervisorContext ctx) {
        // 1. 参数解析
        Long agentId = parseAgentId(args.get("agent_id"));
        Object taskRaw = args.get("task");
        String task = taskRaw != null ? String.valueOf(taskRaw).trim() : null;
        if (agentId == null || task == null || task.isBlank()) {
            return errJson("select_agent 参数不完整：需要 agent_id（数字）和 task（字符串）");
        }

        // 2. 防递归 + 非法调度校验
        if (agentId.equals(ctx.supervisorAgentId)) {
            return errJson("不能调度 Supervisor 自身（防递归），请从可用 Worker 列表中选择");
        }
        Agent worker = ctx.workerMap.get(agentId);
        if (worker == null) {
            return errJson("Agent " + agentId + " 不是可用的 Worker，可用 agent_id: " + ctx.workerMap.keySet());
        }

        // 3. 取消检查
        if (ctx.isCompleted != null && ctx.isCompleted.get()) {
            return errJson("编排已取消，停止派工");
        }

        // 4. 调度预算检查
        if (ctx.dispatchSeq.get() >= ctx.maxRounds) {
            return errJson("调度预算已耗尽（maxRounds=" + ctx.maxRounds
                    + "），请勿再派工，立即调用 aggregate_results 汇总已有结果输出最终答案");
        }

        int dispatchIndex = ctx.dispatchSeq.incrementAndGet();
        log.info("[Supervisor] 派工 {}/{}: agent={}, task长度={}", dispatchIndex, ctx.maxRounds,
                worker.getName(), task.length());

        sendSseEvent(ctx.emitter, SseEvent.TYPE_STEP_START, SseEvent.withOrchStep(Map.of(
                "stepIndex", dispatchIndex,
                "stepType", "supervisor_dispatch",
                "stepName", "派工: " + worker.getName(),
                "agentId", agentId,
                "dispatchIndex", dispatchIndex,
                "maxRounds", ctx.maxRounds), dispatchIndex));

        // 5. 执行 Worker（toolContext=null 防递归）
        long dispatchStart = System.currentTimeMillis();
        AgentTestChatService.AgentStepResult workerResult = ctx.agentTestChatService.callAgentStep(
                agentId, task, ctx.request.getHistory(),
                ctx.request.getSceneId(), ctx.request.getConversationId(), ctx.request.getRequestId(),
                ctx.emitter, ctx.isCompleted, ctx.authToken, dispatchIndex, null);
        long dispatchDuration = System.currentTimeMillis() - dispatchStart;

        ctx.totalTokens[0] += workerResult.tokensPrompt;
        ctx.totalTokens[1] += workerResult.tokensCompletion;

        // 6. trace：派工步骤（children=Worker 内部步骤）
        AgentTraceStep dispatchTrace = new AgentTraceStep();
        dispatchTrace.setStepIndex(dispatchIndex);
        dispatchTrace.setStepType("supervisor_dispatch");
        dispatchTrace.setStepName("派工: " + worker.getName());
        dispatchTrace.setStatus(workerResult.success ? "success" : "fail");
        dispatchTrace.setDurationMs(dispatchDuration);
        dispatchTrace.setInput(task);
        dispatchTrace.setOutput(workerResult.success ? workerResult.output : ("执行失败: " + workerResult.errorMessage));
        Map<String, Object> dispatchMeta = new LinkedHashMap<>();
        dispatchMeta.put("agentId", agentId);
        dispatchMeta.put("agentName", worker.getName());
        dispatchMeta.put("tokensPrompt", workerResult.tokensPrompt);
        dispatchMeta.put("tokensCompletion", workerResult.tokensCompletion);
        dispatchMeta.put("model", workerResult.modelName);
        dispatchTrace.setMetadata(dispatchMeta);
        dispatchTrace.setChildren(workerResult.steps);
        ctx.stepInfos.add(dispatchTrace);

        // 7. Token 落库
        recordTokenUsage(ctx, agentId, dispatchIndex,
                workerResult.tokensPrompt, workerResult.tokensCompletion,
                workerResult.modelName, workerResult.durationMs,
                workerResult.success ? "success" : "fail");

        // 8. SSE 步骤完成
        Map<String, Object> doneData = new LinkedHashMap<>();
        doneData.put("stepIndex", dispatchIndex);
        doneData.put("status", workerResult.success ? "success" : "fail");
        doneData.put("durationMs", dispatchDuration);
        doneData.put("agentId", agentId);
        doneData.put("agentName", worker.getName());
        doneData.put("tokensPrompt", workerResult.tokensPrompt);
        doneData.put("tokensCompletion", workerResult.tokensCompletion);
        if (workerResult.success) {
            doneData.put("output", workerResult.output);
        } else {
            doneData.put("errorMessage", workerResult.errorMessage);
        }
        sendSseEvent(ctx.emitter, SseEvent.TYPE_STEP_DONE, SseEvent.withOrchStep(doneData, dispatchIndex));

        // 9. 构建回填结果（输出截断保护 Supervisor 上下文）
        Map<String, Object> toolResult = new LinkedHashMap<>();
        toolResult.put("agent", worker.getName());
        toolResult.put("agent_id", agentId);
        toolResult.put("dispatch_index", dispatchIndex);
        toolResult.put("remaining_dispatches", ctx.maxRounds - ctx.dispatchSeq.get());
        if (workerResult.success) {
            String output = workerResult.output != null ? workerResult.output : "";
            boolean truncated = output.length() > MAX_RESULT_LENGTH;
            toolResult.put("success", true);
            toolResult.put("output_truncated", truncated);
            toolResult.put("output", truncated
                    ? output.substring(0, MAX_RESULT_LENGTH) + "...(已截断)"
                    : output);
            ctx.workerOutputs.add("### " + worker.getName() + "\n" + output);
        } else {
            // Worker 失败编排继续：错误回填，Supervisor 换人或直接聚合
            toolResult.put("success", false);
            toolResult.put("error", workerResult.errorMessage);
            toolResult.put("hint", "该 Worker 执行失败，可换其他 Worker 重试，或调用 aggregate_results 汇总已有结果");
        }
        if (ctx.dispatchSeq.get() >= ctx.maxRounds) {
            toolResult.put("budget_exhausted", true);
            toolResult.put("message", "调度预算已耗尽，请立即调用 aggregate_results 输出最终答案");
        }
        return toJson(toolResult);
    }

    /**
     * aggregate_results：捕获最终答案（作为编排最终输出）
     */
    private String handleAggregate(Map<String, Object> args, SupervisorContext ctx) {
        Object answerRaw = args.get("answer");
        String answer = answerRaw != null ? String.valueOf(answerRaw) : null;
        if (answer == null || answer.isBlank()) {
            return errJson("aggregate_results 参数不完整：需要 answer（最终汇总答案）");
        }
        ctx.aggregatedAnswer[0] = answer;
        log.info("[Supervisor] aggregate_results 捕获最终答案: 长度={}", answer.length());
        Map<String, Object> toolResult = new LinkedHashMap<>();
        toolResult.put("success", true);
        toolResult.put("message", "已收到最终答案，编排结束");
        return toJson(toolResult);
    }

    // ===== 工具与提示词构建 =====

    /**
     * 构建编排级工具定义（select_agent + aggregate_results，OpenAI tools 格式）
     */
    private List<Map<String, Object>> buildSupervisorTools() {
        List<Map<String, Object>> supervisorTools = new ArrayList<>();

        // select_agent 工具
        Map<String, Object> selectAgentTool = new HashMap<>();
        selectAgentTool.put("type", "function");
        Map<String, Object> selectAgentFn = new HashMap<>();
        selectAgentFn.put("name", TOOL_SELECT_AGENT);
        selectAgentFn.put("description", "选择一个 Worker Agent 来执行特定任务，可多次调用（每次派工一个 Worker）");
        Map<String, Object> selectAgentParams = new HashMap<>();
        selectAgentParams.put("type", "object");
        Map<String, Object> selectAgentProps = new HashMap<>();
        selectAgentProps.put("agent_id", Map.of("type", "number", "description", "Worker Agent 的 ID"));
        selectAgentProps.put("task", Map.of("type", "string", "description", "分配给该 Agent 的任务描述"));
        selectAgentParams.put("properties", selectAgentProps);
        selectAgentParams.put("required", List.of("agent_id", "task"));
        selectAgentFn.put("parameters", selectAgentParams);
        selectAgentTool.put("function", selectAgentFn);
        supervisorTools.add(selectAgentTool);

        // aggregate_results 工具
        Map<String, Object> aggregateTool = new HashMap<>();
        aggregateTool.put("type", "function");
        Map<String, Object> aggregateFn = new HashMap<>();
        aggregateFn.put("name", TOOL_AGGREGATE);
        aggregateFn.put("description", "汇总所有 Worker Agent 的结果，输出最终答案给用户（必须调用一次）");
        Map<String, Object> aggregateParams = new HashMap<>();
        aggregateParams.put("type", "object");
        Map<String, Object> aggregateProps = new HashMap<>();
        aggregateProps.put("answer", Map.of("type", "string", "description", "最终的汇总答案"));
        aggregateParams.put("properties", aggregateProps);
        aggregateParams.put("required", List.of("answer"));
        aggregateFn.put("parameters", aggregateParams);
        aggregateTool.put("function", aggregateFn);
        supervisorTools.add(aggregateTool);

        return supervisorTools;
    }

    /**
     * 构建 Supervisor 的调度指令（Worker 列表 + 调度规则）
     */
    private String buildSupervisorPrompt(Map<Long, Agent> workerMap, int maxRounds) {
        StringBuilder prompt = new StringBuilder("## 可用 Worker Agent 列表\n\n");
        for (Agent w : workerMap.values()) {
            prompt.append("- **").append(w.getName()).append("** (ID: ").append(w.getId()).append(")\n");
            if (w.getDescription() != null && !w.getDescription().isBlank()) {
                prompt.append("  描述: ").append(w.getDescription()).append("\n");
            }
        }
        prompt.append("\n## 调度规则\n");
        prompt.append("1. 分析用户问题，拆解为可交给 Worker 的子任务\n");
        prompt.append("2. 使用 select_agent 工具派工 Worker 执行子任务（每次一个 Worker，可多次调用，共 ").append(maxRounds)
                .append(" 次预算）\n");
        prompt.append("3. 只能从上方列表中选择 agent_id，不能调度自己\n");
        prompt.append("4. 收到足够结果（或预算耗尽）后，必须调用 aggregate_results 工具输出最终答案\n");
        prompt.append("5. Worker 失败时可换其他 Worker 重试，或直接聚合已有结果\n");
        return prompt.toString();
    }

    // ===== 辅助方法 =====

    /**
     * Token 落库（bizType=orchestration；Supervisor 用 step=0，派工用 dispatchIndex）
     */
    private void recordTokenUsage(SupervisorContext ctx, Long agentId, int orchestrationStep,
            int tokensPrompt, int tokensCompletion, String modelName, long durationMs, String status) {
        try {
            TokenUsageLog tokenLog = new TokenUsageLog();
            tokenLog.setCallTime(new Date());
            tokenLog.setModelName(modelName);
            tokenLog.setTokensInput(tokensPrompt);
            tokenLog.setTokensOutput(tokensCompletion);
            tokenLog.setDurationMs((int) durationMs);
            tokenLog.setStatus(status);
            tokenLog.setUserId(ctx.userId);
            tokenLog.setBizType("orchestration");
            tokenLog.setSceneId(ctx.request.getSceneId());
            tokenLog.setAgentId(agentId);
            tokenLog.setOrchestrationStep(orchestrationStep);
            tokenLog.setRequestId(ctx.request.getRequestId());
            tokenStatsService.recordCall(tokenLog);
        } catch (Exception e) {
            log.error("记录 Token 统计失败: agentId={}, step={}, {}", agentId, orchestrationStep, e.getMessage(), e);
        }
    }

    /**
     * 解析工具参数中的 agent_id（LLM 可能返回数字或字符串）
     */
    private Long parseAgentId(Object raw) {
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        if (raw instanceof String) {
            try {
                return Long.valueOf(((String) raw).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String errJson(String message) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("error", message);
        return toJson(err);
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("工具结果序列化失败: {}", e.getMessage());
            return "{\"success\": false, \"error\": \"工具结果序列化失败\"}";
        }
    }
}
