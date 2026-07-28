package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI 功能位置注册实体
 * <p>对应 ai_position 表，注册前端各模块的 AI 功能入口位置，
 * 用于将 AI 场景部署到具体位置（如文章编辑器工具栏、详情页工具栏等）。</p>
 */
@Entity
@Data
@Table(name = "ai_position")
public class AiPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "module_key")
    @Comment("前端模块标识，如 blogsManagement、stockFund")
    private String moduleKey;

    @Column(name = "position_key")
    @Comment("模块内位置标识，如 editor-toolbar、detail-toolbar")
    private String positionKey;

    @Column(name = "label")
    @Comment("位置显示名称，如 文章编辑器工具栏")
    private String label;

    @Column(name = "description")
    @Comment("位置描述")
    private String description;

    @Column(name = "component")
    @Comment("关联前端组件名（可选）")
    private String component;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
