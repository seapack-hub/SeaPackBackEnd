package org.seaPack.dto.ai;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 概览统计 DTO
 * <p>今日与昨日的 Token 消耗和调用对比数据。</p>
 */
@Data
public class TokenStatOverview {

    /** 今日调用次数 */
    private Long todayCalls;

    /** 今日 Token 总数 */
    private Long todayTokens;

    /** 今日费用(元) */
    private BigDecimal todayCost;

    /** 今日成功率(%) */
    private BigDecimal successRate;

    /** 昨日调用次数 */
    private Long yesterdayCalls;

    /** 昨日 Token 总数 */
    private Long yesterdayTokens;

    /** 昨日费用(元) */
    private BigDecimal yesterdayCost;

    /** 昨日成功率(%) */
    private BigDecimal yesterdaySuccessRate;
}
