package org.seaPack.dto.ai;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 当前用户额度使用情况 DTO
 * <p>返回当前登录用户的额度和使用情况。</p>
 */
@Data
public class UserQuotaMyDTO {

    /** 配额ID */
    private Long quotaId;

    /** 配额类型：daily / monthly / total */
    private String quotaType;

    /** 额度上限（token数），0 表示不限制 */
    private Long quotaLimit;

    /** 已用 token 数 */
    private Long usedTokens;

    /** 剩余额度 = quotaLimit - usedTokens（total / daily 类型有效） */
    private Long remainingTokens;

    /** 使用率百分比 */
    private BigDecimal usagePercent;

    /** 预警阈值（百分比） */
    private Integer alertThreshold;

    /** 状态：normal / warning / exceeded / disabled */
    private String status;

    /** 配额周期开始日期 */
    private String startDate;

    /** 配额周期结束日期 */
    private String endDate;

    /** 当日调用次数 */
    private Integer callCount;
}
