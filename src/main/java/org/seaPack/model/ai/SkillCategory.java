package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI 技能分类实体
 * <p>对应 ai_skill_category 表，如"内容生成"、"数据分析"等技能分组。</p>
 */
@Entity
@Data
@Table(name = "ai_skill_category")
public class SkillCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name")
    @Comment("分类名称，如内容生成、数据分析")
    private String name;

    @Column(name = "code")
    @Comment("分类编码，唯一标识，如 content_gen")
    private String code;

    @Column(name = "icon")
    @Comment("分类图标SVG文件名")
    private String icon;

    @Column(name = "description")
    @Comment("分类描述")
    private String description;

    @Column(name = "sort_order")
    @Comment("排序号，越小越靠前")
    private Integer sortOrder;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
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
}
