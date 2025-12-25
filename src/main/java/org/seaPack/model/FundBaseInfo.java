package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "fund_base_info", indexes = {
        @Index(name = "idx_fund_code", columnList = "fund_code")
})
public class FundBaseInfo {

    @Id
    @Column(name = "fund_code")
    @Comment("基金代码 行业唯一标识符")
    private String fundCode;

    @Column(name = "fund_name")
    @Comment("基金简称")
    private String fundName;

    @Column(name = "fund_full_name")
    @Comment("基金全称")
    private String fundFullName;

    @Column(name = "fund_type")
    @Comment("基金类型")
    private String fundType;

    @Column(name = "fund_type_name")
    @Comment("基金类型名称 如 股票型, 混合型, 债券型, 货币市场型等")
    private String fundTypeName;

    @Column(name = "management_company")
    @Comment("基金管理公司")
    private String managementCompany;

    @Column(name = "custodian")
    @Comment("基金托管人")
    private String custodian;

    @Column(name = "incept_date")
    @Comment("成立日期")
    private Date inceptDate;

    @Column(name = "issue_share")
    @Comment("成立规模")
    private BigDecimal issueShare;

    @Column(name = "m_fee")
    @Comment("管理费率")
    private BigDecimal mFee;

    @Column(name = "c_fee")
    @Comment("托管费率")
    private BigDecimal cFee;

    @Column(name = "s_fee")
    @Comment("销售服务费率")
    private BigDecimal sFee;

    @Column(name = "latest_asset_size")
    @Comment("最新资产规模(亿元)")
    private BigDecimal latestAssetSize;

    @Column(name = "status")
    @Comment("基金状态")
    private String status;

    @Column(name = "created_at")
    @Comment("记录创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("记录更新时间")
    private Date updatedAt;
}