package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Token 调用明细实体
 * <p>对应 ai_token_usage_log 表，记录每次 LLM 调用的 Token 消耗、费用和归属信息。</p>
 */
@Entity
@Data
@Table(name = "ai_token_usage_log")
public class TokenUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    // ===== 调用信息 =====

    @Column(name = "call_time")
    @Comment("调用时间")
    private Date callTime;

    @Column(name = "model_name")
    @Comment("模型编码（如 gpt-4o、deepseek-chat）")
    private String modelName;

    @Column(name = "tokens_input")
    @Comment("输入 Token 数")
    private Integer tokensInput;

    @Column(name = "tokens_output")
    @Comment("输出 Token 数")
    private Integer tokensOutput;

    @Column(name = "duration_ms")
    @Comment("执行耗时（毫秒）")
    private Integer durationMs;

    @Column(name = "cost_yuan")
    @Comment("费用（元）")
    private BigDecimal costYuan;

    @Column(name = "status")
    @Comment("状态：success / fail")
    private String status;

    // ===== 归属信息（谁、在做什么） =====

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID（关联 sys_user.id）")
    private Long userId;

    @Column(name = "biz_type", nullable = false)
    @Comment("用途：orchestration-编排 / agent-Agent对话 / chat-通用对话 / skill-Skill执行")
    private String bizType;

    @Column(name = "scene_id")
    @Comment("场景ID（关联 ai_scene.id）")
    private Long sceneId;

    /** 场景名称（非数据库字段，JOIN 返回） */
    @Transient
    private String sceneName;

    @Column(name = "agent_id")
    @Comment("Agent ID（关联 ai_agent.id）")
    private Long agentId;

    /** Agent 名称（非数据库字段，JOIN 返回） */
    @Transient
    private String agentName;

    @Column(name = "skill_id")
    @Comment("Skill ID（关联 ai_skill.id）")
    private Long skillId;

    @Column(name = "orchestration_step")
    @Comment("编排步骤序号（biz_type=orchestration 时有效）")
    private Integer orchestrationStep;

    // ===== 关联信息 =====

    @Column(name = "session_id")
    @Comment("关联 ai_execution_session.id")
    private Long sessionId;

    @Column(name = "request_id")
    @Comment("前端生成的请求ID（用于追踪单轮对话）")
    private String requestId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息（status=fail 时记录）")
    private String errorMessage;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
