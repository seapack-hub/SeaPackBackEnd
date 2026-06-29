package org.seaPack.model.blog;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 博客开源项目实体
 * <p>对应 blog_project 表，展示个人开源项目卡片，
 * 含图标、颜色、链接和排序信息。</p>
 */
@Entity
@Data
@Table(name = "blog_project")
public class BlogProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name")
    @Comment("项目名称")
    private String name;

    @Column(name = "description")
    @Comment("项目描述")
    private String description;

    @Column(name = "icon")
    @Comment("SPIcon图标名")
    private String icon;

    @Column(name = "color")
    @Comment("图标颜色")
    private String color;

    @Column(name = "bg_color")
    @Comment("图标背景色")
    private String bgColor;

    @Column(name = "url")
    @Comment("项目链接")
    private String url;

    @Column(name = "sort")
    @Comment("排序号")
    private Integer sort;

    @Column(name = "status")
    @Comment("状态: 0隐藏 1显示")
    private Integer status;

    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;

    @Column(name = "update_time")
    @Comment("更新时间")
    private Date updateTime;
}
