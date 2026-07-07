package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * Agent 关联知识库实体
 * <p>对应 ai_agent_knowledge 表，一个 Agent 可关联多个知识库。</p>
 */
@Entity
@Data
@Table(name = "ai_agent_knowledge")
public class AgentKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "agent_id")
    @Comment("助手ID")
    private Long agentId;

    @Column(name = "knowledge_id")
    @Comment("知识库ID")
    private Long knowledgeId;

    /** 知识库名称（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String knowledgeName;

    @Column(name = "enabled")
    @Comment("1启用 0禁用")
    private Integer enabled;

    @Column(name = "retrieval_count")
    @Comment("每次检索返回片段数")
    private Integer retrievalCount;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
