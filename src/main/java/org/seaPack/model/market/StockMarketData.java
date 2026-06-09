package org.seaPack.model.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "stock_market_data")
public class StockMarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "stock_id")
    @Comment("关联股票ID")
    private Long stockId;

    @Column(name = "current_price")
    @Comment("当前最新价")
    private BigDecimal currentPrice;

    @Column(name = "dividend_per_share")
    @Comment("每股股息(年度)")
    private BigDecimal dividendPerShare;

    @Column(name = "calculated_yield")
    @Comment("计算出的股息率%")
    private BigDecimal calculatedYield;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "data_time")
    @Comment("数据时间")
    private Date dataTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;

    @Comment("股票代码(联表查询)")
    private String stockCode;

    @Comment("股票名称(联表查询)")
    private String stockName;
}
