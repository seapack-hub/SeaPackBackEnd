package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 用户每日 Token 使用量实体
 * <p>对应 ai_user_token_usage 表，按天累加记录用户的 Token 消耗。</p>
 */
@Entity
@Data
@Table(name = "ai_user_token_usage")
public class UserTokenUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID（关联 sys_user.id）")
    private Long userId;

    @Column(name = "usage_date", nullable = false)
    @Comment("统计日期")
    private Date usageDate;

    @Column(name = "tokens_used", nullable = false)
    @Comment("当日已用Token数")
    private Long tokensUsed;

    @Column(name = "call_count", nullable = false)
    @Comment("当日调用次数")
    private Integer callCount;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
