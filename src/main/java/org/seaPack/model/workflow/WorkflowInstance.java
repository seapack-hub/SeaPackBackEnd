package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流执行实例实体
 * <p>对应 workflow_instance 表，记录每次工作流执行的状态和进度。</p>
 */
@Entity
@Data
@Table(name = "workflow_instance")
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "workflow_id")
    @Comment("工作流定义ID")
    private Long workflowId;

    @Column(name = "workflow_version")
    @Comment("执行时的工作流版本")
    private Integer workflowVersion;

    @Column(name = "workflow_name")
    @Comment("工作流名称（冗余）")
    private String workflowName;

    @Column(name = "definition_snapshot", columnDefinition = "JSON")
    @Comment("完整工作流定义快照")
    private String definitionSnapshot;

    @Column(name = "status")
    @Comment("状态: 0=待执行, 1=运行中, 2=已完成, 3=失败, 4=暂停, 5=已取消")
    private Integer status;

    @Column(name = "trigger_type")
    @Comment("触发类型: manual/api/schedule/event")
    private String triggerType;

    @Column(name = "input_params", columnDefinition = "JSON")
    @Comment("输入变量数据")
    private String inputParams;

    @Column(name = "output_result", columnDefinition = "JSON")
    @Comment("输出变量数据")
    private String outputResult;

    @Column(name = "current_node_id")
    @Comment("当前执行到的节点ID")
    private String currentNodeId;

    @Column(name = "completed_nodes", columnDefinition = "JSON")
    @Comment("已完成的节点ID列表")
    private String completedNodes;

    @Column(name = "total_nodes")
    @Comment("总节点数")
    private Integer totalNodes;

    @Column(name = "completed_count")
    @Comment("已完成节点数")
    private Integer completedCount;

    @Column(name = "started_at")
    @Comment("开始执行时间")
    private Date startedAt;

    @Column(name = "finished_at")
    @Comment("执行完成时间")
    private Date finishedAt;

    @Column(name = "duration_ms")
    @Comment("执行耗时（毫秒）")
    private Integer durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "error_node_id")
    @Comment("出错的节点ID")
    private String errorNodeId;

    @Column(name = "created_by")
    @Comment("创建人ID")
    private Long createdBy;

    /** 创建人名称（非数据库字段，关联查询填充） */
    @Transient
    private String createdByName;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
