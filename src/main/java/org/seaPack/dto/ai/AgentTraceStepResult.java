package org.seaPack.dto.ai;

import java.util.List;

/**
 * Agent 链路追踪步骤执行结果
 * <p>用于各步骤之间的数据传递：step（追踪信息）、output（步骤输出）、nextStepIndex（下一步序号）。</p>
 */
public class AgentTraceStepResult {
    public AgentTraceStep step;
    public String output;
    public int nextStepIndex;
    public int tokensPrompt;
    public int tokensCompletion;
    public String modelName;

    /** 附加步骤列表（如工具调用步骤，需插入到 step 之前） */
    public List<AgentTraceStep> extraSteps;
}
