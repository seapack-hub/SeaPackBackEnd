package org.seaPack.service.ai.tool;

import java.util.List;
import java.util.Map;

/**
 * LLM 工具回路扩展上下文（编排级工具注入）
 * <p>
 * 允许编排策略（如 Supervisor）向 Agent 的 Function Calling 回路注入临时工具：
 * 工具定义与 Agent 技能工具合并后传给 LLM，命中的工具调用不再走
 * {@link ToolCallDispatcher}（DB 技能体系），而是回调 {@link ToolExecutor} 由编排逻辑执行。
 * </p>
 * <p>
 * 注意：编排级工具仅在注入本上下文的那次 LLM 调用内有效，不会持久化为 Skill，
 * Worker Agent 执行时不注入本上下文，天然避免调度递归。
 * </p>
 */
public class OrchestrationToolContext {

    /** 编排级工具定义（OpenAI tools 格式），与 Agent 技能工具合并后传给 LLM */
    private final List<Map<String, Object>> toolDefinitions;

    /** 编排级工具执行器：工具名 + 参数 → 工具结果（作为 tool 消息回填给 LLM） */
    private final ToolExecutor toolExecutor;

    /** 工具回路最大轮数（null 时使用默认值） */
    private final Integer maxToolRounds;

    public OrchestrationToolContext(List<Map<String, Object>> toolDefinitions,
            ToolExecutor toolExecutor, Integer maxToolRounds) {
        this.toolDefinitions = toolDefinitions;
        this.toolExecutor = toolExecutor;
        this.maxToolRounds = maxToolRounds;
    }

    public List<Map<String, Object>> getToolDefinitions() {
        return toolDefinitions;
    }

    public ToolExecutor getToolExecutor() {
        return toolExecutor;
    }

    public Integer getMaxToolRounds() {
        return maxToolRounds;
    }

    /**
     * 编排级工具执行器（函数式接口）
     */
    @FunctionalInterface
    public interface ToolExecutor {

        /**
         * 执行编排级工具
         *
         * @param toolName  工具名（如 select_agent / aggregate_results）
         * @param arguments LLM 返回的结构化参数
         * @return 工具结果文本（将作为 tool 消息 content 回填给 LLM，约定 JSON 格式）
         */
        String execute(String toolName, Map<String, Object> arguments);
    }
}
