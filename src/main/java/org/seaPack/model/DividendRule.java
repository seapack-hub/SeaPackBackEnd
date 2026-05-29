package org.seaPack.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "dividend_rule")
public class DividendRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "stock_id")
    @Comment("关联股票ID")
    private Long stockId;

    @Column(name = "trigger_rate")
    @Comment("触发股息率阈值, 如 4.00 表示 4%")
    private BigDecimal triggerRate;

    @Column(name = "email_notify")
    @Comment("通知邮箱")
    private String emailNotify;

    @Column(name = "phone_notify")
    @Comment("通知手机号")
    private String phoneNotify;

    @Column(name = "is_active")
    @Comment("是否开启监控 0-关闭 1-开启")
    private Integer isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    @Comment("更新时间")
    private Date updateTime;
}
