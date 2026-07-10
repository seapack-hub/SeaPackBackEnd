package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

/**
 * 工作流分类实体
 * <p>对应 workflow_category 表，支持树形结构的分类管理。</p>
 */
@Entity
@Data
@Table(name = "workflow_category")
public class WorkflowCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    @Comment("分类名称")
    private String name;

    @Column(name = "parent_id")
    @Comment("父级分类ID，0表示顶级")
    private Long parentId;

    @Column(name = "sort_order")
    @Comment("排序序号")
    private Integer sortOrder;

    @Column(name = "status")
    @Comment("状态: 1=启用, 0=禁用")
    private Integer status;

    @Column(name = "created_by")
    @Comment("创建人ID")
    private Long createdBy;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;

    // ===== 非数据库字段（树形结构辅助） =====

    /** 子分类列表 */
    @Transient
    @Comment("子分类列表")
    private List<WorkflowCategory> children;

    /** 子分类数量 */
    @Transient
    @Comment("子分类数量")
    private Integer childCount;
}
