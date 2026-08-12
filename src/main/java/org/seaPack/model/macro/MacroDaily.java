package org.seaPack.model.macro;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 日频宏观指标
 */
@Data
@Entity
@Table(name = "macro_daily", indexes = {
        @Index(name = "idx_indicator_date", columnList = "indicator_code, stat_date")
})
public class MacroDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Column(name = "stat_date", nullable = false)
    @Comment("交易日期")
    private Date statDate;

    @Column(name = "indicator_code", nullable = false, length = 64)
    @Comment("指标编码: SHIBOR_ON, MARGIN_BUY 等")
    private String indicatorCode;

    @Column(name = "metric_value", nullable = false, precision = 20, scale = 4)
    @Comment("指标值")
    private BigDecimal metricValue;

    @Column(name = "metric_value2", precision = 20, scale = 4)
    @Comment("第二数值")
    private BigDecimal metricValue2;

    @Column(name = "mom_change", precision = 14, scale = 4)
    @Comment("日环比变化")
    private BigDecimal momChange;

    @Column(name = "source", nullable = false, length = 32)
    @Comment("数据来源: CIB/SSE/SZSE")
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
