package org.seaPack.service.ai.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.AgentMessageMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.AgentMessage;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.service.ai.AgentTestChatService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Crew 执行模式
 * <p>Agent 间通过 delegate_to_agent 工具自主委托任务。
 * 每个 Agent 注入 delegate_to_agent 工具，可自主决定将任务委托给其他 Agent。</p>
 */
@Slf4j
@Component
public class CrewStrategy extends OrchestrationStrategyHandler {

    private final AgentTestChatService agentTestChatService;
    private final AgentMapper agentMapper;
    private final AgentMessageMapper agentMessageMapper;

    public CrewStrategy(AgentTestChatService agentTestChatService,
                         AgentMapper agentMapper,
                         AgentMessageMapper agentMessageMapper) {
        this.agentTestChatService = agentTestChatService;
        this.agentMapper = agentMapper;
        this.agentMessageMapper = agentMessageMapper;
    }

    @Override
    public OrchestrationResult execute(ExecuteParams params) {
        return executeCrew(params.orchestration, params.steps, params.request,
                params.emitter, params.isCompleted, params.userId);
    }

    private OrchestrationResult executeCrew(
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
        int maxRounds = orchestration.getMaxRounds() != null ? orchestration.getMaxRounds() : 5;

        // 收集所有参与的 Agent
        List<Long> agentIds = steps.stream()
                .map(SceneOrchestrationStep::getAgentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (agentIds.isEmpty()) {
            sendSseError(emitter, "Crew 模式需要至少一个 Agent");
            return new OrchestrationResult();
        }

        // 加载所有 Agent 信息
        Map<Long, Agent> agentMap = new HashMap<>();
        for (Long aid : agentIds) {
            Agent a = agentMapper.selectById(aid);
            if (a != null) agentMap.put(aid, a);
        }

        sendSseEvent(emitter, "orchestration_start", Map.of(
                "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "Crew",
                "strategy", "crew",
                "totalSteps", steps.size(),
                "agentCount", agentIds.size(),
                "maxRounds", maxRounds,
                "message", "Crew 模式启动，共 " + agentIds.size() + " 个 Agent 协作"
        ));

        // 由第一个 Agent 开始执行
        Long currentAgentId = agentIds.get(0);
        String currentInput = request.getMessage();

        for (int round = 0; round < maxRounds && !isCompleted.get(); round++) {
            Agent currentAgent = agentMap.get(currentAgentId);
            if (currentAgent == null) break;

            sendSseEvent(emitter, "step_start", Map.of(
                    "stepIndex", round,
                    "stepType", "crew_execution",
                    "stepName", currentAgent.getName() + " 执行 (轮次 " + (round + 1) + ")"
            ));

            try {
                AgentTestChatService.AgentStepResult agentResult = agentTestChatService.callAgentStep(
                        currentAgentId, currentInput, request.getHistory(),
                        request.getSceneId(), request.getConversationId(), request.getRequestId(),
                        emitter, isCompleted, null);

                if (isCompleted.get()) break;

                if (!agentResult.success) {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", round,
                            "errorMessage", currentAgent.getName() + " 执行失败: " + agentResult.errorMessage
                    ));
                    break;
                }

                totalPrompt += agentResult.tokensPrompt;
                totalCompletion += agentResult.tokensCompletion;

                // 记录消息
                AgentMessage msg = new AgentMessage();
                msg.setRoundIndex(round);
                msg.setSenderType("worker");
                msg.setSenderAgentId(currentAgentId);
                msg.setMessageType("report");
                msg.setContent(agentResult.output);
                try {
                    agentMessageMapper.insert(msg);
                } catch (Exception e) {
                    log.warn("保存 Crew 消息失败: {}", e.getMessage());
                }

                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", round,
                        "status", "success",
                        "durationMs", agentResult.durationMs,
                        "output", agentResult.output
                ));

                AgentTraceStep traceStep = new AgentTraceStep();
                traceStep.setStepIndex(round);
                traceStep.setStepType("crew_execution");
                traceStep.setStepName(currentAgent.getName());
                traceStep.setStatus("success");
                traceStep.setDurationMs(agentResult.durationMs);
                traceStep.setOutput(agentResult.output);
                Map<String, Object> meta = new HashMap<>();
                meta.put("agentId", currentAgentId);
                meta.put("agentName", currentAgent.getName());
                meta.put("model", agentResult.modelName);
                meta.put("tokensPrompt", agentResult.tokensPrompt);
                meta.put("tokensCompletion", agentResult.tokensCompletion);
                traceStep.setMetadata(meta);
                stepInfos.add(traceStep);

                overallOutput.append(agentResult.output);

                // Crew 模式：第一个 Agent 的输出作为最终结果（简化版）
                break;

            } catch (Exception e) {
                log.error("Crew 轮次 {} 异常: {}", round + 1, e.getMessage(), e);
                sendSseEvent(emitter, "step_error", Map.of("stepIndex", round, "errorMessage", e.getMessage()));
                break;
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
