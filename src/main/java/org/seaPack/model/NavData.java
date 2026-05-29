package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "nav_data")
public class NavData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Integer id;

    @Column(name = "fund_code")
    @Comment("基金代码")
    private String fundCode;

    @Column(name = "net_asset_value")
    @Comment("单位净值")
    private BigDecimal netAssetValue;

    @Column(name = "accumulated_nav")
    @Comment("累计净值")
    private BigDecimal accumulatedNav;

    @Column(name = "adjusted_nav")
    @Comment("复权净值")
    private BigDecimal adjustedNav;

    @Column(name = "nav_date")
    @Comment("净值日期")
    private Date navDate;

    @Column(name = "daily_growth_rate")
    @Comment("日增长率(%)")
    private BigDecimal dailyGrowthRate;

    @Column(name = "dividend_per_unit")
    @Comment("每单位分红")
    private BigDecimal dividendPerUnit;

    @Column(name = "adjustment_factor")
    @Comment("复权因子")
    private BigDecimal adjustmentFactor;

    @Column(name = "data_source")
    @Comment("数据来源")
    private String dataSource;

    @Column(name = "last_updated")
    @Comment("最后更新时间")
    private Date lastUpdated;
}
