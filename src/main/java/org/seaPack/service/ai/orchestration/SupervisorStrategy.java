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

/**
 * Supervisor 执行模式
 * <p>一个总控 Agent 通过 Function Calling 动态调度 Worker Agent。
 * Supervisor 的 system prompt 中注入两个工具：
 * select_agent(agent_id, task_description) — 选择 Worker 执行任务
 * aggregate_results(final_answer) — 汇总所有结果输出最终答案</p>
 */
@Slf4j
@Component
public class SupervisorStrategy extends OrchestrationStrategyHandler {

    private final AgentTestChatService agentTestChatService;
    private final AgentMapper agentMapper;
    private final AgentMessageMapper agentMessageMapper;

    public SupervisorStrategy(AgentTestChatService agentTestChatService,
                               AgentMapper agentMapper,
                               AgentMessageMapper agentMessageMapper) {
        this.agentTestChatService = agentTestChatService;
        this.agentMapper = agentMapper;
        this.agentMessageMapper = agentMessageMapper;
    }

    @Override
    public OrchestrationResult execute(ExecuteParams params) {
        return executeSupervisor(params.orchestration, params.steps, params.request,
                params.emitter, params.isCompleted, params.userId);
    }

    private OrchestrationResult executeSupervisor(
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

        // 加载 Supervisor Agent
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

        // 收集所有可用 Worker Agent 信息
        List<Agent> workerAgents = new ArrayList<>();
        for (SceneOrchestrationStep s : steps) {
            if (s.getAgentId() != null && !s.getAgentId().equals(supervisorAgentId)) {
                Agent worker = agentMapper.selectById(s.getAgentId());
                if (worker != null && worker.getStatus() != null && worker.getStatus() == 1) {
                    workerAgents.add(worker);
                }
            }
        }

        // 构建 Worker Agent 描述列表
        StringBuilder workerListDesc = new StringBuilder("## 可用 Worker Agent 列表\n\n");
        for (Agent w : workerAgents) {
            workerListDesc.append("- **").append(w.getName()).append("** (ID: ").append(w.getId()).append(")\n");
            if (w.getDescription() != null && !w.getDescription().isBlank()) {
                workerListDesc.append("  描述: ").append(w.getDescription()).append("\n");
            }
        }
        workerListDesc.append("\n你可以使用 select_agent 工具选择一个或多个 Worker 来执行任务，");
        workerListDesc.append("使用 aggregate_results 工具汇总最终结果。");

        sendSseEvent(emitter, "orchestration_start", Map.of(
                "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "Supervisor",
                "strategy", "supervisor",
                "totalSteps", steps.size(),
                "supervisorAgentId", supervisorAgentId,
                "workerCount", workerAgents.size(),
                "maxRounds", maxRounds,
                "message", "Supervisor 模式启动，共 " + workerAgents.size() + " 个 Worker Agent"
        ));

        // 构建 Supervisor 的可用工具（select_agent + aggregate_results）
        List<Map<String, Object>> supervisorTools = new ArrayList<>();

        // select_agent 工具
        Map<String, Object> selectAgentTool = new HashMap<>();
        selectAgentTool.put("type", "function");
        Map<String, Object> selectAgentFn = new HashMap<>();
        selectAgentFn.put("name", "select_agent");
        selectAgentFn.put("description", "选择一个 Worker Agent 来执行特定任务");
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
        aggregateFn.put("name", "aggregate_results");
        aggregateFn.put("description", "汇总所有 Worker Agent 的结果，输出最终答案给用户");
        Map<String, Object> aggregateParams = new HashMap<>();
        aggregateParams.put("type", "object");
        Map<String, Object> aggregateProps = new HashMap<>();
        aggregateProps.put("answer", Map.of("type", "string", "description", "最终的汇总答案"));
        aggregateParams.put("properties", aggregateProps);
        aggregateParams.put("required", List.of("answer"));
        aggregateFn.put("parameters", aggregateParams);
        aggregateTool.put("function", aggregateFn);
        supervisorTools.add(aggregateTool);

        // 执行 Supervisor 循环
        String supervisorPrompt = workerListDesc.toString();
        List<Map<String, String>> conversationHistory = new ArrayList<>();
        conversationHistory.add(Map.of("role", "user", "content", request.getMessage()));

        for (int round = 0; round < maxRounds && !isCompleted.get(); round++) {
            sendSseEvent(emitter, "step_start", Map.of(
                    "stepIndex", round,
                    "stepType", "supervisor_round",
                    "stepName", "Supervisor 轮次 " + (round + 1)
            ));

            try {
                AgentTestChatService.AgentStepResult supervisorResult = agentTestChatService.callAgentStep(
                        supervisorAgentId, supervisorPrompt + "\n\n当前用户问题：" + request.getMessage(),
                        conversationHistory, request.getSceneId(),
                        request.getConversationId(), request.getRequestId(),
                        emitter, isCompleted, null);

                if (isCompleted.get()) break;

                if (!supervisorResult.success) {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", round,
                            "errorMessage", "Supervisor 执行失败: " + supervisorResult.errorMessage
                    ));
                    break;
                }

                totalPrompt += supervisorResult.tokensPrompt;
                totalCompletion += supervisorResult.tokensCompletion;

                // 记录 Supervisor 消息
                AgentMessage supervisorMsg = new AgentMessage();
                supervisorMsg.setSessionId(null);
                supervisorMsg.setRoundIndex(round);
                supervisorMsg.setSenderType("supervisor");
                supervisorMsg.setSenderAgentId(supervisorAgentId);
                supervisorMsg.setMessageType("instruction");
                supervisorMsg.setContent(supervisorResult.output);
                try {
                    agentMessageMapper.insert(supervisorMsg);
                } catch (Exception e) {
                    log.warn("保存 Supervisor 消息失败: {}", e.getMessage());
                }

                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", round,
                        "status", "success",
                        "durationMs", supervisorResult.durationMs,
                        "output", supervisorResult.output,
                        "round", round + 1
                ));

                // 如果 Supervisor 输出包含 aggregate_results 的结果，说明已完成
                if (supervisorResult.output != null && supervisorResult.output.contains("[AGGREGATED]")) {
                    String finalAnswer = supervisorResult.output.replace("[AGGREGATED]", "").trim();
                    overallOutput.append(finalAnswer);
                    break;
                }

                conversationHistory.add(Map.of("role", "assistant", "content", supervisorResult.output));

            } catch (Exception e) {
                log.error("Supervisor 轮次 {} 异常: {}", round + 1, e.getMessage(), e);
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
