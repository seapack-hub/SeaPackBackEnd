package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * Agent 关联技能实体
 * <p>对应 ai_agent_skill 表，一个 Agent 可关联多个技能。</p>
 */
@Entity
@Data
@Table(name = "ai_agent_skill")
public class AgentSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "agent_id")
    @Comment("助手ID")
    private Long agentId;

    @Column(name = "skill_id")
    @Comment("技能ID")
    private Long skillId;

    /** 技能名称（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String skillName;

    /** 技能编码（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String skillCode;

    @Column(name = "enabled")
    @Comment("1启用 0禁用")
    private Integer enabled;

    @Column(name = "is_primary")
    @Comment("1=主技能 0=辅助技能")
    private Integer isPrimary;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
