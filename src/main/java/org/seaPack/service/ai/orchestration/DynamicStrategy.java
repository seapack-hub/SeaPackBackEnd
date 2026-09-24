package org.seaPack.service.ai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.service.ai.AgentTestChatService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Dynamic 执行模式（LLM 动态规划）
 * <p>LLM 根据用户问题 + Agent 描述动态生成执行计划，
 * 然后按计划依次执行各 Agent。</p>
 */
@Slf4j
@Component
public class DynamicStrategy extends OrchestrationStrategyHandler {

    private final AgentTestChatService agentTestChatService;
    private final AgentMapper agentMapper;
    private final ObjectMapper objectMapper;

    public DynamicStrategy(AgentTestChatService agentTestChatService,
                           AgentMapper agentMapper,
                           ObjectMapper objectMapper) {
        this.agentTestChatService = agentTestChatService;
        this.agentMapper = agentMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public OrchestrationResult execute(ExecuteParams params) {
        return executeDynamicPlan(params.orchestration, params.steps, params.request,
                params.emitter, params.isCompleted, params.userId);
    }

    private OrchestrationResult executeDynamicPlan(
            SceneOrchestration orchestration,
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            SseEmitter emitter,
            AtomicBoolean isCompleted,
            Long userId) {

        StringBuilder overallOutput = new StringBuilder();
        List<AgentTraceStep> stepInfos = new ArrayList<>();
        int totalPrompt = 0;
        int totalCompletion = 0;

        // 收集所有可用 Agent
        List<Long> agentIds = steps.stream()
                .map(SceneOrchestrationStep::getAgentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Agent> agentMap = new HashMap<>();
        StringBuilder agentListDesc = new StringBuilder("## 可用 Agent 列表\n\n");
        for (Long aid : agentIds) {
            Agent a = agentMapper.selectById(aid);
            if (a != null && a.getStatus() != null && a.getStatus() == 1) {
                agentMap.put(aid, a);
                agentListDesc.append("- **").append(a.getName()).append("** (ID: ").append(aid).append(")\n");
                if (a.getDescription() != null && !a.getDescription().isBlank()) {
                    agentListDesc.append("  能力描述: ").append(a.getDescription()).append("\n");
                }
            }
        }

        sendSseEvent(emitter, "orchestration_start", Map.of(
                "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "Dynamic",
                "strategy", "dynamic",
                "totalSteps", steps.size(),
                "agentCount", agentIds.size(),
                "message", "Dynamic 模式启动，LLM 将动态规划执行方案"
        ));

        // Step 1: LLM 生成执行计划
        sendSseEvent(emitter, "step_start", Map.of(
                "stepIndex", 0,
                "stepType", "plan_generation",
                "stepName", "执行计划生成"
        ));

        try {
            String planPrompt = agentListDesc.toString() +
                    "\n## 用户问题\n" + request.getMessage() +
                    "\n\n## 要求\n请根据用户问题，选择最合适的 Agent 来回答。" +
                    "输出格式为 JSON 数组，每个元素包含 agent_id 和 task 字段。" +
                    "例如：[{\"agent_id\": 1, \"task\": \"分析用户的问题并回答\"}]" +
                    "\n只输出 JSON，不要其他内容。";

            AgentTestChatService.AgentStepResult planResult = agentTestChatService.callAgentStep(
                    agentIds.get(0), planPrompt, null,
                    request.getSceneId(), request.getConversationId(), request.getRequestId(),
                    null, isCompleted, null); // emitter=null，不推送内部步骤

            if (isCompleted.get()) {
                OrchestrationResult r = new OrchestrationResult();
                r.output = "";
                return r;
            }

            totalPrompt += planResult.tokensPrompt;
            totalCompletion += planResult.tokensCompletion;

            sendSseEvent(emitter, "step_done", Map.of(
                    "stepIndex", 0,
                    "status", "success",
                    "durationMs", planResult.durationMs,
                    "output", planResult.output != null ? planResult.output.substring(0, Math.min(200, planResult.output.length())) : ""
            ));

            // Step 2: 解析计划并执行
            String planOutput = planResult.output != null ? planResult.output.trim() : "[]";
            // 尝试提取 JSON
            int jsonStart = planOutput.indexOf('[');
            int jsonEnd = planOutput.lastIndexOf(']');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                planOutput = planOutput.substring(jsonStart, jsonEnd + 1);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> plan = objectMapper.readValue(planOutput, List.class);

            for (int i = 0; i < plan.size() && !isCompleted.get(); i++) {
                Map<String, Object> stepPlan = plan.get(i);
                Object agentIdObj = stepPlan.get("agent_id");
                String task = stepPlan.get("task") != null ? stepPlan.get("task").toString() : request.getMessage();

                Long planAgentId = null;
                if (agentIdObj instanceof Number) {
                    planAgentId = ((Number) agentIdObj).longValue();
                } else if (agentIdObj instanceof String) {
                    planAgentId = Long.parseLong((String) agentIdObj);
                }

                if (planAgentId == null || !agentMap.containsKey(planAgentId)) {
                    log.warn("Dynamic 计划中的 Agent ID 无效: {}", agentIdObj);
                    continue;
                }

                sendSseEvent(emitter, "step_start", Map.of(
                        "stepIndex", i + 1,
                        "stepType", "dynamic_execution",
                        "stepName", "执行计划步骤 " + (i + 1)
                ));

                AgentTestChatService.AgentStepResult execResult = agentTestChatService.callAgentStep(
                        planAgentId, task, request.getHistory(),
                        request.getSceneId(), request.getConversationId(), request.getRequestId(),
                        emitter, isCompleted, null);

                if (isCompleted.get()) break;

                if (execResult.success) {
                    totalPrompt += execResult.tokensPrompt;
                    totalCompletion += execResult.tokensCompletion;

                    if (overallOutput.length() > 0 && execResult.output != null) {
                        overallOutput.append("\n\n");
                    }
                    if (execResult.output != null) {
                        overallOutput.append(execResult.output);
                    }

                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", i + 1,
                            "status", "success",
                            "durationMs", execResult.durationMs,
                            "output", execResult.output
                    ));

                    Agent traceAgent = agentMap.get(planAgentId);
                    AgentTraceStep traceStep = new AgentTraceStep();
                    traceStep.setStepIndex(i + 1);
                    traceStep.setStepType("dynamic_execution");
                    traceStep.setStepName(traceAgent != null ? traceAgent.getName() : "Agent-" + planAgentId);
                    traceStep.setStatus("success");
                    traceStep.setDurationMs(execResult.durationMs);
                    traceStep.setOutput(execResult.output);
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("agentId", planAgentId);
                    meta.put("agentName", traceAgent != null ? traceAgent.getName() : "");
                    meta.put("model", execResult.modelName);
                    meta.put("task", task);
                    traceStep.setMetadata(meta);
                    stepInfos.add(traceStep);
                } else {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", i + 1,
                            "errorMessage", execResult.errorMessage
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Dynamic 执行异常: {}", e.getMessage(), e);
            sendSseError(emitter, "Dynamic 执行失败: " + e.getMessage());
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt;
        result.tokensCompletion = totalCompletion;
        result.steps = stepInfos;
        return result;
    }
}
