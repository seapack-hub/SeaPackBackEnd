package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI 场景定义实体
 * <p>对应 ai_scene 表，定义了一个业务场景，可关联多个 Agent 和知识库。</p>
 */
@Entity
@Data
@Table(name = "ai_scene")
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name")
    @Comment("场景名称")
    private String name;

    @Column(name = "code")
    @Comment("场景编码，唯一标识")
    private String code;

    @Column(name = "icon")
    @Comment("图标")
    private String icon;

    @Column(name = "cover_color")
    @Comment("卡片渐变色")
    private String coverColor;

    @Column(name = "description")
    @Comment("场景描述")
    private String description;

    @Column(name = "module_key")
    @Comment("关联前端模块")
    private String moduleKey;

    @Column(name = "is_public")
    @Comment("1公开 0私有")
    private Integer isPublic;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "use_count")
    @Comment("使用次数")
    private Integer useCount;

    @Column(name = "created_by")
    @Comment("创建人ID")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
