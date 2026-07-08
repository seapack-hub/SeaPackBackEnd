package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * Skill 调试日志实体
 * <p>对应 ai_skill_debug_log 表，记录每次调试执行的完整输入输出及 LLM 原始交互。</p>
 */
@Entity
@Data
@Table(name = "ai_skill_debug_log")
public class SkillDebugLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "skill_id")
    @Comment("调试的技能 ID")
    private Long skillId;

    @Column(name = "skill_name")
    @Comment("技能名称（冗余）")
    private String skillName;

    @Column(name = "skill_code")
    @Comment("技能编码（冗余）")
    private String skillCode;

    @Column(name = "input_params", columnDefinition = "JSON")
    @Comment("输入参数键值对")
    private String inputParams;

    @Column(name = "user_message")
    @Comment("用户补充指令")
    private String userMessage;

    @Column(name = "raw_prompt_template", columnDefinition = "TEXT")
    @Comment("原始提示词模板（含 {{变量}}）")
    private String rawPromptTemplate;

    @Column(name = "rendered_prompt", columnDefinition = "TEXT")
    @Comment("变量替换后的完整 Prompt")
    private String renderedPrompt;

    @Column(name = "llm_request_body", columnDefinition = "JSON")
    @Comment("发送给 LLM 的完整请求体")
    private String llmRequestBody;

    @Column(name = "llm_response_body", columnDefinition = "JSON")
    @Comment("LLM 返回的原始响应体")
    private String llmResponseBody;

    @Column(name = "llm_model")
    @Comment("实际调用的模型")
    private String llmModel;

    @Column(name = "tokens_prompt")
    @Comment("提示词 Token 数")
    private Integer tokensPrompt;

    @Column(name = "tokens_completion")
    @Comment("补全 Token 数")
    private Integer tokensCompletion;

    @Column(name = "duration_ms")
    @Comment("总耗时（毫秒）")
    private Integer durationMs;

    @Column(name = "duration_llm_ms")
    @Comment("LLM 调用耗时（毫秒）")
    private Integer durationLlmMs;

    @Column(name = "output_result", columnDefinition = "TEXT")
    @Comment("最终输出结果")
    private String outputResult;

    @Column(name = "status")
    @Comment("状态：success/fail/timeout")
    private String status;

    @Column(name = "error_message")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "created_by")
    @Comment("调试人")
    private Long createdBy;

    @Column(name = "created_at")
    @Comment("调试时间")
    private Date createdAt;
}
