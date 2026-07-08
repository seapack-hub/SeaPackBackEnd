package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * Agent 关联提示词模板实体
 * <p>对应 ai_agent_prompt 表，一个 Agent 可组合多个提示词模板。</p>
 */
@Entity
@Data
@Table(name = "ai_agent_prompt")
public class AgentPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "agent_id")
    @Comment("助手ID")
    private Long agentId;

    @Column(name = "template_id")
    @Comment("提示词模板ID")
    private Long templateId;

    /** 模板名称（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String templateName;

    /** 模板编码（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String templateCode;

    @Column(name = "is_primary")
    @Comment("1=主模板 0=辅助模板")
    private Integer isPrimary;

    @Column(name = "enabled")
    @Comment("1启用 0禁用")
    private Integer enabled;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
