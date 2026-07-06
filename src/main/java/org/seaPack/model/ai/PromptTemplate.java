package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

/**
 * AI 提示词模板实体
 * <p>对应 ai_prompt_template 表，支持跨技能/助手复用的提示词模板。</p>
 */
@Entity
@Data
@Table(name = "ai_prompt_template")
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name")
    @Comment("模板名称，如股票技术分析模板")
    private String name;

    @Column(name = "code")
    @Comment("模板编码，唯一标识")
    private String code;

    @Column(name = "category")
    @Comment("分类：stock_analysis / content_gen / data_qa / general")
    private String category;

    @Column(name = "content", columnDefinition = "TEXT")
    @Comment("模板正文，支持 {{变量名}} 占位符")
    private String content;

    @Column(name = "description")
    @Comment("模板用途说明")
    private String description;

    @Column(name = "output_format")
    @Comment("期望输出格式：markdown/json/text/html")
    private String outputFormat;

    @Column(name = "version")
    @Comment("版本号")
    private String version;

    @Column(name = "use_count")
    @Comment("被引用次数")
    private Integer useCount;

    @Column(name = "status")
    @Comment("1启用 0禁用")
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

    /** 模板变量定义列表（非数据库字段，联查/请求时填充） */
    @Transient
    private List<TemplateVariable> variables;
}
