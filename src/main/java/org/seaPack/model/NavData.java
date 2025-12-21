package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "nav_data")
public class NavData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fundCode;

    private BigDecimal netAssetValue;

    private BigDecimal accumulatedNav;

    private BigDecimal adjustedNav;

    private Date navDate;

    private BigDecimal dailyGrowthRate;

    private BigDecimal dividendPerUnit;

    private BigDecimal adjustmentFactor;

    private String dataSource;

    private Date lastUpdated;

}