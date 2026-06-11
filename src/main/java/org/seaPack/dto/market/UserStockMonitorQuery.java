package org.seaPack.dto.market;

import lombok.Data;

/**
 * 用户监控股票列表查询参数
 * <p>用于分页 + 条件查询监控池。userId 为必填，其余为可选筛选条件。</p>
 */
@Data
public class UserStockMonitorQuery {

    /** 用户 ID（必填） */
    private Long userId;

    /** 股票代码（模糊匹配） */
    private String stockCode;

    /** 股票名称（模糊匹配，关联 stock_basic） */
    private String stockName;

    /** 是否启用 (1-启用, 0-暂停) */
    private Integer isActive;

    /** 页码（默认 1） */
    private Integer pageNum = 1;

    /** 每页条数（默认 10） */
    private Integer pageSize = 10;
}
