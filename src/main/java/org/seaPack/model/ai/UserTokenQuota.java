package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 用户 Token 配额实体
 * <p>对应 ai_user_token_quota 表，配置用户的 Token 使用额度上限。</p>
 */
@Entity
@Data
@Table(name = "ai_user_token_quota")
public class UserTokenQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID（关联 sys_user.id）")
    private Long userId;

    @Column(name = "quota_type", nullable = false)
    @Comment("配额类型：daily-日额度 / monthly-月额度 / total-总额度")
    private String quotaType;

    @Column(name = "quota_limit", nullable = false)
    @Comment("额度上限（token数），0表示不限制")
    private Long quotaLimit;

    @Column(name = "alert_threshold", nullable = false)
    @Comment("预警阈值（百分比，如80表示用量达80%时告警）")
    private Integer alertThreshold;

    @Column(name = "is_enabled", nullable = false)
    @Comment("是否启用：0-禁用 1-启用")
    private Integer isEnabled;

    @Column(name = "status", nullable = false)
    @Comment("状态：normal-正常 / warning-预警 / exceeded-已超限 / disabled-已禁用")
    private String status;

    @Column(name = "start_date")
    @Comment("配额周期开始日期（daily=当天 / monthly=当月1号 / total=9999-12-31）")
    private Date startDate;

    @Column(name = "end_date")
    @Comment("配额周期结束日期（monthly=当月最后一天 / daily、total为NULL）")
    private Date endDate;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
