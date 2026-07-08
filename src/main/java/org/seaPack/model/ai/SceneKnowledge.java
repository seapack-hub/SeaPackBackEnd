package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 场景关联知识库实体
 * <p>对应 ai_scene_knowledge 表，一个场景可关联多个知识库。</p>
 */
@Entity
@Data
@Table(name = "ai_scene_knowledge")
public class SceneKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "scene_id")
    @Comment("场景ID")
    private Long sceneId;

    @Column(name = "knowledge_id")
    @Comment("知识库ID")
    private Long knowledgeId;

    /** 知识库名称（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String knowledgeName;

    @Column(name = "enabled")
    @Comment("1启用 0禁用")
    private Integer enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
