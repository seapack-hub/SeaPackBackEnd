package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 标注记录实体
 * <p>对应 annotation_record 表，存储人工任务中的标注数据。</p>
 */
@Entity
@Data
@Table(name = "annotation_record")
public class AnnotationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "task_id", nullable = false)
    @Comment("人工任务ID")
    private Long taskId;

    @Column(name = "instance_id")
    @Comment("工作流实例ID")
    private Long instanceId;

    @Column(name = "node_log_id")
    @Comment("节点执行日志ID")
    private Long nodeLogId;

    // ===== 标注内容 =====

    @Column(name = "content_id", length = 100)
    @Comment("标注对象ID")
    private String contentId;

    @Column(name = "content_type", length = 50)
    @Comment("标注对象类型: chunk/qa_pair/document/answer")
    private String contentType;

    @Column(name = "annotation_type", length = 50)
    @Comment("标注类型: label/correction/rating/tag/quality")
    private String annotationType;

    // ===== 标注数据 =====

    @Column(name = "label", length = 100)
    @Comment("标签")
    private String label;

    @Column(name = "score")
    @Comment("质量评分 0.00-1.00")
    private BigDecimal score;

    @Column(name = "content", columnDefinition = "TEXT")
    @Comment("标注内容")
    private String content;

    @Column(name = "metadata", columnDefinition = "JSON")
    @Comment("扩展元数据")
    private String metadata;

    // ===== 标注人 =====

    @Column(name = "annotated_by")
    @Comment("标注人ID")
    private Long annotatedBy;

    @Column(name = "annotated_at")
    @Comment("标注时间")
    private Date annotatedAt;

    // ===== 关联字段（非数据库字段） =====

    @Transient
    @Comment("标注人姓名")
    private String annotatedByName;
}
