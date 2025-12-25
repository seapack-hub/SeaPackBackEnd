package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "holdings")
public class Holding {
    @Id
    @Column(name = "id")
    @Comment("持仓记录唯一标识ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    @Comment("关联用户ID")
    private Integer userId;

    @Column(name = "user_name")
    @Comment("用户名称")
    private String userName;

    @Column(name = "fund_code")
    @Comment("关联基金代码")
    private String fundCode;

    @Column(name = "fund_name")
    @Comment("基金名称")
    private String fundName;

    @Column(name = "total_shares")
    @Comment("持有总份额（单位：份）")
    private BigDecimal totalShares;

    @Column(name = "available_shares")
    @Comment("可用份额（可赎回份额")
    private BigDecimal availableShares;

    @Column(name = "frozen_shares")
    @Comment("冻结份额（申购未确认等）")
    private BigDecimal frozenShares;

    @Column(name = "avg_cost_price")
    @Comment("平均成本单价（元/份）")
    private BigDecimal avgCostPrice;

    @Column(name = "total_cost")
    @Comment("持仓总成本（元）")
    private BigDecimal totalCost;

    @Column(name = "cost_principal")
    @Comment("实际投入本金（元）")
    private BigDecimal costPrincipal;

    @Column(name = "last_updated")
    @Comment("最后更新时间")
    private Date lastUpdated;
}