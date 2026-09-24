package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

/**
 * 场景编排主表实体
 * <p>对应 ai_scene_orchestration 表，定义场景的编排策略和元信息。
 * 一个场景可配置多个编排，但运行时根据 code 或默认策略选择执行。</p>
 */
@Entity
@Data
@Table(name = "ai_scene_orchestration")
public class SceneOrchestration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "scene_id")
    @Comment("关联场景ID")
    private Long sceneId;

    @Column(name = "name")
    @Comment("编排名称")
    private String name;

    @Column(name = "code")
    @Comment("编排编码（场景内唯一）")
    private String code;

    @Column(name = "description")
    @Comment("编排描述")
    private String description;

    @Column(name = "strategy")
    @Comment("执行策略：sequential-顺序 | parallel-并行 | supervisor-总控调度 | crew-角色协作 | dynamic-动态规划")
    private String strategy;

    @Column(name = "supervisor_agent_id")
    @Comment("Supervisor Agent ID，有值时由该Agent动态调度，无值时按步骤执行")
    private Long supervisorAgentId;

    @Column(name = "max_rounds")
    @Comment("Agent间最大协作轮次（防止死循环），默认5")
    private Integer maxRounds;

    @Column(name = "context_strategy")
    @Comment("上下文传递策略：text_only-纯文本 | structured-结构化JSON | shared_state-共享状态对象")
    private String contextStrategy;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

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

    /** 编排步骤列表（非数据库字段，查询时填充） */
    @Transient
    private List<SceneOrchestrationStep> steps;
}
