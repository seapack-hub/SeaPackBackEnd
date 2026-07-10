package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流执行统计实体
 * <p>对应 workflow_stats 表，按工作流+日期维度聚合执行数据，用于 Dashboard 展示。</p>
 */
@Entity
@Data
@Table(name = "workflow_stats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"workflow_id", "stat_date"}))
public class WorkflowStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "workflow_id", nullable = false)
    @Comment("工作流ID")
    private Long workflowId;

    @Column(name = "stat_date", nullable = false)
    @Comment("统计日期")
    private Date statDate;

    // ===== 执行统计 =====

    @Column(name = "total_runs")
    @Comment("总执行次数")
    private Integer totalRuns;

    @Column(name = "success_count")
    @Comment("成功次数")
    private Integer successCount;

    @Column(name = "failed_count")
    @Comment("失败次数")
    private Integer failedCount;

    @Column(name = "running_count")
    @Comment("运行中次数")
    private Integer runningCount;

    // ===== 耗时统计 =====

    @Column(name = "avg_duration_ms")
    @Comment("平均耗时（毫秒）")
    private Integer avgDurationMs;

    @Column(name = "max_duration_ms")
    @Comment("最大耗时（毫秒）")
    private Integer maxDurationMs;

    @Column(name = "min_duration_ms")
    @Comment("最小耗时（毫秒）")
    private Integer minDurationMs;

    // ===== Token 统计 =====

    @Column(name = "total_tokens")
    @Comment("总Token消耗")
    private Integer totalTokens;

    // ===== 人工任务统计 =====

    @Column(name = "human_tasks_count")
    @Comment("人工任务数")
    private Integer humanTasksCount;

    @Column(name = "human_tasks_avg_minutes")
    @Comment("人工任务平均处理时间（分钟）")
    private Integer humanTasksAvgMinutes;

    // ===== 关联字段（非数据库字段，由 JOIN 查询填充） =====

    @Transient
    @Comment("工作流名称")
    private String workflowName;
}
