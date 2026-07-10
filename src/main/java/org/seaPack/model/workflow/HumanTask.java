package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 人工任务实体
 * <p>对应 human_task 表，存储工作流中需要人工处理的审批/标注/反馈等任务。</p>
 */
@Entity
@Data
@Table(name = "human_task")
public class HumanTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "instance_id", nullable = false)
    @Comment("工作流实例ID")
    private Long instanceId;

    @Column(name = "node_id", nullable = false, length = 100)
    @Comment("工作流节点ID")
    private String nodeId;

    @Column(name = "node_log_id")
    @Comment("关联的节点执行日志ID")
    private Long nodeLogId;

    // ===== 任务类型 =====

    @Column(name = "task_type", nullable = false, length = 50)
    @Comment("任务类型: approval/review/annotation/feedback/input")
    private String taskType;

    // ===== 审批人配置 =====

    @Column(name = "assignee_type", length = 20)
    @Comment("审批人类型: user/role/department/expression")
    private String assigneeType;

    @Column(name = "assignee_ids", columnDefinition = "JSON")
    @Comment("审批人ID列表")
    private String assigneeIds;

    @Column(name = "assignee_expression", length = 500)
    @Comment("动态审批人表达式")
    private String assigneeExpression;

    // ===== 任务内容 =====

    @Column(name = "title", length = 200)
    @Comment("任务标题")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("任务描述")
    private String description;

    @Column(name = "content", columnDefinition = "JSON")
    @Comment("待审核/标注的数据内容")
    private String content;

    // ===== 任务状态 =====

    @Column(name = "status")
    @Comment("状态: 0=待处理, 1=处理中, 2=已通过, 3=已驳回, 4=已升级, 5=已过期, 6=已转办")
    private Integer status;

    // ===== 审核结果 =====

    @Column(name = "action", length = 20)
    @Comment("审核动作: approve/reject/return/delegate")
    private String action;

    @Column(name = "result", columnDefinition = "JSON")
    @Comment("审核结果数据")
    private String result;

    @Column(name = "comment", columnDefinition = "TEXT")
    @Comment("审核意见")
    private String comment;

    // ===== 超时策略 =====

    @Column(name = "timeout_minutes")
    @Comment("超时时间（分钟）")
    private Integer timeoutMinutes;

    @Column(name = "timeout_action", length = 20)
    @Comment("超时动作: auto_approve/auto_reject/escalate")
    private String timeoutAction;

    // ===== 转办信息 =====

    @Column(name = "delegated_to")
    @Comment("转办人ID")
    private Long delegatedTo;

    @Column(name = "delegated_at")
    @Comment("转办时间")
    private Date delegatedAt;

    // ===== 时间信息 =====

    @Column(name = "due_at")
    @Comment("截止时间")
    private Date dueAt;

    @Column(name = "started_at")
    @Comment("开始处理时间")
    private Date startedAt;

    @Column(name = "completed_at")
    @Comment("完成时间")
    private Date completedAt;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    // ===== 关联字段（查询返回，非数据库字段） =====

    @Transient
    @Comment("工作流名称")
    private String workflowName;

    @Transient
    @Comment("工作流ID")
    private Long workflowId;
}
