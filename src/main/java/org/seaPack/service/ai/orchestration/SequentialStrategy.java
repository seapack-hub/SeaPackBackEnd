package org.seaPack.service.ai.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.service.ai.AgentTestChatService;
import org.seaPack.service.ai.TokenStatsService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 顺序执行策略
 * <p>
 * 按步骤索引依次执行，每步走完整 Agent 流程（提示词组装 → 知识库检索 → LLM with tools）。
 * 支持 nodeType=condition 条件分支（仅支持 ${step_N.status} == "值" / ${step_N.output} ==
 * "值"）、
 * aggregate 结果汇总、inputMode 输入来源（user_input / prev_output）、retryCount 失败重试。
 * 注：outputTarget / timeoutMs / shared_state / handoff 字段暂未在本策略中生效。
 * </p>
 */
@Slf4j
@Component
public class SequentialStrategy extends OrchestrationStrategyHandler {

    private final AgentTestChatService agentTestChatService;
    private final TokenStatsService tokenStatsService;

    public SequentialStrategy(AgentTestChatService agentTestChatService, TokenStatsService tokenStatsService) {
        this.agentTestChatService = agentTestChatService;
        this.tokenStatsService = tokenStatsService;
    }

    @Override
    public OrchestrationResult execute(ExecuteParams params) {
        return executeSequential(params.steps, params.request, params.emitter, params.isCompleted, params.userId,
                params.orchestration, params.authToken);
    }

    private OrchestrationResult executeSequential(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            SseEmitter emitter,
            AtomicBoolean isCompleted,
            Long userId,
            SceneOrchestration orchestration,
            String authToken) {

        StringBuilder overallOutput = new StringBuilder();
        Map<Integer, String> stepOutputs = new HashMap<>();
        Map<Integer, String> stepStatuses = new HashMap<>();
        List<AgentTraceStep> stepInfos = new ArrayList<>();
        int totalPrompt = 0;
        int totalCompletion = 0;

        // 按 stepIndex 建索引，支持条件分支跳转
        Map<Integer, SceneOrchestrationStep> stepMap = new LinkedHashMap<>();
        for (SceneOrchestrationStep s : steps) {
            if (s.getStepIndex() != null) {
                stepMap.put(s.getStepIndex(), s);
            }
        }

        Integer currentStepIndex = steps.isEmpty() ? 0 : steps.get(0).getStepIndex();

        // C6: 循环跳转保护——复用编排 maxRounds（无值时按步骤数 3 倍兜底），防止条件分支死循环烧 Token
        int maxIterations = (orchestration != null && orchestration.getMaxRounds() != null
                && orchestration.getMaxRounds() > 0)
                        ? orchestration.getMaxRounds() * Math.max(steps.size(), 1)
                        : Math.max(steps.size() * 3, 10);
        int iterations = 0;
        // C7: 记录最后执行的步骤，用于判断最终输出是否取 aggregate 结果
        Integer lastExecutedStepIdx = null;

        while (currentStepIndex != null && !isCompleted.get()) {
            if (++iterations > maxIterations) {
                sendSseError(emitter, "步骤跳转次数超过上限（" + maxIterations + " 次），可能存在循环跳转，请检查条件分支配置");
                break;
            }
            SceneOrchestrationStep step = stepMap.get(currentStepIndex);
            if (step == null)
                break;

            if (step.getStatus() != null && step.getStatus() != 1) {
                currentStepIndex = getNextStepIndex(step, stepMap);
                continue;
            }

            long stepStart = System.currentTimeMillis();
            int stepIdx = step.getStepIndex();

            // ===== condition 节点 =====
            if ("condition".equals(step.getNodeType())) {
                if (step.getCondition() != null && !step.getCondition().isBlank()) {
                    ConditionResult condResult = evaluateCondition(step.getCondition(), stepOutputs, stepStatuses);

                    sendSseEvent(emitter, "step_start", Map.of(
                            "stepIndex", stepIdx, "stepType", "condition",
                            "stepName", step.getStepName() != null ? step.getStepName() : ("条件判断" + stepIdx)));

                    long condDuration = System.currentTimeMillis() - stepStart;
                    AgentTraceStep traceStep = new AgentTraceStep();
                    traceStep.setStepIndex(stepIdx);
                    traceStep.setStepType("condition");
                    traceStep.setStepName(step.getStepName() != null ? step.getStepName() : "条件判断");

                    // C1: 表达式非法 → step_error + fail，不跳转，降级为顺序下一步
                    if (!condResult.valid) {
                        sendSseEvent(emitter, "step_error",
                                Map.of("stepIndex", stepIdx, "errorMessage", condResult.error));
                        sendSseEvent(emitter, "step_done", Map.of(
                                "stepIndex", stepIdx, "status", "fail", "durationMs", condDuration,
                                "errorMessage", condResult.error));
                        traceStep.setStatus("fail");
                        traceStep.setDurationMs(condDuration);
                        traceStep.setOutput("条件表达式非法: " + condResult.error);
                        stepInfos.add(traceStep);
                        stepStatuses.put(stepIdx, "fail");
                        currentStepIndex = getNextStepIndex(step, stepMap);
                        continue;
                    }

                    boolean conditionMet = condResult.met;
                    // C5: 跳转目标校验——目标不存在时发错误并降级为顺序下一步，不再静默终止
                    Integer jumpTarget = conditionMet ? step.getBranchTrueStep() : step.getBranchFalseStep();
                    if (jumpTarget != null && !stepMap.containsKey(jumpTarget)) {
                        String invalidMsg = "条件跳转目标步骤不存在: step_" + jumpTarget + "，已降级为顺序下一步";
                        sendSseEvent(emitter, "step_error", Map.of("stepIndex", stepIdx, "errorMessage", invalidMsg));
                        log.warn("步骤[{}] 条件跳转目标不存在: {}", step.getStepName(), jumpTarget);
                        jumpTarget = getNextStepIndex(step, stepMap);
                    }

                    String jumpMessage = conditionMet ? "条件为真，跳转到步骤 " + step.getBranchTrueStep()
                            : "条件为假，跳转到步骤 " + step.getBranchFalseStep();
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", stepIdx, "phase", "condition_eval",
                            "rawCondition", step.getCondition(),
                            "result", conditionMet ? "true" : "false",
                            "branchTrueStep", step.getBranchTrueStep() != null ? step.getBranchTrueStep() : "null",
                            "branchFalseStep", step.getBranchFalseStep() != null ? step.getBranchFalseStep() : "null",
                            "message", jumpMessage));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "success",
                            "durationMs", condDuration,
                            "result", conditionMet ? "true" : "false"));

                    traceStep.setStatus("success");
                    traceStep.setDurationMs(condDuration);
                    traceStep.setOutput(conditionMet ? "条件为真" : "条件为假");
                    stepInfos.add(traceStep);
                    stepStatuses.put(stepIdx, "success");

                    currentStepIndex = jumpTarget;
                    continue;
                }
                currentStepIndex = getNextStepIndex(step, stepMap);
                continue;
            }

            // ===== aggregate 节点 =====
            if ("aggregate".equals(step.getNodeType())) {
                sendSseEvent(emitter, "step_start", Map.of(
                        "stepIndex", stepIdx, "stepType", "aggregate",
                        "stepName", step.getStepName() != null ? step.getStepName() : ("结果汇总" + stepIdx)));

                // C7: 聚合"本步骤之前"的 agent 步骤输出，排除此前的 aggregate 步骤，避免嵌套重复
                StringBuilder aggregated = new StringBuilder();
                for (Map.Entry<Integer, String> entry : stepOutputs.entrySet()) {
                    if (entry.getKey().equals(stepIdx))
                        continue; // 跳过自身（防御性）
                    SceneOrchestrationStep prevStep = stepMap.get(entry.getKey());
                    if (prevStep != null && "aggregate".equals(prevStep.getNodeType()))
                        continue;
                    if (entry.getValue() == null || entry.getValue().isEmpty())
                        continue;
                    if (aggregated.length() > 0)
                        aggregated.append("\n\n");
                    aggregated.append("【步骤").append(entry.getKey()).append("】\n").append(entry.getValue());
                }
                String aggregatedOutput = aggregated.toString();
                stepOutputs.put(stepIdx, aggregatedOutput);
                stepStatuses.put(stepIdx, "success");
                lastExecutedStepIdx = stepIdx;
                // C7: 聚合结果只写入 stepOutputs 供后续 ${step_N.output} 引用，不再直接拼进 overallOutput；
                // 若本步骤是最后一步，循环结束后由 overallOutput 兜底逻辑取用

                long aggDuration = System.currentTimeMillis() - stepStart;
                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", stepIdx, "status", "success",
                        "durationMs", aggDuration,
                        "output", aggregatedOutput.length() > 500 ? aggregatedOutput.substring(0, 500) + "..."
                                : aggregatedOutput));

                AgentTraceStep traceStep = new AgentTraceStep();
                traceStep.setStepIndex(stepIdx);
                traceStep.setStepType("aggregate");
                traceStep.setStepName(step.getStepName() != null ? step.getStepName() : "结果汇总");
                traceStep.setStatus("success");
                traceStep.setDurationMs(aggDuration);
                traceStep.setOutput(aggregatedOutput);
                stepInfos.add(traceStep);

                currentStepIndex = getNextStepIndex(step, stepMap);
                continue;
            }

            // ===== agent 节点 =====

            // 条件评估（旧方式兼容）
            if (step.getCondition() != null && !step.getCondition().isBlank()) {
                ConditionResult condResult = evaluateCondition(step.getCondition(), stepOutputs, stepStatuses);
                if (!condResult.valid) {
                    // C1: 表达式非法 → step_error + fail，跳过本步骤
                    sendSseEvent(emitter, "step_start", Map.of(
                            "stepIndex", stepIdx, "stepType", "llm",
                            "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)));
                    sendSseEvent(emitter, "step_error", Map.of("stepIndex", stepIdx, "errorMessage", condResult.error));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "fail",
                            "message", condResult.error, "durationMs", 0));
                    stepStatuses.put(stepIdx, "fail");
                    AgentTraceStep invalidCondTrace = new AgentTraceStep();
                    invalidCondTrace.setStepIndex(stepIdx);
                    invalidCondTrace.setStepType("llm_call");
                    invalidCondTrace.setStepName(step.getStepName() != null ? step.getStepName() : "步骤" + stepIdx);
                    invalidCondTrace.setStatus("fail");
                    invalidCondTrace.setDurationMs(0L);
                    invalidCondTrace.setOutput("条件表达式非法，跳过本步骤: " + condResult.error);
                    stepInfos.add(invalidCondTrace);
                    currentStepIndex = getNextStepIndex(step, stepMap);
                    continue;
                }
                if (!condResult.met) {
                    sendSseEvent(emitter, "step_start", Map.of(
                            "stepIndex", stepIdx, "stepType", "llm",
                            "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "skip",
                            "message", "条件不满足: " + step.getCondition(), "durationMs", 0));
                    stepStatuses.put(stepIdx, "skip");
                    AgentTraceStep traceStep = new AgentTraceStep();
                    traceStep.setStepIndex(stepIdx);
                    traceStep.setStepType("llm_call");
                    traceStep.setStepName(step.getStepName() != null ? step.getStepName() : "步骤" + stepIdx);
                    traceStep.setStatus("skip");
                    traceStep.setDurationMs(0L);
                    traceStep.setOutput("条件不满足，跳过本步骤");
                    stepInfos.add(traceStep);
                    currentStepIndex = getNextStepIndex(step, stepMap);
                    continue;
                }
            }

            sendSseEvent(emitter, "step_start", Map.of(
                    "stepIndex", stepIdx, "stepType", "llm",
                    "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)));

            AgentTraceStep traceStep = new AgentTraceStep();
            traceStep.setStepIndex(stepIdx);
            traceStep.setStepType("llm_call");
            traceStep.setStepName(step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx));
            traceStep.setStatus("running");
            stepInfos.add(traceStep);

            try {
                if (step.getAgentId() == null) {
                    throw new IllegalArgumentException("Agent 节点缺少 agentId: step=" + step.getStepName());
                }

                String stepInput = resolveInput(step, stepOutputs, request.getMessage());

                sendSseEvent(emitter, "step_detail", Map.of(
                        "stepIndex", stepIdx, "phase", "input_resolved",
                        "inputMode", step.getInputMode() != null ? step.getInputMode() : "user_input",
                        "resolvedInput",
                        stepInput.length() > 500 ? stepInput.substring(0, 500) + "...(" + stepInput.length() + "字符)"
                                : stepInput,
                        "message", "输入解析完成"));

                AgentTestChatService.AgentStepResult agentResult = agentTestChatService.callAgentStep(
                        step.getAgentId(), stepInput, request.getHistory(),
                        request.getSceneId(), request.getConversationId(), request.getRequestId(),
                        emitter, isCompleted, authToken, stepIdx);

                if (isCompleted.get())
                    break;

                if (agentResult.success) {
                    stepOutputs.put(stepIdx, agentResult.output);
                    stepStatuses.put(stepIdx, "success");
                    lastExecutedStepIdx = stepIdx;
                    totalPrompt += agentResult.tokensPrompt;
                    totalCompletion += agentResult.tokensCompletion;

                    if (overallOutput.length() > 0 && agentResult.output != null && !agentResult.output.isEmpty()) {
                        overallOutput.append("\n\n");
                    }
                    if (agentResult.output != null) {
                        overallOutput.append(agentResult.output);
                    }

                    long stepDuration = System.currentTimeMillis() - stepStart;
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "success", "durationMs", stepDuration,
                            "output", agentResult.output,
                            "tokensPrompt", agentResult.tokensPrompt, "tokensCompletion", agentResult.tokensCompletion,
                            "model", agentResult.modelName));

                    traceStep.setStatus("success");
                    traceStep.setDurationMs(stepDuration);
                    traceStep.setInput(stepInput);
                    traceStep.setOutput(agentResult.output);
                    Map<String, Object> stepMeta = new HashMap<>();
                    stepMeta.put("tokensPrompt", agentResult.tokensPrompt);
                    stepMeta.put("tokensCompletion", agentResult.tokensCompletion);
                    stepMeta.put("model", agentResult.modelName);
                    traceStep.setMetadata(stepMeta);

                    // 记录 Token 统计
                    try {
                        TokenUsageLog tokenLog = new TokenUsageLog();
                        tokenLog.setCallTime(new Date());
                        tokenLog.setModelName(agentResult.modelName);
                        tokenLog.setTokensInput(agentResult.tokensPrompt);
                        tokenLog.setTokensOutput(agentResult.tokensCompletion);
                        tokenLog.setDurationMs((int) agentResult.durationMs);
                        tokenLog.setStatus("success");
                        tokenLog.setUserId(userId);
                        tokenLog.setBizType("orchestration");
                        tokenLog.setSceneId(request.getSceneId());
                        tokenLog.setAgentId(step.getAgentId());
                        tokenLog.setOrchestrationStep(stepIdx);
                        tokenLog.setRequestId(request.getRequestId());
                        tokenStatsService.recordCall(tokenLog);
                    } catch (Exception e) {
                        log.error("记录 Token 统计失败: step={}, {}", stepIdx, e.getMessage(), e);
                    }
                } else {
                    stepStatuses.put(stepIdx, "fail");
                    long stepDuration = System.currentTimeMillis() - stepStart;

                    sendSseEvent(emitter, "step_error",
                            Map.of("stepIndex", stepIdx, "errorMessage", agentResult.errorMessage));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "fail", "durationMs", stepDuration,
                            "errorMessage", agentResult.errorMessage));
                    traceStep.setStatus("fail");
                    traceStep.setDurationMs(stepDuration);
                    traceStep.setOutput("执行失败: " + agentResult.errorMessage);

                    // 重试逻辑
                    if (step.getRetryCount() != null && step.getRetryCount() > 0) {
                        boolean retried = false;
                        for (int i = 0; i < step.getRetryCount(); i++) {
                            if (isCompleted.get())
                                break;
                            log.info("步骤[{}] 第{}次重试", step.getStepName(), i + 1);
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx, "phase", "retry",
                                    "retryIndex", i + 1, "maxRetry", step.getRetryCount(),
                                    "message", "第" + (i + 1) + "次重试"));
                            try {
                                AgentTestChatService.AgentStepResult retryResult = agentTestChatService.callAgentStep(
                                        step.getAgentId(), stepInput, request.getHistory(),
                                        request.getSceneId(), request.getConversationId(), request.getRequestId(),
                                        emitter, isCompleted, authToken, stepIdx);
                                if (retryResult.success) {
                                    stepOutputs.put(stepIdx, retryResult.output);
                                    stepStatuses.put(stepIdx, "success");
                                    lastExecutedStepIdx = stepIdx;
                                    totalPrompt += retryResult.tokensPrompt;
                                    totalCompletion += retryResult.tokensCompletion;
                                    if (overallOutput.length() > 0 && retryResult.output != null) {
                                        overallOutput.append("\n\n");
                                    }
                                    if (retryResult.output != null) {
                                        overallOutput.append(retryResult.output);
                                    }
                                    long retryDuration = System.currentTimeMillis() - stepStart;
                                    sendSseEvent(emitter, "step_done", Map.of(
                                            "stepIndex", stepIdx, "status", "success", "durationMs", retryDuration,
                                            "output", retryResult.output));
                                    traceStep.setStatus("success");
                                    traceStep.setDurationMs(retryDuration);
                                    traceStep.setOutput(retryResult.output);
                                    retried = true;
                                    break;
                                }
                            } catch (Exception retryEx) {
                                log.warn("步骤[{}] 第{}次重试失败: {}", step.getStepName(), i + 1, retryEx.getMessage());
                            }
                        }
                        if (retried) {
                            currentStepIndex = getNextStepIndex(step, stepMap);
                            continue;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("步骤[{}]执行异常: {}", step.getStepName(), e.getMessage());
                stepStatuses.put(stepIdx, "fail");
                long stepDuration = System.currentTimeMillis() - stepStart;
                sendSseEvent(emitter, "step_error", Map.of("stepIndex", stepIdx, "errorMessage", e.getMessage()));
                traceStep.setStatus("fail");
                traceStep.setDurationMs(stepDuration);
                traceStep.setOutput("执行异常: " + e.getMessage());
            }

            currentStepIndex = getNextStepIndex(step, stepMap);
        }

        // C7: 若最后执行的是 aggregate 步骤，其聚合输出即为最终结果（覆盖 overallOutput，
        // 避免"聚合内容 + 后续无内容"或"聚合与 agent 输出叠加"的语义混乱）
        if (lastExecutedStepIdx != null) {
            SceneOrchestrationStep lastStep = stepMap.get(lastExecutedStepIdx);
            if (lastStep != null && "aggregate".equals(lastStep.getNodeType())) {
                String aggOutput = stepOutputs.get(lastExecutedStepIdx);
                if (aggOutput != null && !aggOutput.isEmpty()) {
                    overallOutput.setLength(0);
                    overallOutput.append(aggOutput);
                }
            }
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt;
        result.tokensCompletion = totalCompletion;
        result.steps = stepInfos;
        return result;
    }
}
