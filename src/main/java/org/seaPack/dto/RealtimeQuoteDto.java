package org.seaPack.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * A股实时行情 DTO
 * 对应东方财富实时行情接口返回的字段
 */
@Data
public class RealtimeQuoteDto {

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 最新价格 */
    private BigDecimal latestPrice;

    /** 今开 */
    private BigDecimal openPrice;

    /** 昨收 */
    private BigDecimal preClose;

    /** 最高 */
    private BigDecimal highPrice;

    /** 最低 */
    private BigDecimal lowPrice;

    /** 成交量（股） */
    private Long volume;

    /** 成交额（元） */
    private BigDecimal amount;

    /** 涨停价 */
    private BigDecimal limitUp;

    /** 跌停价 */
    private BigDecimal limitDown;
}
