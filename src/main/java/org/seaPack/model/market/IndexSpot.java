package org.seaPack.model.market;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/**
 * 大盘指数实时行情
 */
@Data
@Entity
@Table(name = "index_spot")
public class IndexSpot {

    @Id
    @Column(name = "index_code")
    @Comment("指数代码，如 000001")
    private String indexCode;

    @Column(name = "index_name")
    @Comment("指数名称，如 上证指数")
    private String indexName;

    @Column(name = "latest_price")
    @Comment("最新价")
    private BigDecimal latestPrice;

    @Column(name = "change_pct")
    @Comment("涨跌幅(%)，可能为NULL")
    private BigDecimal changePct;

    @Column(name = "change_amt")
    @Comment("涨跌额，可能为NULL")
    private BigDecimal changeAmt;

    @Column(name = "open_price")
    @Comment("开盘价")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    @Comment("最高价")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    @Comment("最低价")
    private BigDecimal lowPrice;

    @Column(name = "prev_close")
    @Comment("昨收价")
    private BigDecimal prevClose;

    @Column(name = "volume")
    @Comment("成交量(手)")
    private Long volume;

    @Column(name = "turnover")
    @Comment("成交额(元)")
    private BigDecimal turnover;

    @Column(name = "trade_date")
    @Comment("交易日期")
    private String tradeDate;

    @Column(name = "sort_order")
    @Comment("展示排序")
    private Integer sortOrder;
}
