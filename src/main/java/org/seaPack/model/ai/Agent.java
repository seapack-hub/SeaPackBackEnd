package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * AI Agent/助手定义实体
 * <p>对应 ai_agent 表，定义了一个可配置的 AI 助手，包含系统提示词、模型参数、记忆配置等。</p>
 */
@Entity
@Data
@Table(name = "ai_agent")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "name")
    @Comment("助手名称，如\"股票分析师\"")
    private String name;

    @Column(name = "code")
    @Comment("助手编码，唯一标识")
    private String code;

    @Column(name = "avatar")
    @Comment("助手头像（emoji或图片URL）")
    private String avatar;

    @Column(name = "description")
    @Comment("助手描述，展示给用户看")
    private String description;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    @Comment("系统提示词，定义助手角色和行为规则")
    private String systemPrompt;

    @Column(name = "greeting")
    @Comment("开场白，首次对话时自动发送")
    private String greeting;

    @Column(name = "model_code")
    @Comment("默认模型编码")
    private String modelCode;

    @Column(name = "temperature")
    @Comment("模型温度参数 0-2")
    private BigDecimal temperature;

    @Column(name = "max_tokens")
    @Comment("最大输出token数")
    private Integer maxTokens;

    @Column(name = "output_format")
    @Comment("输出格式：markdown/json/text/html")
    private String outputFormat;

    @Column(name = "memory_enabled")
    @Comment("是否开启对话记忆：1是 0否")
    private Integer memoryEnabled;

    @Column(name = "memory_window")
    @Comment("记忆窗口大小（最近N轮对话）")
    private Integer memoryWindow;

    @Column(name = "version")
    @Comment("配置版本号")
    private String version;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "use_count")
    @Comment("使用次数统计")
    private Integer useCount;

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
