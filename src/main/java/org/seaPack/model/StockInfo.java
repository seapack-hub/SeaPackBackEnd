package org.seaPack.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "stock_basic")
public class StockInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    @Comment("主键ID")
    private Long stockId;

    @Column(name = "stock_code")
    @Comment("股票代码，如 600519")
    private String stockCode;

    @Column(name = "stock_name")
    @Comment("股票名称，如 贵州茅台")
    private String stockName;

    @Column(name = "exchange")
    @Comment("交易所，如 SH(沪市), SZ(深市)")
    private String exchange;

    @Column(name = "industry")
    @Comment("所属行业，如 银行、煤炭")
    private String industry;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;

    @Transient
    @Comment("行业名称")
    private String industryName;

    @Transient
    @Comment("交易所名称")
    private String exchangeName;

    @Transient
    @Comment("行业ID集合(含子级)，用于父级筛选时查询全部子级数据")
    private List<String> industryIds;

    @Comment("关键字(查询用)")
    private String keywords;
}
