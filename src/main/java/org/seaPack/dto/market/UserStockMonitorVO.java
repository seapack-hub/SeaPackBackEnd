package org.seaPack.dto.market;

import lombok.Data;

/**
 * 用户监控股票列表视图 DTO
 * <p>
 * 由 user_stock_monitor 与 stock_basic、monitor_threshold_config 联合查询得出，
 * thresholds 为 JSON 数组字符串，前端可直接 JSON.parse 解析为阈值列表。
 */
@Data
public class UserStockMonitorVO {

    /** user_stock_monitor 表主键 ID */
    private Long monitorId;

    /** 股票代码 */
    private String stockCode;

    /** 股票名称（从 stock_basic 联表查询） */
    private String stockName;

    /** 是否启用监控 (1-启用, 0-暂停) */
    private Integer isActive;

    /** 用户备注 */
    private String remark;

    /**
     * 阈值列表 JSON 字符串
     * 格式：[{"id":1, "rate":0.0500, "type":"CROSS_UP"}, ...]
     * 无阈值时为 "[]"
     */
    private String thresholds;
}
