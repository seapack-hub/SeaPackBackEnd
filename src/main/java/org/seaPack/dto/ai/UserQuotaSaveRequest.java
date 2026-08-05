package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 用户额度保存请求 DTO
 * <p>新增或编辑用户额度配置时的请求体。</p>
 */
@Data
public class UserQuotaSaveRequest {

    /** 用户ID（新增时必填，编辑时可选） */
    private Long userId;

    /** 配额类型：daily / monthly / total */
    private String quotaType;

    /** 额度上限（token数），0 表示不限制 */
    private Long quotaLimit;

    /** 预警阈值（百分比，默认 80） */
    private Integer alertThreshold;

    /** 配额周期开始日期，daily 时格式 yyyy-MM-dd（当天），monthly 时 yyyy-MM-01，total 时填 9999-12-31 */
    private String startDate;

    /** 配额周期结束日期，daily / total 时传 null，monthly 时格式 yyyy-MM-dd（月末） */
    private String endDate;
}
