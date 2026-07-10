package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 反馈记录实体
 * <p>对应 feedback_record 表，存储人工任务中的反馈数据，用于模型改进。</p>
 */
@Entity
@Data
@Table(name = "feedback_record")
public class FeedbackRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "task_id")
    @Comment("人工任务ID")
    private Long taskId;

    @Column(name = "instance_id")
    @Comment("工作流实例ID")
    private Long instanceId;

    @Column(name = "node_log_id")
    @Comment("节点执行日志ID")
    private Long nodeLogId;

    @Column(name = "session_id")
    @Comment("AI会话ID")
    private Long sessionId;

    // ===== 反馈类型 =====

    @Column(name = "feedback_type", nullable = false, length = 50)
    @Comment("反馈类型: thumbs_up/thumbs_down/correction/suggestion/bug_report")
    private String feedbackType;

    // ===== 反馈内容 =====

    @Column(name = "original_output", columnDefinition = "TEXT")
    @Comment("原始输出")
    private String originalOutput;

    @Column(name = "corrected_output", columnDefinition = "TEXT")
    @Comment("修正后的输出")
    private String correctedOutput;

    @Column(name = "reason", columnDefinition = "TEXT")
    @Comment("反馈原因")
    private String reason;

    @Column(name = "rating")
    @Comment("评分 1-5")
    private Integer rating;

    // ===== 关联信息 =====

    @Column(name = "skill_id")
    @Comment("关联的技能ID")
    private Long skillId;

    @Column(name = "model_code", length = 50)
    @Comment("使用的模型编码")
    private String modelCode;

    // ===== 反馈人 =====

    @Column(name = "created_by")
    @Comment("反馈人ID")
    private Long createdBy;

    @Column(name = "created_at")
    @Comment("反馈时间")
    private Date createdAt;

    // ===== 关联字段（非数据库字段） =====

    @Transient
    @Comment("反馈人姓名")
    private String createdByName;
}
