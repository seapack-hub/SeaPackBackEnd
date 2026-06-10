package org.seaPack.dto.market;

import lombok.Data;

/**
 * A 股标的池基础信息 DTO
 * <p>
 * 对应本地 Parquet 文件中全市场 A 股标的的基本信息，
 * 数据由 Python 脚本通过 TickFlow 获取并保存为 Parquet 格式，
 * 包含股票代码、名称、上市日期、总股本、流通股本、涨跌停价格等字段。
 */
@Data
public class InstrumentDto {

    /** 股票代码（带交易所后缀），如 600000.SH */
    private String symbol;

    /** 交易所代码，SH / SZ / BJ */
    private String exchange;

    /** 纯数字股票代码，如 600000 */
    private String code;

    /** 股票名称，如 浦发银行 */
    private String name;

    /** 地区，固定为 CN */
    private String region;

    /** 标的类型，如 stock */
    private String type;

    /** 证券子类型，如 cn_equity */
    private String extType;

    /** 上市日期，格式 yyyy-MM-dd */
    private String listingDate;

    /** 总股本（股） */
    private Double totalShares;

    /** 流通股本（股） */
    private Double floatShares;

    /** 最小变动价位（元） */
    private Double tickSize;

    /** 涨停价（元） */
    private Double limitUp;

    /** 跌停价（元） */
    private Double limitDown;
}
