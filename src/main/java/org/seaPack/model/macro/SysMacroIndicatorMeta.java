package org.seaPack.model.macro;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 宏观指标元数据字典
 */
@Data
@Entity
@Table(name = "sys_macro_indicator_meta")
public class SysMacroIndicatorMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "indicator_code", nullable = false, unique = true, length = 64)
    @Comment("指标编码: M0, CPI_YOY, FOREX_USD 等")
    private String indicatorCode;

    @Column(name = "indicator_name", nullable = false, length = 128)
    @Comment("指标中文名")
    private String indicatorName;

    @Column(name = "frequency", nullable = false, length = 16)
    @Comment("频率: monthly/daily/weekly")
    private String frequency;

    @Column(name = "unit", nullable = false, length = 16)
    @Comment("单位: 万亿元, %, 亿美元")
    private String unit;

    @Column(name = "chart_type", nullable = false, length = 16)
    @Comment("默认图表: line/bar/step")
    private String chartType;

    @Column(name = "chart_color", length = 16)
    @Comment("图表颜色")
    private String chartColor;

    @Column(name = "parent_code", length = 64)
    @Comment("父指标编码（分组展示）")
    private String parentCode;

    @Column(name = "sort_order", nullable = false)
    @Comment("排序序号")
    private Integer sortOrder;

    @Column(name = "status", nullable = false)
    @Comment("状态: 1-启用 0-禁用")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at", updatable = false)
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
