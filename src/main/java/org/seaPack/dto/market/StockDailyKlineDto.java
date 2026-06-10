package org.seaPack.dto.market;

import lombok.Data;

/**
 * A 股日 K 线数据 DTO
 * <p>
 * 对应 TickFlow 下载的 Parquet 文件中的每行记录，
 * 包含股票代码、交易日期、开高低收价格、成交量和成交额等字段。
 */
@Data
public class StockDailyKlineDto {

    /** 股票代码（带交易所后缀），如 600036.SH */
    private String symbol;

    /** 股票名称 */
    private String name;

    /** Unix 时间戳（毫秒） */
    private Long timestamp;

    /** 交易日期，格式 yyyy-MM-dd，如 2024-01-02 */
    private String tradeDate;

    /** 交易时间（与 tradeDate 相同日期，时分秒为零） */
    private String tradeTime;

    /** 开盘价（前复权） */
    private Double open;

    /** 最高价（前复权） */
    private Double high;

    /** 最低价（前复权） */
    private Double low;

    /** 收盘价（前复权） */
    private Double close;

    /** 成交量（股） */
    private Long volume;

    /** 成交额（元） */
    private Double amount;
}
