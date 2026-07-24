package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 场景级助手运行配置实体
 * <p>对应 ai_scene_agent_config 表，同一 Agent 在不同场景可用不同模型/参数，
 * 覆盖优先级：ai_scene_agent_config > ai_agent 默认值。</p>
 */
@Entity
@Data
@Table(name = "ai_scene_agent_config")
public class SceneAgentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "scene_id")
    @Comment("场景ID")
    private Long sceneId;

    @Column(name = "agent_id")
    @Comment("助手ID")
    private Long agentId;

    @Column(name = "model")
    @Comment("覆盖模型编码")
    private String model;

    @Column(name = "temperature")
    @Comment("覆盖温度")
    private BigDecimal temperature;

    @Column(name = "max_tokens")
    @Comment("最大输出 Token")
    private Integer maxTokens;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    @Comment("场景级 System Prompt 追加内容")
    private String systemPrompt;

    @Column(name = "output_format")
    @Comment("输出格式")
    private String outputFormat;

    @Column(name = "context_limit")
    @Comment("上下文窗口上限 Token 数")
    private Integer contextLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
