package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 技能执行日志实体
 * <p>对应 ai_skill_execution_log 表，记录每次 AI 技能调用的输入输出、Token 消耗和费用估算。</p>
 */
@Entity
@Data
@Table(name = "ai_skill_execution_log")
public class SkillExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "skill_id")
    @Comment("技能ID")
    private Long skillId;

    @Column(name = "skill_code")
    @Comment("技能编码（冗余，方便查询）")
    private String skillCode;

    @Column(name = "model_name")
    @Comment("调用的模型编码")
    private String modelName;

    @Column(name = "module_key")
    @Comment("来源模块")
    private String moduleKey;

    @Column(name = "scene_id")
    @Comment("所属场景ID")
    private Long sceneId;

    @Column(name = "agent_id")
    @Comment("所属AgentID")
    private Long agentId;

    @Column(name = "input_params", columnDefinition = "JSON")
    @Comment("输入参数 JSON")
    private String inputParams;

    @Column(name = "output_result", columnDefinition = "LONGTEXT")
    @Comment("输出结果")
    private String outputResult;

    @Column(name = "tokens_prompt")
    @Comment("提示词token数")
    private Integer tokensPrompt;

    @Column(name = "tokens_completion")
    @Comment("补全token数")
    private Integer tokensCompletion;

    @Column(name = "tokens_total")
    @Comment("总token = prompt + completion")
    private Integer tokensTotal;

    @Column(name = "cost_yuan")
    @Comment("估算费用（元）")
    private BigDecimal costYuan;

    @Column(name = "duration_ms")
    @Comment("执行耗时(毫秒)")
    private Integer durationMs;

    @Column(name = "status")
    @Comment("状态：success / fail / timeout")
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "created_by")
    @Comment("执行人ID")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
