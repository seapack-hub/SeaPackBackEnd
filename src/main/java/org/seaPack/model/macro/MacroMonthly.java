package org.seaPack.model.macro;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 月频宏观指标
 */
@Data
@Entity
@Table(name = "macro_monthly", indexes = {
        @Index(name = "idx_indicator_date", columnList = "indicator_code, stat_date")
})
public class MacroMonthly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Column(name = "stat_date", nullable = false)
    @Comment("统计月份（每月1号）")
    private Date statDate;

    @Column(name = "indicator_code", nullable = false, length = 64)
    @Comment("指标编码: M0, M1, M2, M0_YOY, CPI_YOY, FOREX_USD 等")
    private String indicatorCode;

    @Column(name = "metric_value", nullable = false, precision = 20, scale = 4)
    @Comment("指标值")
    private BigDecimal metricValue;

    @Column(name = "metric_value2", precision = 20, scale = 4)
    @Comment("第二数值（如FOREX_SDR，成对存储）")
    private BigDecimal metricValue2;

    @Column(name = "mom_change", precision = 14, scale = 4)
    @Comment("环比变化")
    private BigDecimal momChange;

    @Column(name = "data_version", nullable = false)
    @Comment("口径版本: 1-旧 2-新(2025 M1口径调整)")
    private Integer dataVersion;

    @Column(name = "source", nullable = false, length = 32)
    @Comment("数据来源: PBC/NBS/SAFE")
    private String source;

    @Column(name = "extra", columnDefinition = "JSON")
    @Comment("扩展字段")
    private String extra;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    private Date updatedAt;
}
