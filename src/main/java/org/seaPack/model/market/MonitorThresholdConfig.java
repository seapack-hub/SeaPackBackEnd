package org.seaPack.model.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 监控阈值配置实体
 * <p>每条记录对应一个监控规则：当股息率达到指定比例且满足触发类型时告警。</p>
 */
@Data
@Entity
@Table(name = "monitor_threshold_config")
public class MonitorThresholdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "monitor_id")
    @Comment("关联 user_stock_monitor 的 ID")
    private Long monitorId;

    @Column(name = "threshold_rate")
    @Comment("阈值比例，如 0.0300 代表 3%")
    private BigDecimal thresholdRate;

    @Column(name = "trigger_type")
    @Comment("触发类型: CROSS_UP-向上突破, CROSS_DOWN-向下跌破")
    private String triggerType;

    @Column(name = "is_active")
    @Comment("是否生效 (1-是, 0-否)")
    private Integer isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
