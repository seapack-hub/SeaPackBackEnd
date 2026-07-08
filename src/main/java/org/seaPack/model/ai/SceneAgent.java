package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 场景关联助手实体
 * <p>对应 ai_scene_agent 表，一个场景可关联多个 Agent。</p>
 */
@Entity
@Data
@Table(name = "ai_scene_agent")
public class SceneAgent {

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

    /** 助手名称（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String agentName;

    /** 助手编码（非数据库字段，查询时 JOIN 返回） */
    @Transient
    private String agentCode;

    @Column(name = "is_default")
    @Comment("1=默认助手 0=普通")
    private Integer isDefault;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
