package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;

/**
 * 链路追踪快照 DTO
 * <p>包含完整的调用链路步骤列表和汇总指标。
 * 新方案新增 route / agentName / model / systemPromptLength / tokensPrompt / tokensCompletion 顶层字段，
 * 同时保留 steps / totalDurationMs / totalTokens 兼容旧版链路展示。</p>
 */
@Data
public class AgentTraceSnapshot {

    /** 链路类型：agent / orchestration / llm（新方案） */
    private String route;

    /** 链路名称（Agent 名 / 编排名，新方案） */
    private String agentName;

    /** 使用的模型（新方案） */
    private String model;

    /** 系统提示词长度（新方案，单 Agent 对话） */
    private Integer systemPromptLength;

    /** 提示词 Token 数（新方案顶层字段） */
    private Integer tokensPrompt;

    /** 补全 Token 数（新方案顶层字段） */
    private Integer tokensCompletion;

    /** 调用链路步骤列表 */
    private List<AgentTraceStep> steps;

    /** 总耗时（毫秒） */
    private Long totalDurationMs;

    /** Token 汇总 */
    private TotalTokens totalTokens;

    @Data
    public static class TotalTokens {
        private Integer prompt;
        private Integer completion;
    }
}
