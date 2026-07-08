package org.seaPack.dto.ai;

import lombok.Data;

import java.util.Map;

/**
 * 调试执行响应 DTO
 * <p>包含完整调试信息：输出、Prompt、LLM 原始请求/响应、Token 统计及日志 ID。</p>
 */
@Data
public class SkillDebugResponse {

    /** 最终输出 */
    private String output;

    /** 渲染后的 Prompt */
    private String renderedPrompt;

    /** 原始模板 */
    private String rawPromptTemplate;

    /** LLM 请求体 */
    private Map<String, Object> llmRequestBody;

    /** LLM 响应体 */
    private Map<String, Object> llmResponseBody;

    /** 模型 */
    private String llmModel;

    /** Token 数 */
    private Integer tokensPrompt;

    private Integer tokensCompletion;

    /** 总耗时 */
    private Integer durationMs;

    /** LLM 调用耗时 */
    private Integer durationLlmMs;

    /** 日志 ID */
    private Long debugLogId;
}
