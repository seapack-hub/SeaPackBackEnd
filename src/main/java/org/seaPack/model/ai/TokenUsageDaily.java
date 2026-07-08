package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Token 日统计汇总实体
 * <p>对应 ai_token_usage_daily 表，由定时任务按天聚合生成。</p>
 */
@Entity
@Data
@Table(name = "ai_token_usage_daily")
public class TokenUsageDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "stat_date")
    @Comment("统计日期")
    private Date statDate;

    @Column(name = "model_name")
    @Comment("模型编码")
    private String modelName;

    @Column(name = "agent_id")
    @Comment("Agent ID")
    private Long agentId;

    @Column(name = "skill_id")
    @Comment("技能ID")
    private Long skillId;

    @Column(name = "scene_id")
    @Comment("场景ID")
    private Long sceneId;

    @Column(name = "module_key")
    @Comment("模块标识")
    private String moduleKey;

    @Column(name = "call_count")
    @Comment("调用次数")
    private Integer callCount;

    @Column(name = "success_count")
    @Comment("成功次数")
    private Integer successCount;

    @Column(name = "fail_count")
    @Comment("失败次数")
    private Integer failCount;

    @Column(name = "tokens_input")
    @Comment("输入Token数")
    private Long tokensInput;

    @Column(name = "tokens_output")
    @Comment("输出Token数")
    private Long tokensOutput;

    @Column(name = "tokens_total")
    @Comment("总Token数")
    private Long tokensTotal;

    @Column(name = "total_duration_ms")
    @Comment("总耗时(毫秒)")
    private Long totalDurationMs;

    @Column(name = "total_cost_yuan")
    @Comment("总费用(元)")
    private BigDecimal totalCostYuan;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
