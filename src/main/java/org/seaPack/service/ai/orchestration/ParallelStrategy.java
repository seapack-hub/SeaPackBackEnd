package org.seaPack.service.ai.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.service.ai.AgentTestChatService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 并行执行策略
 * <p>所有步骤同时调用各自的 Agent，最终合并输出。
 * 注意：并行模式下 input_mapping 不能引用其他步骤的输出（因为同时执行）。</p>
 */
@Slf4j
@Component
public class ParallelStrategy extends OrchestrationStrategyHandler {

    private final AgentTestChatService agentTestChatService;

    public ParallelStrategy(AgentTestChatService agentTestChatService) {
        this.agentTestChatService = agentTestChatService;
    }

    @Override
    public OrchestrationResult execute(ExecuteParams params) {
        return executeParallel(params.steps, params.request, params.emitter, params.isCompleted, params.userId);
    }

    private OrchestrationResult executeParallel(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            SseEmitter emitter,
            AtomicBoolean isCompleted,
            Long userId) {

        StringBuilder overallOutput = new StringBuilder();
        int[] totalPrompt = {0};
        int[] totalCompletion = {0};
        List<AgentTraceStep> stepInfos = Collections.synchronizedList(new ArrayList<>());
        int stepCount = steps.size();
        String[] orderedOutputs = new String[stepCount];

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = new CompletableFuture[stepCount];

        for (int i = 0; i < stepCount; i++) {
            SceneOrchestrationStep step = steps.get(i);
            int index = i;
            int stepIdx = step.getStepIndex();

            futures[i] = CompletableFuture.runAsync(() -> {
                if (isCompleted.get()) return;

                long stepStart = System.currentTimeMillis();
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
                    if (step.getStatus() != null && step.getStatus() != 1) {
                        orderedOutputs[index] = "";
                        traceStep.setStatus("skip");
                        traceStep.setDurationMs(0L);
                        traceStep.setOutput("步骤已禁用，跳过执行");
                        return;
                    }

                    if (step.getAgentId() == null) {
                        throw new IllegalArgumentException("Agent 节点缺少 agentId");
                    }

                    String stepInput = resolveInput(step, new HashMap<>(), request.getMessage());

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

                    if (isCompleted.get()) return;

                    if (agentResult.success) {
                        orderedOutputs[index] = agentResult.output;
                        totalPrompt[0] += agentResult.tokensPrompt;
                        totalCompletion[0] += agentResult.tokensCompletion;

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
                    } else {
                        orderedOutputs[index] = "";
                        sendSseEvent(emitter, "step_error", Map.of("stepIndex", stepIdx, "errorMessage", agentResult.errorMessage));
                        traceStep.setStatus("fail");
                        traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                        traceStep.setOutput("执行失败: " + agentResult.errorMessage);
                    }
                } catch (Exception e) {
                    orderedOutputs[index] = "";
                    sendSseEvent(emitter, "step_error", Map.of("stepIndex", stepIdx, "errorMessage", e.getMessage()));
                    traceStep.setStatus("fail");
                    traceStep.setDurationMs(System.currentTimeMillis() - stepStart);
                    traceStep.setOutput("执行异常: " + e.getMessage());
                }
            });
        }

        // 等待所有步骤完成
        try {
            CompletableFuture.allOf(futures).get();
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
        stepInfos.sort(Comparator.comparing(s -> s.getStepIndex() == null ? 0 : s.getStepIndex()));
        result.steps = stepInfos;
        return result;
    }
}
