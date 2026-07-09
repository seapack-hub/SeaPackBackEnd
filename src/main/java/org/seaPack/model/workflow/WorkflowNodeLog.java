package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流节点执行日志实体
 * <p>对应 workflow_node_log 表，记录每个节点的执行详情。</p>
 */
@Entity
@Data
@Table(name = "workflow_node_log")
public class WorkflowNodeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "instance_id")
    @Comment("工作流实例ID")
    private Long instanceId;

    @Column(name = "node_id")
    @Comment("X6节点ID")
    private String nodeId;

    @Column(name = "node_type")
    @Comment("节点类型: start/end/skill/http_request/condition/approval等")
    private String nodeType;

    @Column(name = "node_name")
    @Comment("节点名称")
    private String nodeName;

    @Column(name = "status")
    @Comment("状态: 0=待执行, 1=执行中, 2=已完成, 3=失败, 4=跳过, 5=等待人工, 6=超时, 7=已取消")
    private Integer status;

    @Column(name = "input_data", columnDefinition = "JSON")
    @Comment("传入的变量数据")
    private String inputData;

    @Column(name = "output_data", columnDefinition = "JSON")
    @Comment("输出的变量数据")
    private String outputData;

    @Column(name = "node_config_snapshot", columnDefinition = "JSON")
    @Comment("节点业务配置快照")
    private String nodeConfigSnapshot;

    @Column(name = "retry_count")
    @Comment("重试次数")
    private Integer retryCount;

    @Column(name = "max_retries")
    @Comment("最大重试次数")
    private Integer maxRetries;

    @Column(name = "executor_type")
    @Comment("执行者类型: system/human/ai")
    private String executorType;

    @Column(name = "executor_ref")
    @Comment("执行者引用: 技能ID/用户ID/模型名称")
    private String executorRef;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "error_stack", columnDefinition = "TEXT")
    @Comment("错误堆栈")
    private String errorStack;

    @Column(name = "started_at")
    @Comment("开始时间")
    private Date startedAt;

    @Column(name = "completed_at")
    @Comment("完成时间")
    private Date completedAt;

    @Column(name = "duration_ms")
    @Comment("执行耗时（毫秒）")
    private Integer durationMs;
}
