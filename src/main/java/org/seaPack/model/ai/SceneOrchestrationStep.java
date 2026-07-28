package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 场景编排步骤实体
 * <p>对应 ai_scene_orchestration_step 表，定义编排中每一步的执行细节。
 * 包括执行哪个 Agent、输入如何映射、执行条件等。</p>
 */
@Entity
@Data
@Table(name = "ai_scene_orchestration_step")
public class SceneOrchestrationStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "orchestration_id")
    @Comment("关联编排ID")
    private Long orchestrationId;

    @Column(name = "step_index")
    @Comment("步骤序号（从1开始，顺序执行时按此排序）")
    private Integer stepIndex;

    @Column(name = "step_name")
    @Comment("步骤名称（展示用）")
    private String stepName;

    @Column(name = "agent_id")
    @Comment("关联Agent ID")
    private Long agentId;

    /** Agent 名称（非数据库字段，关联查询填充） */
    @Transient
    private String agentName;

    /** Agent 编码（非数据库字段，关联查询填充） */
    @Transient
    private String agentCode;

    @Column(name = "input_mapping")
    @Comment("输入映射：引用上一步输出的表达式")
    private String inputMapping;

    @Column(name = "`condition`")
    @Comment("执行条件表达式")
    private String condition;

    @Column(name = "retry_count")
    @Comment("失败重试次数")
    private Integer retryCount;

    @Column(name = "timeout_ms")
    @Comment("超时时间（毫秒），NULL 不限")
    private Integer timeoutMs;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
