package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 技能执行响应 DTO
 * <p>返回 AI 执行结果、Token 消耗统计及日志记录 ID。</p>
 */
@Data
public class SkillExecuteResponse {

    /** 技能 ID */
    private Long skillId;

    /** 技能编码 */
    private String skillCode;

    /** AI 生成的输出内容 */
    private String output;

    /** 提示词消耗的 Token 数 */
    private Integer tokensPrompt;

    /** 补全生成的 Token 数 */
    private Integer tokensCompletion;

    /** 执行耗时（毫秒） */
    private Integer durationMs;

    /** 执行日志记录 ID */
    private Long logId;
}
