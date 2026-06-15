package org.seaPack.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 告警通知日志实体
 * <p>记录每次触发阈值时生成的告警，包含触发时的股息率和股价快照。</p>
 */
@Data
@Entity
@Table(name = "alert_log")
public class AlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "rule_id")
    @Comment("触发的监控规则ID")
    private Long ruleId;

    @Column(name = "triggered_rate")
    @Comment("触发时的实际股息率(%)")
    private BigDecimal triggeredRate;

    @Column(name = "triggered_price")
    @Comment("触发时的股价")
    private BigDecimal triggeredPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "sent_time")
    @Comment("发送时间")
    private Date sentTime;
}
