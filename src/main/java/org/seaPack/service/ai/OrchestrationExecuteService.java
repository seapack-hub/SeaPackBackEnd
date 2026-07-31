package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.mapper.ai.ExecutionSessionMapper;
import org.seaPack.model.ai.ExecutionSession;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.SceneMapper;
import org.seaPack.mapper.ai.SceneOrchestrationMapper;
import org.seaPack.mapper.ai.SceneOrchestrationStepMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.Scene;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 编排执行服务
 * <p>负责解析编排策略并按步骤依次/并行执行 Agent，
 * 通过 SSE 流式输出每步的进度、内容和最终结果。
 * 直接调用 LLM API（流式），不复用 AgentTestChatService。</p>
 */
@Slf4j
@Service
public class OrchestrationExecuteService {

    @Autowired
    private SceneOrchestrationMapper orchestrationMapper;

    @Autowired
    private SceneOrchestrationStepMapper stepMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private SceneMapper sceneMapper;

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private LlmSseHelper llmSseHelper;

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== 主入口 =====

    /**
     * 执行编排（SSE 流式输出）
     *
     * @param request 执行请求（orchestrationId, message, history）
     * @param emitter SSE 发射器
     */
    public void execute(OrchestrationExecuteRequest request, Long userId, SseEmitter emitter) {
        long totalStart = System.currentTimeMillis();
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // 注册完成回调：客户端断连时标记中断
        emitter.onCompletion(() -> {
            log.info("编排 SSE 连接已关闭，设置中断标记");
            isCompleted.set(true);
        });
        emitter.onTimeout(() -> {
            log.warn("编排 SSE 连接超时");
            isCompleted.set(true);
        });

        try {
            // 1. 加载编排：先尝试编排ID，再尝试场景ID
            SceneOrchestration orchestration = orchestrationMapper.selectById(request.getOrchestrationId());
            if (orchestration == null) {
                // 不是编排ID，尝试作为场景ID查询
                Scene scene = sceneMapper.selectById(request.getOrchestrationId());
                if (scene == null) {
                    sendSseError(emitter, "未找到编排或场景: " + request.getOrchestrationId());
                    return;
                }
                // 查找该场景下第一个启用的编排（按 sort_order 升序）
                List<SceneOrchestration> sceneOrchestrations = orchestrationMapper.selectBySceneId(scene.getId());
                orchestration = sceneOrchestrations.stream()
                        .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                        .findFirst()
                        .orElse(null);
                if (orchestration == null) {
                    sendSseError(emitter, "场景 [" + scene.getName() + "] 下没有启用的编排");
                    return;
                }
                log.info("通过场景ID [{}] 找到编排 [{}]", scene.getId(), orchestration.getName());
            }
            if (orchestration.getStatus() == null || orchestration.getStatus() != 1) {
                sendSseError(emitter, "编排已禁用: " + orchestration.getName());
                return;
            }

            // 2. 加载步骤（按 step_index 升序）
            List<SceneOrchestrationStep> steps = stepMapper.selectByOrchestrationId(orchestration.getId());
            if (steps == null || steps.isEmpty()) {
                sendSseError(emitter, "编排没有定义任何步骤: " + orchestration.getName());
                return;
            }

            // 3. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            // 4. 按策略执行
            String strategy = orchestration.getStrategy() != null ? orchestration.getStrategy() : "sequential";

            // 4a. 发送编排启动事件：告知前端编排的基本信息和执行计划
            sendSseEvent(emitter, "orchestration_start", Map.of(
                    "orchestrationId", orchestration.getId(),
                    "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "",
                    "strategy", strategy,
                    "totalSteps", steps.size(),
                    "provider", providerName,
                    "chatModel", config.getChatModel() != null ? config.getChatModel() : "",
                    "message", "编排 [" + orchestration.getName() + "] 开始执行，策略: " + strategy + "，共 " + steps.size() + " 个步骤"
            ));

            String mergedResult;
            int totalTokensPrompt = 0;
            int totalTokensCompletion = 0;
            List<AgentTraceStep> stepInfos = new ArrayList<>();

            switch (strategy) {
                case "parallel":
                    OrchestrationResult parallelResult = executeParallel(steps, request, config, emitter, isCompleted);
                    mergedResult = parallelResult.output;
                    totalTokensPrompt = parallelResult.tokensPrompt;
                    totalTokensCompletion = parallelResult.tokensCompletion;
                    stepInfos = parallelResult.steps != null ? parallelResult.steps : new ArrayList<>();
                    break;
                default:
                    // sequential（含 auto 单步骤时退化）
                    OrchestrationResult sequentialResult = executeSequential(steps, request, config, emitter, isCompleted);
                    mergedResult = sequentialResult.output;
                    totalTokensPrompt = sequentialResult.tokensPrompt;
                    totalTokensCompletion = sequentialResult.tokensCompletion;
                    stepInfos = sequentialResult.steps != null ? sequentialResult.steps : new ArrayList<>();
                    break;
            }

            if (isCompleted.get()) {
                log.info("编排执行被中断，跳过 done 事件");
                return;
            }

            // 5. 发送完成事件
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", mergedResult);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("strategy", strategy);
            doneData.put("totalSteps", steps.size());
            doneData.put("tokens", Map.of(
                    "prompt", totalTokensPrompt,
                    "completion", totalTokensCompletion
            ));
            doneData.put("message", "编排执行完成，共耗时 " + totalDuration + "ms");
            sendSseEvent(emitter, "done", doneData);

            // 6. 保存执行会话（用于刷新后链路追踪历史查询）
            try {
                saveSession(request, orchestration, mergedResult, totalDuration,
                        totalTokensPrompt, totalTokensCompletion,
                        config.getChatModel(), "success", null, userId, stepInfos, strategy);
            } catch (Exception ex) {
                log.warn("保存编排执行会话失败: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("编排执行异常", e);
            sendSseError(emitter, "编排执行失败: " + e.getMessage());
        } finally {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    // ===== 动态编排执行（LLM 路由构建的步骤） =====

    /**
     * 执行动态构建的编排步骤（不查数据库，直接执行传入的步骤列表）
     * <p>用于 LLM 动态选择 Agent 后的多 Agent 协作场景。</p>
     *
     * @param steps     动态构建的步骤列表（stepIndex 从 1 开始）
     * @param strategy  执行策略：sequential / parallel
     * @param message   用户输入消息
     * @param history   对话历史
     * @param emitter   SSE 发射器
     */
    public void executeDynamic(List<SceneOrchestrationStep> steps, String strategy,
                               String message, List<Map<String, String>> history,
                               Long sceneId, String conversationId, String requestId,
                               Long userId, SseEmitter emitter) {
        long totalStart = System.currentTimeMillis();
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        emitter.onCompletion(() -> {
            log.info("动态编排 SSE 连接已关闭");
            isCompleted.set(true);
        });
        emitter.onTimeout(() -> {
            log.warn("动态编排 SSE 连接超时");
            isCompleted.set(true);
        });

        try {
            // 1. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            // 2. 发送编排启动事件
            sendSseEvent(emitter, "orchestration_start", Map.of(
                    "orchestrationName", "动态编排",
                    "strategy", strategy != null ? strategy : "sequential",
                    "totalSteps", steps.size(),
                    "provider", providerName,
                    "chatModel", config.getChatModel() != null ? config.getChatModel() : "",
                    "message", "LLM 选择了 " + steps.size() + " 个 Agent，策略: " + (strategy != null ? strategy : "sequential")
            ));

            // 3. 构造请求对象
            OrchestrationExecuteRequest request = new OrchestrationExecuteRequest();
            request.setMessage(message);
            request.setHistory(history);
            request.setSceneId(sceneId);
            request.setConversationId(conversationId);
            request.setRequestId(requestId);

            // 4. 按策略执行
            String execStrategy = strategy != null ? strategy : "sequential";
            OrchestrationResult result;

            if ("parallel".equals(execStrategy)) {
                result = executeParallel(steps, request, config, emitter, isCompleted);
            } else {
                result = executeSequential(steps, request, config, emitter, isCompleted);
            }

            if (isCompleted.get()) {
                log.info("动态编排执行被中断");
                return;
            }

            // 5. 发送完成事件
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", result.output);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("strategy", execStrategy);
            doneData.put("totalSteps", steps.size());
            doneData.put("tokens", Map.of(
                    "prompt", result.tokensPrompt,
                    "completion", result.tokensCompletion
            ));
            doneData.put("message", "动态编排执行完成，共耗时 " + totalDuration + "ms");
            sendSseEvent(emitter, "done", doneData);

            // 6. 保存动态编排执行会话（用于刷新后链路追踪历史查询）
            try {
                saveDynamicSession(request, result.output, totalDuration,
                        result.tokensPrompt, result.tokensCompletion,
                        config.getChatModel(), "success", null, userId, result.steps, execStrategy);
            } catch (Exception ex) {
                log.warn("保存动态编排执行会话失败: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("动态编排执行异常", e);
            sendSseError(emitter, "动态编排执行失败: " + e.getMessage());
        } finally {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    // ===== 顺序执行 =====

    /**
     * 顺序执行：按步骤索引依次执行，上一步的输出可作为下一步的输入映射源
     */
    private OrchestrationResult executeSequential(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            AIProperties.ProviderConfig config,
            SseEmitter emitter,
            AtomicBoolean isCompleted) {

        StringBuilder overallOutput = new StringBuilder();
        // 缓存每步输出 key: stepIndex, value: output
        Map<Integer, String> stepOutputs = new HashMap<>();
        // 缓存每步状态
        Map<Integer, String> stepStatuses = new HashMap<>();
        // 步骤链路信息
        List<AgentTraceStep> stepInfos = new ArrayList<>();
        int totalPrompt = 0;
        int totalCompletion = 0;

        for (SceneOrchestrationStep step : steps) {
            if (isCompleted.get()) break;

            // 跳过已禁用的步骤
            if (step.getStatus() != null && step.getStatus() != 1) {
                continue;
            }

            long stepStart = System.currentTimeMillis();
            int stepIdx = step.getStepIndex();

            // 4a. 发送 step_start：告知前端步骤开始
            sendSseEvent(emitter, "step_start", Map.of(
                    "stepIndex", stepIdx,
                    "stepType", "llm",
                    "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)
            ));

            AgentTraceStep traceStep = new AgentTraceStep();
            traceStep.setStepIndex(stepIdx);
            traceStep.setStepType("llm_call");
            traceStep.setStepName(step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx));
            traceStep.setStatus("running");
            stepInfos.add(traceStep);

            try {
                // 4b. 条件评估（发送详细评估过程）
                if (step.getCondition() != null && !step.getCondition().isBlank()) {
                    // 替换占位符，展示实际评估值
                    String resolvedCondition = step.getCondition();
                    for (Map.Entry<Integer, String> entry : stepStatuses.entrySet()) {
                        resolvedCondition = resolvedCondition.replace(
                                "${step_" + entry.getKey() + ".status}",
                                entry.getValue() != null ? entry.getValue() : "未执行"
                        );
                    }
                    boolean conditionMet = evaluateCondition(step.getCondition(), stepOutputs, stepStatuses);

                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "phase", "condition_eval",
                            "rawCondition", step.getCondition(),
                            "resolvedCondition", resolvedCondition,
                            "result", conditionMet ? "pass" : "skip",
                            "message", conditionMet
                                    ? "条件满足，继续执行"
                                    : "条件不满足，跳过本步骤"
                    ));

                    if (!conditionMet) {
                        sendSseEvent(emitter, "step_done", Map.of(
                                "stepIndex", stepIdx,
                                "stepName", step.getStepName(),
                                "status", "skip",
                                "message", "条件不满足: " + step.getCondition(),
                                "durationMs", 0
                        ));
                        stepStatuses.put(stepIdx, "skip");
                        traceStep.setStatus("skip");
                        traceStep.setDurationMs(0L);
                        traceStep.setOutput("条件不满足，跳过本步骤");
                        continue;
                    }
                }

                // 4c. 加载 Agent（发送详细 Agent 信息）
                Agent agent = agentMapper.selectById(step.getAgentId());
                if (agent == null) {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "errorMessage", "Agent 不存在: " + step.getAgentId()
                    ));
                    stepStatuses.put(stepIdx, "fail");
                    traceStep.setStatus("fail");
                    traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                    traceStep.setOutput("Agent 不存在: " + step.getAgentId());
                    continue;
                }
                if (agent.getStatus() == null || agent.getStatus() != 1) {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "errorMessage", "Agent 已禁用: " + agent.getName()
                    ));
                    stepStatuses.put(stepIdx, "fail");
                    traceStep.setStatus("fail");
                    traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                    traceStep.setOutput("Agent 已禁用: " + agent.getName());
                    continue;
                }

                Map<String, Object> agentDetail = new HashMap<>();
                agentDetail.put("stepIndex", stepIdx);
                agentDetail.put("stepName", step.getStepName());
                agentDetail.put("phase", "agent_loaded");
                agentDetail.put("agentId", agent.getId());
                agentDetail.put("agentName", agent.getName() != null ? agent.getName() : "");
                agentDetail.put("agentCode", agent.getCode() != null ? agent.getCode() : "");
                agentDetail.put("model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel());
                agentDetail.put("systemPromptLength", agent.getSystemPrompt() != null ? agent.getSystemPrompt().length() : 0);
                agentDetail.put("temperature", agent.getTemperature() != null ? agent.getTemperature() : 1.0);
                agentDetail.put("maxTokens", agent.getMaxTokens() != null ? agent.getMaxTokens() : 0);
                agentDetail.put("memoryWindow", agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20);
                agentDetail.put("message", "Agent [" + agent.getName() + "] 加载完成");
                sendSseEvent(emitter, "step_detail", agentDetail);

                // 4d. 解析输入映射（发送解析详情）
                String stepInput = resolveInputMapping(step.getInputMapping(), stepOutputs, request.getMessage());

                sendSseEvent(emitter, "step_detail", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "phase", "input_resolved",
                        "rawTemplate", step.getInputMapping() != null ? step.getInputMapping() : "(空，使用用户原始输入)",
                        "resolvedInput", stepInput.length() > 500
                                ? stepInput.substring(0, 500) + "...(" + stepInput.length() + "字符)"
                                : stepInput,
                        "inputLength", stepInput.length(),
                        "message", "输入映射解析完成"
                ));

                // 4e. 调用 LLM 流式输出（发送调用准备信息）
                sendSseEvent(emitter, "step_detail", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "phase", "llm_calling",
                        "model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel(),
                        "inputLength", stepInput.length(),
                        "historyCount", request.getHistory() != null ? request.getHistory().size() : 0,
                        "systemPromptLength", agent.getSystemPrompt() != null ? agent.getSystemPrompt().length() : 0,
                        "message", "正在调用 LLM: " + (agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel())
                ));

                StepLlmResult llmResult = callLlmStream(agent, stepInput, request.getHistory(),
                        config, emitter, stepIdx, isCompleted);

                if (isCompleted.get()) break;

                // 4f. 保存输出
                stepOutputs.put(stepIdx, llmResult.output);
                stepStatuses.put(stepIdx, "success");
                totalPrompt += llmResult.tokensPrompt;
                totalCompletion += llmResult.tokensCompletion;

                // 追加到总输出
                if (overallOutput.length() > 0 && !llmResult.output.isEmpty()) {
                    overallOutput.append("\n\n");
                }
                overallOutput.append(llmResult.output);

                long stepDuration = System.currentTimeMillis() - stepStart;

                // 发送 LLM 调用完成详情
                sendSseEvent(emitter, "step_detail", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "phase", "llm_done",
                        "model", llmResult.modelName,
                        "tokensPrompt", llmResult.tokensPrompt,
                        "tokensCompletion", llmResult.tokensCompletion,
                        "outputLength", llmResult.output.length(),
                        "durationMs", llmResult.durationMs,
                        "message", "LLM 调用完成，耗时 " + llmResult.durationMs + "ms"
                ));

                // 发送 step_done（含完整输出）
                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "status", "success",
                        "durationMs", stepDuration,
                        "output", llmResult.output,
                        "tokensPrompt", llmResult.tokensPrompt,
                        "tokensCompletion", llmResult.tokensCompletion,
                        "model", llmResult.modelName
                ));

                traceStep.setStatus("success");
                traceStep.setDurationMs(stepDuration);
                traceStep.setInput(stepInput);
                traceStep.setOutput(llmResult.output);
                Map<String, Object> stepMeta = new HashMap<>();
                stepMeta.put("tokensPrompt", llmResult.tokensPrompt);
                stepMeta.put("tokensCompletion", llmResult.tokensCompletion);
                stepMeta.put("model", llmResult.modelName);
                stepMeta.put("agentId", agent.getId());
                stepMeta.put("agentName", agent.getName() != null ? agent.getName() : "");
                traceStep.setMetadata(stepMeta);

            } catch (Exception e) {
                log.warn("步骤[{}]执行异常: {}", step.getStepName(), e.getMessage());
                stepStatuses.put(stepIdx, "fail");

                // 重试逻辑
                if (step.getRetryCount() != null && step.getRetryCount() > 0) {
                    boolean retried = false;
                    for (int i = 0; i < step.getRetryCount(); i++) {
                        if (isCompleted.get()) break;
                        log.info("步骤[{}] 第{}次重试", step.getStepName(), i + 1);

                        sendSseEvent(emitter, "step_detail", Map.of(
                                "stepIndex", stepIdx,
                                "stepName", step.getStepName(),
                                "phase", "retry",
                                "retryIndex", i + 1,
                                "maxRetry", step.getRetryCount(),
                                "message", "第" + (i + 1) + "次重试（共" + step.getRetryCount() + "次）"
                        ));

                        try {
                            Agent agent = agentMapper.selectById(step.getAgentId());
                            if (agent == null) continue;

                            sendSseEvent(emitter, "step_progress", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "message", "第" + (i + 1) + "次重试..."
                            ));

                            // 发送重试的 Agent 加载详情
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "phase", "agent_loaded",
                                    "agentId", agent.getId(),
                                    "agentName", agent.getName() != null ? agent.getName() : "",
                                    "model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel(),
                                    "systemPromptLength", agent.getSystemPrompt() != null ? agent.getSystemPrompt().length() : 0,
                                    "message", "Agent [" + agent.getName() + "] 重新加载完成"
                            ));

                            String stepInput = resolveInputMapping(step.getInputMapping(), stepOutputs, request.getMessage());

                            // 发送输入映射解析详情
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "phase", "input_resolved",
                                    "rawTemplate", step.getInputMapping() != null ? step.getInputMapping() : "(空，使用用户原始输入)",
                                    "resolvedInput", stepInput.length() > 500
                                            ? stepInput.substring(0, 500) + "...(" + stepInput.length() + "字符)"
                                            : stepInput,
                                    "inputLength", stepInput.length(),
                                    "message", "输入映射解析完成"
                            ));

                            // 发送 LLM 调用准备
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "phase", "llm_calling",
                                    "model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel(),
                                    "inputLength", stepInput.length(),
                                    "historyCount", request.getHistory() != null ? request.getHistory().size() : 0,
                                    "message", "正在调用 LLM: " + (agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel())
                            ));

                            StepLlmResult llmResult = callLlmStream(agent, stepInput, request.getHistory(),
                                    config, emitter, stepIdx, isCompleted);

                            stepOutputs.put(stepIdx, llmResult.output);
                            stepStatuses.put(stepIdx, "success");
                            totalPrompt += llmResult.tokensPrompt;
                            totalCompletion += llmResult.tokensCompletion;

                            if (overallOutput.length() > 0 && !llmResult.output.isEmpty()) {
                                overallOutput.append("\n\n");
                            }
                            overallOutput.append(llmResult.output);

                            long stepDuration = System.currentTimeMillis() - stepStart;

                            // 发送重试 LLM 完成详情
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "phase", "llm_done",
                                    "model", llmResult.modelName,
                                    "tokensPrompt", llmResult.tokensPrompt,
                                    "tokensCompletion", llmResult.tokensCompletion,
                                    "outputLength", llmResult.output.length(),
                                    "durationMs", llmResult.durationMs,
                                    "message", "重试 LLM 调用完成，耗时 " + llmResult.durationMs + "ms"
                            ));

                            sendSseEvent(emitter, "step_done", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "status", "success",
                                    "durationMs", stepDuration,
                                    "output", llmResult.output,
                                    "tokensPrompt", llmResult.tokensPrompt,
                                    "tokensCompletion", llmResult.tokensCompletion,
                                    "model", llmResult.modelName
                            ));
                            traceStep.setStatus("success");
                            traceStep.setDurationMs(stepDuration);
                            traceStep.setOutput(llmResult.output);
                            retried = true;
                            break;
                        } catch (Exception retryEx) {
                            log.warn("步骤[{}] 第{}次重试失败: {}", step.getStepName(), i + 1, retryEx.getMessage());

                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "phase", "retry_failed",
                                    "retryIndex", i + 1,
                                    "errorMessage", retryEx.getMessage(),
                                    "message", "第" + (i + 1) + "次重试失败: " + retryEx.getMessage()
                            ));
                        }
                    }
                    if (retried) continue;
                }

                sendSseEvent(emitter, "step_error", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "errorMessage", e.getMessage()
                ));
                traceStep.setStatus("fail");
                traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                traceStep.setOutput("执行异常: " + e.getMessage());
            }
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt;
        result.tokensCompletion = totalCompletion;
        result.steps = stepInfos;
        return result;
    }

    // ===== 并行执行 =====

    /**
     * 并行执行：所有步骤同时调用各自的 Agent，最终合并输出。
     * 注意：并行模式下 input_mapping 不能引用其他步骤的输出（因为同时执行）。
     */
    private OrchestrationResult executeParallel(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            AIProperties.ProviderConfig config,
            SseEmitter emitter,
            AtomicBoolean isCompleted) {

        StringBuilder overallOutput = new StringBuilder();
        int[] totalPrompt = {0};
        int[] totalCompletion = {0};
        // 步骤链路信息（并发收集，结束后按 stepIndex 排序）
        List<AgentTraceStep> stepInfos = Collections.synchronizedList(new ArrayList<>());
        // 按步骤索引排序的并行结果
        int stepCount = steps.size();
        String[] orderedOutputs = new String[stepCount];

        // 使用 CompletableFuture 并发执行所有步骤
        @SuppressWarnings("unchecked")
        java.util.concurrent.CompletableFuture<Void>[] futures = new java.util.concurrent.CompletableFuture[stepCount];

        for (int i = 0; i < stepCount; i++) {
            SceneOrchestrationStep step = steps.get(i);
            int index = i;
            int stepIdx = step.getStepIndex();

            futures[i] = java.util.concurrent.CompletableFuture.runAsync(() -> {
                if (isCompleted.get()) return;

                long stepStart = System.currentTimeMillis();
                sendSseEvent(emitter, "step_start", Map.of(
                        "stepIndex", stepIdx,
                        "stepType", "llm",
                        "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)
                ));

                AgentTraceStep traceStep = new AgentTraceStep();
                traceStep.setStepIndex(stepIdx);
                traceStep.setStepType("llm_call");
                traceStep.setStepName(step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx));
                traceStep.setStatus("running");
                stepInfos.add(traceStep);

                try {
                    if (step.getStatus() != null && step.getStatus() != 1) {
                        orderedOutputs[index] = "";
                        sendSseEvent(emitter, "step_detail", Map.of(
                                "stepIndex", stepIdx,
                                "stepName", step.getStepName(),
                                "phase", "skipped",
                                "message", "步骤已禁用，跳过执行"
                        ));
                        traceStep.setStatus("skip");
                        traceStep.setDurationMs(0L);
                        traceStep.setOutput("步骤已禁用，跳过执行");
                        return;
                    }

                    Agent agent = agentMapper.selectById(step.getAgentId());
                    if (agent == null || agent.getStatus() == null || agent.getStatus() != 1) {
                        orderedOutputs[index] = "";
                        sendSseEvent(emitter, "step_error", Map.of(
                                "stepIndex", stepIdx,
                                "stepName", step.getStepName(),
                                "errorMessage", "Agent 不可用: " + (agent != null ? agent.getName() : step.getAgentId())
                        ));
                        traceStep.setStatus("fail");
                        traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                        traceStep.setOutput("Agent 不可用: " + (agent != null ? agent.getName() : step.getAgentId()));
                        return;
                    }

                    // 发送 Agent 加载详情
                    Map<String, Object> agentDetail = new HashMap<>();
                    agentDetail.put("stepIndex", stepIdx);
                    agentDetail.put("stepName", step.getStepName());
                    agentDetail.put("phase", "agent_loaded");
                    agentDetail.put("agentId", agent.getId());
                    agentDetail.put("agentName", agent.getName() != null ? agent.getName() : "");
                    agentDetail.put("agentCode", agent.getCode() != null ? agent.getCode() : "");
                    agentDetail.put("model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel());
                    agentDetail.put("systemPromptLength", agent.getSystemPrompt() != null ? agent.getSystemPrompt().length() : 0);
                    agentDetail.put("temperature", agent.getTemperature() != null ? agent.getTemperature() : 1.0);
                    agentDetail.put("maxTokens", agent.getMaxTokens() != null ? agent.getMaxTokens() : 0);
                    agentDetail.put("message", "Agent [" + agent.getName() + "] 加载完成");
                    sendSseEvent(emitter, "step_detail", agentDetail);

                    String stepInput = resolveInputMapping(step.getInputMapping(), new HashMap<>(), request.getMessage());

                    // 发送输入映射解析详情
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "phase", "input_resolved",
                            "rawTemplate", step.getInputMapping() != null ? step.getInputMapping() : "(空，使用用户原始输入)",
                            "resolvedInput", stepInput.length() > 500
                                    ? stepInput.substring(0, 500) + "...(" + stepInput.length() + "字符)"
                                    : stepInput,
                            "inputLength", stepInput.length(),
                            "message", "输入映射解析完成"
                    ));

                    // 发送 LLM 调用准备
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "phase", "llm_calling",
                            "model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel(),
                            "inputLength", stepInput.length(),
                            "historyCount", request.getHistory() != null ? request.getHistory().size() : 0,
                            "message", "正在调用 LLM: " + (agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel())
                    ));

                    StepLlmResult llmResult = callLlmStream(agent, stepInput, request.getHistory(),
                            config, emitter, stepIdx, isCompleted);

                    orderedOutputs[index] = llmResult.output;
                    totalPrompt[0] += llmResult.tokensPrompt;
                    totalCompletion[0] += llmResult.tokensCompletion;

                    long stepDuration = System.currentTimeMillis() - stepStart;

                    // 发送 LLM 完成详情
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "phase", "llm_done",
                            "model", llmResult.modelName,
                            "tokensPrompt", llmResult.tokensPrompt,
                            "tokensCompletion", llmResult.tokensCompletion,
                            "outputLength", llmResult.output.length(),
                            "durationMs", llmResult.durationMs,
                            "message", "LLM 调用完成，耗时 " + llmResult.durationMs + "ms"
                    ));

                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "status", "success",
                            "durationMs", stepDuration,
                            "output", llmResult.output,
                            "tokensPrompt", llmResult.tokensPrompt,
                            "tokensCompletion", llmResult.tokensCompletion,
                            "model", llmResult.modelName
                    ));
                    traceStep.setStatus("success");
                    traceStep.setDurationMs(stepDuration);
                    traceStep.setInput(stepInput);
                    traceStep.setOutput(llmResult.output);
                    Map<String, Object> stepMeta = new HashMap<>();
                    stepMeta.put("tokensPrompt", llmResult.tokensPrompt);
                    stepMeta.put("tokensCompletion", llmResult.tokensCompletion);
                    stepMeta.put("model", llmResult.modelName);
                    stepMeta.put("agentId", agent.getId());
                    stepMeta.put("agentName", agent.getName() != null ? agent.getName() : "");
                    traceStep.setMetadata(stepMeta);
                } catch (Exception e) {
                    orderedOutputs[index] = "";
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "errorMessage", e.getMessage()
                    ));
                    traceStep.setStatus("fail");
                    traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                    traceStep.setOutput("执行异常: " + e.getMessage());
                }
            });
        }

        // 等待所有步骤完成
        try {
            java.util.concurrent.CompletableFuture.allOf(futures).get();
        } catch (Exception e) {
            log.warn("并行执行等待中断: {}", e.getMessage());
        }

        // 按顺序合并输出
        for (int i = 0; i < stepCount; i++) {
            if (orderedOutputs[i] != null && !orderedOutputs[i].isEmpty()) {
                if (overallOutput.length() > 0) {
                    overallOutput.append("\n\n");
                }
                overallOutput.append(orderedOutputs[i]);
            }
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt[0];
        result.tokensCompletion = totalCompletion[0];
        // 按 stepIndex 排序
        stepInfos.sort(Comparator.comparing(s -> s.getStepIndex() == null ? 0 : s.getStepIndex()));
        result.steps = stepInfos;
        return result;
    }

    // ===== LLM 流式调用 =====

    /**
     * 流式调用 LLM API，逐 token 发送 content 事件
     *
     * @param agent       Agent 实体（含 systemPrompt、modelCode、temperature 等）
     * @param userMessage 当前输入（经过 input_mapping 解析后）
     * @param history     历史消息列表
     * @param config      AI 提供商配置
     * @param emitter     SSE 发射器
     * @param stepIndex   当前步骤索引（用于事件）
     * @param isCompleted 中断标记
     * @return 完整输出和 token 统计
     */
    private StepLlmResult callLlmStream(
            Agent agent,
            String userMessage,
            List<Map<String, String>> history,
            AIProperties.ProviderConfig config,
            SseEmitter emitter,
            int stepIndex,
            AtomicBoolean isCompleted) {

        long llmStart = System.currentTimeMillis();
        String modelName = agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel();

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // system prompt
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", agent.getSystemPrompt());
            messages.add(systemMsg);
        }

        // 历史消息（受限窗口）
        if (history != null && !history.isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> trimmedHistory = history;
            if (history.size() > window * 2) {
                trimmedHistory = history.subList(history.size() - window * 2, history.size());
            }
            messages.addAll(trimmedHistory);
        }

        // 用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        // 构建请求
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        if (agent.getTemperature() != null) {
            requestBody.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            requestBody.put("max_tokens", agent.getMaxTokens());
        }

        StringBuilder outputBuilder = new StringBuilder();
        int[] tokenUsage = {0, 0}; // [prompt, completion]

        try {
            // 使用 LlmSseHelper 实现流式请求（统一 SSE 读取逻辑）
            java.net.HttpURLConnection connection = llmSseHelper.createConnection(url, config.getApiKey(), requestBody);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000); // 5 分钟读取超时

            llmSseHelper.readChunks(connection, isCompleted, chunk -> {
                if (chunk.isDone()) return;
                if (chunk.hasDeltaContent()) {
                    outputBuilder.append(chunk.getDeltaContent());
                    sendSseEvent(emitter, "content", java.util.Map.of("text", chunk.getDeltaContent()));
                }
                if (chunk.hasUsage()) {
                    tokenUsage[0] = chunk.getPromptTokens() != null ? chunk.getPromptTokens() : tokenUsage[0];
                    tokenUsage[1] = chunk.getCompletionTokens() != null ? chunk.getCompletionTokens() : tokenUsage[1];
                }
            });
            connection.disconnect();

        } catch (Exception e) {
            throw new RuntimeException("LLM 流式调用失败: " + e.getMessage(), e);
        }

        long llmDuration = System.currentTimeMillis() - llmStart;
        log.info("步骤 LLM 调用完成: model={}, tokens={}/{}, duration={}ms",
                modelName, tokenUsage[0], tokenUsage[1], llmDuration);

        StepLlmResult result = new StepLlmResult();
        result.output = outputBuilder.toString();
        result.tokensPrompt = tokenUsage[0];
        result.tokensCompletion = tokenUsage[1];
        result.modelName = modelName;
        result.durationMs = llmDuration;
        return result;
    }

    // ===== 辅助方法 =====

    /**
     * 解析输入映射
     * <p>支持占位符替换：${step_1.output} 引用某步骤输出，${user_message} 引用用户原始输入</p>
     */
    private String resolveInputMapping(String inputMapping, Map<Integer, String> stepOutputs, String userMessage) {
        if (inputMapping == null || inputMapping.isBlank()) {
            return userMessage;
        }
        String resolved = inputMapping;
        // 替换 ${step_N.output}
        for (Map.Entry<Integer, String> entry : stepOutputs.entrySet()) {
            String placeholder = "${step_" + entry.getKey() + ".output}";
            resolved = resolved.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        // 替换 ${user_message}
        resolved = resolved.replace("${user_message}", userMessage != null ? userMessage : "");
        return resolved;
    }

    /**
     * 评估条件表达式
     * <p>支持 ${step_N.status} == "success" 等简单条件</p>
     */
    private boolean evaluateCondition(String condition, Map<Integer, String> stepOutputs,
                                      Map<Integer, String> stepStatuses) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        String evalExpr = condition;
        // 替换 ${step_N.status}
        for (Map.Entry<Integer, String> entry : stepStatuses.entrySet()) {
            String placeholder = "${step_" + entry.getKey() + ".status}";
            evalExpr = evalExpr.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        // 简单解析：if contains "=="
        if (evalExpr.contains("==")) {
            String[] parts = evalExpr.split("==", 2);
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim().replace("\"", "");
                return left.equals(right);
            }
        }
        // 默认 true
        return true;
    }

    /**
     * 创建流式 HTTP 连接（委托给 LlmSseHelper）
     */
    private HttpURLConnection createStreamingConnection(String url, String apiKey,
                                                         Map<String, Object> requestBody) throws Exception {
        return llmSseHelper.createConnection(url, apiKey, requestBody);
    }

    /**
     * 发送 SSE 事件（委托给 SseEvent 统一工具）
     */
    private void sendSseEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        SseEvent.send(emitter, type, data);
    }

    /**
     * 发送错误事件
     */
    private void sendSseError(SseEmitter emitter, String errorMessage) {
        sendSseEvent(emitter, "error", Map.of("errorMessage", errorMessage));
    }

    // ===== 会话落库（链路追踪历史） =====

    /**
     * 保存编排执行会话
     */
    private void saveSession(OrchestrationExecuteRequest request, SceneOrchestration orchestration,
                             String output, long durationMs, int tokensPrompt, int tokensCompletion,
                             String modelName, String status, String errorMessage, Long userId,
                             List<AgentTraceStep> steps, String strategy) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("orchestration");
        session.setBizId(orchestration != null ? orchestration.getId() : 0L);
        session.setBizName(orchestration != null ? orchestration.getName() : "编排执行");
        session.setSceneId(request != null ? request.getSceneId() : null);
        session.setConversationId(request != null ? request.getConversationId() : null);
        session.setRequestId(request != null ? request.getRequestId() : null);
        session.setUserMessage(request != null ? request.getMessage() : null);
        session.setOutputResult(output);
        session.setTraceSnapshot(buildTraceSnapshot(steps, durationMs, tokensPrompt, tokensCompletion,
                "orchestration", orchestration != null ? orchestration.getName() : null, strategy));
        session.setTotalDurationMs((int) durationMs);
        session.setTokensPrompt(tokensPrompt);
        session.setTokensCompletion(tokensCompletion);
        session.setTokensTotal(tokensPrompt + tokensCompletion);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    /**
     * 保存动态编排执行会话
     */
    private void saveDynamicSession(OrchestrationExecuteRequest request,
                                    String output, long durationMs, int tokensPrompt, int tokensCompletion,
                                    String modelName, String status, String errorMessage, Long userId,
                                    List<AgentTraceStep> steps, String strategy) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("orchestration");
        session.setBizId(0L);
        session.setBizName("动态编排");
        session.setSceneId(request != null ? request.getSceneId() : null);
        session.setConversationId(request != null ? request.getConversationId() : null);
        session.setRequestId(request != null ? request.getRequestId() : null);
        session.setUserMessage(request != null ? request.getMessage() : null);
        session.setOutputResult(output);
        session.setTraceSnapshot(buildTraceSnapshot(steps, durationMs, tokensPrompt, tokensCompletion,
                "orchestration", "动态编排", strategy));
        session.setTotalDurationMs((int) durationMs);
        session.setTokensPrompt(tokensPrompt);
        session.setTokensCompletion(tokensCompletion);
        session.setTokensTotal(tokensPrompt + tokensCompletion);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    /**
     * 构建链路追踪快照 JSON（新方案结构）
     * <p>编排执行：{route, orchestrationName, strategy, steps:[{stepIndex, stepName, agentId, agentName,
     * model, input, output, durationMs, tokensPrompt, tokensCompletion, status}],
     * totalTokensPrompt, totalTokensCompletion, totalDurationMs}</p>
     */
    private String buildTraceSnapshot(List<AgentTraceStep> steps, long durationMs,
                                      int tokensPrompt, int tokensCompletion,
                                      String route, String routeName, String strategy) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("route", route);
        if (routeName != null && !routeName.isBlank()) {
            snapshot.put("orchestrationName", routeName);
        }
        if (strategy != null && !strategy.isBlank()) {
            snapshot.put("strategy", strategy);
        }
        List<Map<String, Object>> stepMaps = new ArrayList<>();
        if (steps != null) {
            for (AgentTraceStep s : steps) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("stepIndex", s.getStepIndex());
                m.put("stepName", s.getStepName());
                m.put("status", s.getStatus());
                m.put("durationMs", s.getDurationMs());
                m.put("input", s.getInput());
                m.put("output", s.getOutput());
                Map<String, Object> meta = s.getMetadata();
                if (meta != null) {
                    if (meta.containsKey("agentId")) m.put("agentId", meta.get("agentId"));
                    if (meta.containsKey("agentName")) m.put("agentName", meta.get("agentName"));
                    if (meta.containsKey("model")) m.put("model", meta.get("model"));
                    if (meta.containsKey("tokensPrompt")) m.put("tokensPrompt", meta.get("tokensPrompt"));
                    if (meta.containsKey("tokensCompletion")) m.put("tokensCompletion", meta.get("tokensCompletion"));
                }
                stepMaps.add(m);
            }
        }
        snapshot.put("steps", stepMaps);
        snapshot.put("totalTokensPrompt", tokensPrompt);
        snapshot.put("totalTokensCompletion", tokensCompletion);
        snapshot.put("totalDurationMs", durationMs);
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.warn("序列化链路快照失败: {}", e.getMessage());
            return "{}";
        }
    }

    // ===== 内部结果类 =====

    /** 编排执行结果 */
    private static class OrchestrationResult {
        String output;
        int tokensPrompt;
        int tokensCompletion;
        List<AgentTraceStep> steps = new ArrayList<>();
    }

    /** 单次 LLM 调用结果 */
    private static class StepLlmResult {
        String output;
        int tokensPrompt;
        int tokensCompletion;
        String modelName;
        long durationMs;
    }
}
