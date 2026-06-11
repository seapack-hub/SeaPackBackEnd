package org.seaPack.model.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票实时行情与股息率快照实体
 * <p>由 iTick WebSocket 实时推送写入，含当前价、开盘价、最高/最低价、动态股息率。</p>
 */
@Data
@Entity
@Table(name = "stock_realtime_quote")
public class StockRealtimeQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "stock_code")
    @Comment("股票代码")
    private String stockCode;

    @Column(name = "current_price")
    @Comment("当前最新价")
    private BigDecimal currentPrice;

    @Column(name = "open_price")
    @Comment("今日开盘价")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    @Comment("今日最高价")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    @Comment("今日最低价")
    private BigDecimal lowPrice;

    @Column(name = "dynamic_yield")
    @Comment("动态股息率(%) = 预期每股分红 / current_price")
    private BigDecimal dynamicYield;

    @Column(name = "trade_date")
    @Comment("交易日期")
    private String tradeDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    @Comment("数据更新时间")
    private Date updateTime;
}
