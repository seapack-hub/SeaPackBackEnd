package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 用户使用明细 DTO
 * <p>按日 / 月汇总的用户 token 使用明细。</p>
 */
@Data
public class UserUsageDetailDTO {

    /** 统计日期，格式 yyyy-MM-dd 或 yyyy-MM（月汇总时） */
    private String statDate;

    /** 已用 token 数 */
    private Long tokensUsed;

    /** 调用次数 */
    private Integer callCount;

    /** 剩余额度（查询当日时返回） */
    private Long remainingTokens;
}
