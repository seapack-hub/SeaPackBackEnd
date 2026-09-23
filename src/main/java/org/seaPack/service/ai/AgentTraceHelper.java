package org.seaPack.service.ai;

import org.seaPack.dto.ai.*;
import org.seaPack.model.ai.Agent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Agent 链路追踪构建工具类
 * <p>提供 trace step 和 snapshot 的构建方法。</p>
 */
@Component
public class AgentTraceHelper {

    /**
     * 构建失败步骤
     */
    public AgentTraceStep buildFailStep(int stepIndex, String stepType, String stepName, String errorMessage) {
        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType(stepType);
        step.setStepName(stepName);
        step.setStatus("fail");
        step.setDurationMs(0L);
        step.setOutput(errorMessage);
        step.setMetadata(new HashMap<>());
        return step;
    }

    /**
     * 构建链路追踪快照
     */
    public AgentTraceSnapshot buildTraceSnapshot(Agent agent, List<AgentTraceStep> steps, long totalDuration,
                                                  int promptTokens, int completionTokens) {
        AgentTraceSnapshot snapshot = new AgentTraceSnapshot();
        snapshot.setRoute("agent");
        snapshot.setAgentName(agent != null ? agent.getName() : null);
        snapshot.setModel(agent != null ? agent.getModelCode() : null);
        snapshot.setSystemPromptLength(agent != null && agent.getSystemPrompt() != null
                ? agent.getSystemPrompt().length() : 0);
        snapshot.setTokensPrompt(promptTokens);
        snapshot.setTokensCompletion(completionTokens);
        snapshot.setSteps(steps);
        snapshot.setTotalDurationMs(totalDuration);
        AgentTraceSnapshot.TotalTokens tokens = new AgentTraceSnapshot.TotalTokens();
        tokens.setPrompt(promptTokens);
        tokens.setCompletion(completionTokens);
        snapshot.setTotalTokens(tokens);
        return snapshot;
    }

    /**
     * 构建错误响应（含 trace snapshot）
     */
    public AgentTestChatResponse buildErrorResponse(Agent agent, AiDialogRequest request,
                                                     List<AgentTraceStep> steps, long totalStart,
                                                     Long userId, Exception e) {
        long totalDuration = System.currentTimeMillis() - totalStart;
        AgentTraceSnapshot snapshot = new AgentTraceSnapshot();
        snapshot.setRoute("agent");
        snapshot.setAgentName(agent != null ? agent.getName() : null);
        snapshot.setModel(agent != null ? agent.getModelCode() : null);
        snapshot.setSteps(steps);
        snapshot.setTotalDurationMs(totalDuration);
        AgentTraceSnapshot.TotalTokens tokens = new AgentTraceSnapshot.TotalTokens();
        tokens.setPrompt(0);
        tokens.setCompletion(0);
        snapshot.setTotalTokens(tokens);

        AgentTestChatResponse response = new AgentTestChatResponse();
        response.setContent("");
        response.setTokensPrompt(0);
        response.setTokensCompletion(0);
        response.setDurationMs((int) totalDuration);
        response.setTraceSnapshot(snapshot);
        return response;
    }
}
