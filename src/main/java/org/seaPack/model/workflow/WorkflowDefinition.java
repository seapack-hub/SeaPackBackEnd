package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流定义实体
 * <p>对应 workflow_definition 表，存储工作流的完整定义（含 X6 画布数据和业务配置）。</p>
 */
@Entity
@Data
@Table(name = "workflow_definition")
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name")
    @Comment("工作流名称")
    private String name;

    @Column(name = "code")
    @Comment("工作流编码")
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("工作流描述")
    private String description;

    @Column(name = "category_id")
    @Comment("分类ID")
    private Long categoryId;

    /** 分类名称（非数据库字段，关联查询填充） */
    @Transient
    private String categoryName;

    @Column(name = "version")
    @Comment("当前版本号")
    private Integer version;

    @Column(name = "status")
    @Comment("状态: 1=启用, 0=禁用")
    private Integer status;

    @Column(name = "nodes", columnDefinition = "JSON")
    @Comment("节点视觉数据")
    private String nodes;

    @Column(name = "edges", columnDefinition = "JSON")
    @Comment("边视觉数据")
    private String edges;

    @Column(name = "node_configs", columnDefinition = "JSON")
    @Comment("节点业务配置")
    private String nodeConfigs;

    @Column(name = "edge_configs", columnDefinition = "JSON")
    @Comment("边业务配置")
    private String edgeConfigs;

    @Column(name = "variables", columnDefinition = "JSON")
    @Comment("变量定义")
    private String variables;

    @Column(name = "viewport", columnDefinition = "JSON")
    @Comment("画布视口状态")
    private String viewport;

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
