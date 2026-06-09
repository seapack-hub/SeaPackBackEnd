package org.seaPack.model.finance;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票分红明细实体
 * <p>
 * 映射 stock_dividend 表，记录每只股票各年份的分红方案明细，
 * 包括现金分红、送转股、预案公告日期、除权除息日等信息。
 * dividendType 和 status 为字典字段，关联 sys_dict 表。
 */
@Data
@Entity
@Table(name = "stock_dividend")
public class StockDividend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "stock_code")
    @Comment("关联股票代码，如 600519")
    private String stockCode;

    @Column(name = "year")
    @Comment("分红所属年份")
    private Integer year;

    @Column(name = "dividend_type")
    @Comment("分红类型：INTERIM-中期分红, FINAL-末期分红/年度分红")
    private String dividendType;

    @Column(name = "cash_per_share")
    @Comment("每股派发现金金额(元)，无现金分红则为0")
    private BigDecimal cashPerShare;

    @Column(name = "bonus_shares_per_10")
    @Comment("每10股送红股数量(股)")
    private BigDecimal bonusSharesPer10;

    @Column(name = "transfer_shares_per_10")
    @Comment("每10股转增股本数量(股)")
    private BigDecimal transferSharesPer10;

    @Column(name = "plan_text")
    @Comment("分红方案原文，如: 10派5元送3股转2股")
    private String planText;

    @Column(name = "announcement_date")
    @Comment("预案公告日期")
    private Date announcementDate;

    @Column(name = "ex_dividend_date")
    @Comment("除权除息日")
    private Date exDividendDate;

    @Column(name = "status")
    @Comment("实施状态：PROPOSED-预案, APPROVED-已批准, IMPLEMENTED-已实施")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    /** 分红类型名称（字典关联，非数据库字段） */
    @Transient
    private String dividendTypeName;

    /** 实施状态名称（字典关联，非数据库字段） */
    @Transient
    private String statusName;

    /** 股票名称（关联查询，非数据库字段） */
    @Transient
    private String stockName;
}
