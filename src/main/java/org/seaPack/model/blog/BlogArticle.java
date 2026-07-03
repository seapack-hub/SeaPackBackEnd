package org.seaPack.model.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 博客文章实体
 * <p>对应 blog_article 表，支持 Markdown 正文、HTML 缓存、
 * 分类/标签、封面样式和阅读量统计。</p>
 */
@Entity
@Data
@Table(name = "blog_article")
public class BlogArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "title")
    @Comment("文章标题")
    private String title;

    @Column(name = "summary")
    @Comment("摘要")
    private String summary;

    @Column(name = "content_md")
    @Comment("Markdown正文")
    private String contentMd;

    @Column(name = "content_html")
    @Comment("渲染后的HTML（缓存）")
    private String contentHtml;

    @Column(name = "category")
    @Comment("分类key，关联 dict(dictType=blog_category) 的 dictCode")
    private String category;

    @Column(name = "tag")
    @Comment("标签名")
    private String tag;

    @Column(name = "tag_type")
    @Comment("标签颜色类型: success/warning/danger/info")
    private String tagType;

    @Column(name = "cover_icon")
    @Comment("封面Emoji图标")
    private String coverIcon;

    @Column(name = "cover_color")
    @Comment("封面渐变色")
    private String coverColor;

    @Column(name = "status")
    @Comment("状态: 0草稿 1已发布")
    private Integer status;

    @Column(name = "view_count")
    @Comment("阅读数")
    private Integer viewCount;

    @Column(name = "like_count")
    @Comment("点赞数")
    private Integer likeCount;

    @Column(name = "is_top")
    @Comment("是否置顶: 0否 1是")
    private Integer isTop;

    @Column(name = "sort")
    @Comment("排序号（置顶文章排序用）")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    @Comment("更新时间")
    private Date updateTime;
}
