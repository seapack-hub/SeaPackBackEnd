package org.seaPack.dto.ai;

import lombok.Data;

/**
 * AI 执行通用返回类型
 * <p>技能执行、模板执行等所有 AI 调用共用的返回结构。</p>
 */
@Data
public class AiExecuteResult {

    /** 渲染后的完整 Prompt（变量替换后） */
    private String renderedPrompt;

    /** LLM 生成的输出内容 */
    private String output;

    /** 提示词消耗的 Token 数 */
    private Integer tokensPrompt;

    /** 补全生成的 Token 数 */
    private Integer tokensCompletion;

    /** 执行耗时（毫秒） */
    private Integer durationMs;
}
