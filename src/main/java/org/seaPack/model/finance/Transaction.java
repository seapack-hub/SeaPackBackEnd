package org.seaPack.model.finance;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Integer id;

    @Column(name = "user_id")
    @Comment("用户ID")
    private Integer userId;

    @Column(name = "fund_code")
    @Comment("基金代码")
    private String fundCode;

    @Column(name = "trade_type")
    @Comment("交易类型（申购/赎回/转换等）")
    private String tradeType;

    @Column(name = "trade_date")
    @Comment("交易日期")
    private Date tradeDate;

    @Column(name = "trade_time")
    @Comment("交易时间")
    private Date tradeTime;

    @Column(name = "nav")
    @Comment("交易时的单位净值")
    private BigDecimal nav;

    @Column(name = "shares")
    @Comment("交易份额")
    private BigDecimal shares;

    @Column(name = "amount")
    @Comment("交易金额")
    private BigDecimal amount;

    @Column(name = "fee")
    @Comment("手续费")
    private BigDecimal fee;

    @Column(name = "status")
    @Comment("交易状态")
    private String status;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;

    @Column(name = "note")
    @Comment("备注")
    private String note;
}
