package org.seaPack.dto.ai;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户额度列表查询 DTO
 * <p>分页查询用户额度列表时返回，含用户名和用量概览。</p>
 */
@Data
public class UserQuotaListDTO {

    /** 配额ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 配额类型：daily / monthly / total */
    private String quotaType;

    /** 额度上限（token数） */
    private Long quotaLimit;

    /** 预警阈值（百分比） */
    private Integer alertThreshold;

    /** 是否启用：0-禁用 1-启用 */
    private Integer isEnabled;

    /** 状态：normal / warning / exceeded / disabled */
    private String status;

    /** 配额周期开始日期 */
    private String startDate;

    /** 配额周期结束日期 */
    private String endDate;

    /** 已用 token 数（从 usage 表实时计算） */
    private Long usedTokens;

    /** 使用率百分比（used / limit * 100） */
    private BigDecimal usagePercent;

    /** 当日调用次数 */
    private Integer callCount;

    /** 创建时间 */
    private String createdAt;
}
