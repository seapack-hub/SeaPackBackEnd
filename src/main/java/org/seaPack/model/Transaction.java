package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String fundCode;

    private String tradeType;

    private Date tradeDate;

    private Date tradeTime;

    private BigDecimal nav;

    private BigDecimal shares;

    private BigDecimal amount;

    private BigDecimal fee;

    private String status;

    private Date createdAt;

    private Date updatedAt;

    private String note;
}