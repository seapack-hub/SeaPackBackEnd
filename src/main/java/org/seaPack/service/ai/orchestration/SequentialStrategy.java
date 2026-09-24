package org.seaPack.service.ai.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.service.ai.AgentTestChatService;
import org.seaPack.service.ai.TokenStatsService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 顺序执行策略
 * <p>按步骤索引依次执行，每步走完整 Agent 流程（提示词组装 → 知识库检索 → LLM with tools）。
 * 支持 nodeType=condition 条件分支、inputMode 输入来源、outputTarget 输出去向。</p>
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
        return executeSequential(params.steps, params.request, params.emitter, params.isCompleted, params.userId);
    }

    private OrchestrationResult executeSequential(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            SseEmitter emitter,
            AtomicBoolean isCompleted,
            Long userId) {

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

        while (currentStepIndex != null && !isCompleted.get()) {
            SceneOrchestrationStep step = stepMap.get(currentStepIndex);
            if (step == null) break;

            if (step.getStatus() != null && step.getStatus() != 1) {
                currentStepIndex = getNextStepIndex(step, stepMap);
                continue;
            }

            long stepStart = System.currentTimeMillis();
            int stepIdx = step.getStepIndex();

            // ===== condition 节点 =====
            if ("condition".equals(step.getNodeType())) {
                if (step.getCondition() != null && !step.getCondition().isBlank()) {
                    boolean conditionMet = evaluateCondition(step.getCondition(), stepOutputs, stepStatuses);

                    sendSseEvent(emitter, "step_start", Map.of(
                            "stepIndex", stepIdx, "stepType", "condition",
                            "stepName", step.getStepName() != null ? step.getStepName() : ("条件判断" + stepIdx)
                    ));
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", stepIdx, "phase", "condition_eval",
                            "rawCondition", step.getCondition(),
                            "result", conditionMet ? "true" : "false",
                            "branchTrueStep", step.getBranchTrueStep() != null ? step.getBranchTrueStep() : "null",
                            "branchFalseStep", step.getBranchFalseStep() != null ? step.getBranchFalseStep() : "null",
                            "message", conditionMet ? "条件为真，跳转到步骤 " + step.getBranchTrueStep() : "条件为假，跳转到步骤 " + step.getBranchFalseStep()
                    ));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "success",
                            "durationMs", System.currentTimeMillis() - stepStart,
                            "result", conditionMet ? "true" : "false"
                    ));

                    AgentTraceStep traceStep = new AgentTraceStep();
                    traceStep.setStepIndex(stepIdx);
                    traceStep.setStepType("condition");
                    traceStep.setStepName(step.getStepName() != null ? step.getStepName() : "条件判断");
                    traceStep.setStatus("success");
                    traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                    traceStep.setOutput(conditionMet ? "条件为真" : "条件为假");
                    stepInfos.add(traceStep);
                    stepStatuses.put(stepIdx, "success");

                    currentStepIndex = conditionMet ? step.getBranchTrueStep() : step.getBranchFalseStep();
                    continue;
                }
                currentStepIndex = getNextStepIndex(step, stepMap);
                continue;
            }

            // ===== aggregate 节点 =====
            if ("aggregate".equals(step.getNodeType())) {
                sendSseEvent(emitter, "step_start", Map.of(
                        "stepIndex", stepIdx, "stepType", "aggregate",
                        "stepName", step.getStepName() != null ? step.getStepName() : ("结果汇总" + stepIdx)
                ));

                StringBuilder aggregated = new StringBuilder();
                for (Map.Entry<Integer, String> entry : stepOutputs.entrySet()) {
                    if (aggregated.length() > 0) aggregated.append("\n\n");
                    aggregated.append("【步骤").append(entry.getKey()).append("】\n").append(entry.getValue());
                }
                String aggregatedOutput = aggregated.toString();
                stepOutputs.put(stepIdx, aggregatedOutput);
                stepStatuses.put(stepIdx, "success");
                overallOutput.append(aggregatedOutput);

                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", stepIdx, "status", "success",
                        "durationMs", System.currentTimeMillis() - stepStart,
                        "output", aggregatedOutput.length() > 500 ? aggregatedOutput.substring(0, 500) + "..." : aggregatedOutput
                ));

                AgentTraceStep traceStep = new AgentTraceStep();
                traceStep.setStepIndex(stepIdx);
                traceStep.setStepType("aggregate");
                traceStep.setStepName(step.getStepName() != null ? step.getStepName() : "结果汇总");
                traceStep.setStatus("success");
                traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                traceStep.setOutput(aggregatedOutput);
                stepInfos.add(traceStep);

                currentStepIndex = getNextStepIndex(step, stepMap);
                continue;
            }

            // ===== agent 节点 =====

            // 条件评估（旧方式兼容）
            if (step.getCondition() != null && !step.getCondition().isBlank()) {
                boolean conditionMet = evaluateCondition(step.getCondition(), stepOutputs, stepStatuses);
                if (!conditionMet) {
                    sendSseEvent(emitter, "step_start", Map.of(
                            "stepIndex", stepIdx, "stepType", "llm",
                            "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)
                    ));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "skip",
                            "message", "条件不满足: " + step.getCondition(), "durationMs", 0
                    ));
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
                    "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)
            ));

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
                        "resolvedInput", stepInput.length() > 500 ? stepInput.substring(0, 500) + "...(" + stepInput.length() + "字符)" : stepInput,
                        "message", "输入解析完成"
                ));

                AgentTestChatService.AgentStepResult agentResult = agentTestChatService.callAgentStep(
                        step.getAgentId(), stepInput, request.getHistory(),
                        request.getSceneId(), request.getConversationId(), request.getRequestId(),
                        emitter, isCompleted, null);

                if (isCompleted.get()) break;

                if (agentResult.success) {
                    stepOutputs.put(stepIdx, agentResult.output);
                    stepStatuses.put(stepIdx, "success");
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
                            "model", agentResult.modelName
                    ));

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

                    sendSseEvent(emitter, "step_error", Map.of("stepIndex", stepIdx, "errorMessage", agentResult.errorMessage));
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx, "status", "fail", "durationMs", stepDuration,
                            "errorMessage", agentResult.errorMessage
                    ));
                    traceStep.setStatus("fail");
                    traceStep.setDurationMs(stepDuration);
                    traceStep.setOutput("执行失败: " + agentResult.errorMessage);

                    // 重试逻辑
                    if (step.getRetryCount() != null && step.getRetryCount() > 0) {
                        boolean retried = false;
                        for (int i = 0; i < step.getRetryCount(); i++) {
                            if (isCompleted.get()) break;
                            log.info("步骤[{}] 第{}次重试", step.getStepName(), i + 1);
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", stepIdx, "phase", "retry",
                                    "retryIndex", i + 1, "maxRetry", step.getRetryCount(),
                                    "message", "第" + (i + 1) + "次重试"
                            ));
                            try {
                                AgentTestChatService.AgentStepResult retryResult = agentTestChatService.callAgentStep(
                                        step.getAgentId(), stepInput, request.getHistory(),
                                        request.getSceneId(), request.getConversationId(), request.getRequestId(),
                                        emitter, isCompleted, null);
                                if (retryResult.success) {
                                    stepOutputs.put(stepIdx, retryResult.output);
                                    stepStatuses.put(stepIdx, "success");
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
                                            "output", retryResult.output
                                    ));
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

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt;
        result.tokensCompletion = totalCompletion;
        result.steps = stepInfos;
        return result;
    }
}
