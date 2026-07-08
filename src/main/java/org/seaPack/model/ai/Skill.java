package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * AI 技能定义实体（核心表）
 * <p>对应 ai_skill 表，定义了一个可配置的 AI 工具，包含提示词模板、LLM 参数等。</p>
 */
@Entity
@Data
@Table(name = "ai_skill")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "category_id")
    @Comment("所属分类ID")
    private Long categoryId;

    /** 所属分类名称（非数据库字段，关联查询填充） */
    @Transient
    private String categoryName;

    @Column(name = "name")
    @Comment("技能名称，如文章AI写作助手")
    private String name;

    @Column(name = "code")
    @Comment("技能编码，唯一标识，用于前端路由匹配")
    private String code;

    @Column(name = "icon")
    @Comment("技能图标SVG文件名")
    private String icon;

    @Column(name = "description")
    @Comment("技能描述")
    private String description;

    @Column(name = "prompt_template")
    @Comment("系统提示词模板，支持 {{variable}} 插值")
    private String promptTemplate;

    @Column(name = "temperature")
    @Comment("LLM温度参数 0-2")
    private BigDecimal temperature;

    @Column(name = "max_tokens")
    @Comment("最大输出token数")
    private Integer maxTokens;

    @Column(name = "input_schema", columnDefinition = "JSON")
    @Comment("输入参数JSON Schema定义")
    private String inputSchema;

    @Column(name = "output_format")
    @Comment("输出格式：markdown / json / text / html")
    private String outputFormat;

    @Column(name = "module_key")
    @Comment("关联的模块标识（冗余字段，快速过滤）")
    private String moduleKey;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "use_count")
    @Comment("使用次数（统计）")
    private Integer useCount;

    @Column(name = "version")
    @Comment("版本号，如 v1.0.0")
    private String version;

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
