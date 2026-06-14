package org.seaPack.dto.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票行情展示 DTO
 * <p>以 stock_basic 为基础，LEFT JOIN stock_realtime_quote 和 stock_dividend，
 * 展示股票基本信息、最新行情、最新分红方案以及计算的股息率、涨跌幅等指标。</p>
 */
@Data
public class StockMarketQuoteDto {

    // ========== stock_basic ==========
    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 交易所 */
    private String exchange;

    /** 交易所名称 */
    private String exchangeName;

    /** 行业 */
    private String industry;

    /** 行业名称 */
    private String industryName;

    // ========== stock_realtime_quote（最新交易日） ==========
    /** 当前最新价 */
    private BigDecimal currentPrice;

    /** 今日开盘价 */
    private BigDecimal openPrice;

    /** 今日最高价 */
    private BigDecimal highPrice;

    /** 今日最低价 */
    private BigDecimal lowPrice;

    /** 动态股息率（后台任务预计算） */
    private BigDecimal dynamicYield;

    /** 交易日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date tradeDate;

    /** 行情更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date quoteUpdateTime;

    // ========== 筛选条件（非展示字段） ==========
    /** 关键字（模糊搜索 stock_code + stock_name） */
    private String keywords;

    // ========== stock_dividend（最近完整自然年分红聚合） ==========
    /** 最近一个完整自然年（如当前 2026 年则取 2025） */
    private Integer latestDividendYear;

    /** 年度每股分红总和（支持一年多次分红累加） */
    private BigDecimal latestCashPerShare;

    // ========== 计算指标 ==========
    /** 涨跌幅(%) = (currentPrice - openPrice) / openPrice * 100 */
    private BigDecimal changePercent;

    /** 年度每股分红总和 */
    private BigDecimal dividendPerShare;

    /** 股息率(%) = totalCashPerYear / currentPrice * 100 */
    private BigDecimal dividendYield;
}
