package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Token 调用明细实体
 * <p>对应 ai_token_usage_log 表，记录每次 AI 调用的 Token 消耗和费用明细。</p>
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

    @Column(name = "call_time")
    @Comment("调用时间")
    private Date callTime;

    @Column(name = "module_key")
    @Comment("模块标识")
    private String moduleKey;

    @Column(name = "model_name")
    @Comment("模型编码")
    private String modelName;

    @Column(name = "agent_id")
    @Comment("Agent ID")
    private Long agentId;

    /** Agent 名称（非数据库字段，JOIN 返回） */
    @Transient
    private String agentName;

    @Column(name = "scene_id")
    @Comment("场景ID")
    private Long sceneId;

    /** 场景名称（非数据库字段，JOIN 返回） */
    @Transient
    private String sceneName;

    @Column(name = "skill_id")
    @Comment("技能ID")
    private Long skillId;

    @Column(name = "tokens_input")
    @Comment("输入Token数")
    private Integer tokensInput;

    @Column(name = "tokens_output")
    @Comment("输出Token数")
    private Integer tokensOutput;

    @Column(name = "duration_ms")
    @Comment("执行耗时(毫秒)")
    private Integer durationMs;

    @Column(name = "cost_yuan")
    @Comment("费用(元)")
    private BigDecimal costYuan;

    @Column(name = "status")
    @Comment("状态：success / fail")
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
