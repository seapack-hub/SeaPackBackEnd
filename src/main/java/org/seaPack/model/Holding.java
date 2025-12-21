package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "holdings")
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String fundCode;

    private BigDecimal totalShares;

    private BigDecimal availableShares;

    private BigDecimal frozenShares;

    private BigDecimal avgCostPrice;

    private BigDecimal totalCost;

    private BigDecimal costPrincipal;

    private Date lastUpdated;
}