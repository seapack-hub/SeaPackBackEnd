package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流版本历史实体
 * <p>对应 workflow_version 表，存储工作流的版本快照。</p>
 */
@Entity
@Data
@Table(name = "workflow_version")
public class WorkflowVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "workflow_id")
    @Comment("工作流ID")
    private Long workflowId;

    @Column(name = "version")
    @Comment("版本号")
    private Integer version;

    @Column(name = "nodes", columnDefinition = "JSON")
    @Comment("节点视觉数据快照")
    private String nodes;

    @Column(name = "edges", columnDefinition = "JSON")
    @Comment("边视觉数据快照")
    private String edges;

    @Column(name = "node_configs", columnDefinition = "JSON")
    @Comment("节点业务配置快照")
    private String nodeConfigs;

    @Column(name = "edge_configs", columnDefinition = "JSON")
    @Comment("边业务配置快照")
    private String edgeConfigs;

    @Column(name = "variables", columnDefinition = "JSON")
    @Comment("变量定义快照")
    private String variables;

    @Column(name = "viewport", columnDefinition = "JSON")
    @Comment("视口快照")
    private String viewport;

    @Column(name = "change_log", columnDefinition = "TEXT")
    @Comment("变更说明")
    private String changeLog;

    @Column(name = "created_by")
    @Comment("创建人ID")
    private Long createdBy;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
