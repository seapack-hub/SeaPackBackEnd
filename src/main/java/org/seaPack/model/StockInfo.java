package org.seaPack.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@Entity
@Table(name = "stock_info")
public class StockInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "stock_code")
    @Comment("股票代码, 如 600519")
    private String stockCode;

    @Column(name = "stock_name")
    @Comment("股票名称, 如 贵州茅台")
    private String stockName;

    @Column(name = "exchange")
    @Comment("交易所, 如 SH, SZ")
    private String exchange;

    @Column(name = "is_del")
    @Comment("是否删除 0-否 1-是")
    private Integer isDel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    @Comment("更新时间")
    private Date updateTime;

    @Comment("关键字(查询用)")
    private String keywords;
}
