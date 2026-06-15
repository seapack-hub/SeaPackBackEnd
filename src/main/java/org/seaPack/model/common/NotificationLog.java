package org.seaPack.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "notification_log")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id")
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "monitor_id")
    @Comment("关联监控记录ID")
    private Long monitorId;

    @Column(name = "stock_code")
    @Comment("股票代码")
    private String stockCode;

    @Column(name = "stock_name")
    @Comment("股票名称")
    private String stockName;

    @Column(name = "stock_id")
    @Comment("关联股票ID")
    private Long stockId;

    @Column(name = "rule_id")
    @Comment("关联阈值规则ID")
    private Long ruleId;

    @Column(name = "trigger_yield")
    @Comment("触发时的股息率(%)")
    private BigDecimal triggerYield;

    @Column(name = "notify_type")
    @Comment("通知方式：SYSTEM/EMAIL/SMS")
    private String notifyType;

    @Column(name = "status")
    @Comment("状态 0失败 1成功")
    private Integer status;

    @Column(name = "message")
    @Comment("通知内容/错误信息")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;
}
