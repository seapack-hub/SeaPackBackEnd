package org.seaPack.dto.market;

import lombok.Data;
import java.math.BigDecimal;

/**
 * A股历史K线行情 DTO
 * 对应东方财富接口返回的K线数据（日K、周K等）
 */
@Data
public class StockHistoryDto {

    /** 交易日期（格式：yyyy-MM-dd） */
    private String tradeDate;

    /** 开盘价 */
    private BigDecimal openPrice;

    /** 收盘价 */
    private BigDecimal closePrice;

    /** 最高价 */
    private BigDecimal highPrice;

    /** 最低价 */
    private BigDecimal lowPrice;

    /** 成交量（股） */
    private Long volume;

    /** 成交额（元） */
    private BigDecimal turnover;

    /** 振幅（%） */
    private BigDecimal amplitude;
}
