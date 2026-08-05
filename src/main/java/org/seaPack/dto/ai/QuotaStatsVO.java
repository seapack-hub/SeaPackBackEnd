package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 额度配置统计概览 DTO
 * <p>用于页面顶部数字卡片展示，管理员快速了解额度配置整体情况。</p>
 */
@Data
public class QuotaStatsVO {

    /** 配置了额度的用户总数（user_id 去重） */
    private Long totalUsers;

    /** 启用状态的配置数 */
    private Long enabledCount;

    /** 已超限的用户数 */
    private Long exceededCount;

    /** 已禁用的配置数 */
    private Long disabledCount;
}
