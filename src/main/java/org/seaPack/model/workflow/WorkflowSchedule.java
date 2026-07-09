package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流调度实体
 * <p>对应 workflow_schedule 表，用于定时/周期触发工作流执行。</p>
 */
@Entity
@Data
@Table(name = "workflow_schedule")
public class WorkflowSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "workflow_id", nullable = false)
    @Comment("工作流ID")
    private Long workflowId;

    @Column(name = "name", length = 100)
    @Comment("调度名称")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("调度描述")
    private String description;

    // ===== 调度配置 =====

    @Column(name = "schedule_type", nullable = false, length = 20)
    @Comment("调度类型: cron/interval/once")
    private String scheduleType;

    @Column(name = "cron_expression", length = 100)
    @Comment("Cron表达式")
    private String cronExpression;

    @Column(name = "interval_seconds")
    @Comment("间隔秒数")
    private Integer intervalSeconds;

    @Column(name = "scheduled_time")
    @Comment("定时执行时间")
    private Date scheduledTime;

    // ===== 输入参数 =====

    @Column(name = "input_params", columnDefinition = "JSON")
    @Comment("每次执行的输入参数")
    private String inputParams;

    // ===== 状态 =====

    @Column(name = "status")
    @Comment("状态: 1=启用, 0=禁用")
    private Integer status;

    @Column(name = "last_run_at")
    @Comment("上次执行时间")
    private Date lastRunAt;

    @Column(name = "next_run_at")
    @Comment("下次执行时间")
    private Date nextRunAt;

    @Column(name = "run_count")
    @Comment("已执行次数")
    private Integer runCount;

    // ===== 元数据 =====

    @Column(name = "created_by")
    @Comment("创建人ID")
    private Long createdBy;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
