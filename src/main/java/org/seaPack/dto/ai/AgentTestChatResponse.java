package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 测试对话响应 DTO
 * <p>包含 Agent 回复内容、Token 统计及完整链路追踪快照。</p>
 */
@Data
public class AgentTestChatResponse {

    /** Agent 回复内容 */
    private String content;

    /** 提示词 token 数 */
    private Integer tokensPrompt;

    /** 补全 token 数 */
    private Integer tokensCompletion;

    /** 执行耗时（毫秒） */
    private Integer durationMs;

    /** 链路追踪快照 */
    private AgentTraceSnapshot traceSnapshot;
}
