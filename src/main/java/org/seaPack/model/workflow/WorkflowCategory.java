package org.seaPack.model.workflow;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 工作流分类实体
 * <p>对应 workflow_category 表，管理工作流的分类。</p>
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

    @Column(name = "name")
    @Comment("分类名称")
    private String name;

    @Column(name = "code")
    @Comment("分类编码")
    private String code;

    @Column(name = "description")
    @Comment("分类描述")
    private String description;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
