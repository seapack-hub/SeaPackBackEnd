package org.seaPack.dto.ai;

import lombok.Data;

/**
 * Agent 对话响应 DTO
 * <p>返回助手回复内容、Token 消耗统计及执行耗时。</p>
 */
@Data
public class AgentChatResponse {

    /** 助手回复内容 */
    private String content;

    /** 提示词 token 数 */
    private Integer tokensPrompt;

    /** 补全 token 数 */
    private Integer tokensCompletion;

    /** 执行耗时（毫秒） */
    private Integer durationMs;
}
