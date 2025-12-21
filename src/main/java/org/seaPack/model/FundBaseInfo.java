package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "fund_base_info")
public class FundBaseInfo {

    @Id
    private String fundCode;

    private String fundName;

    private String fundFullName;

    private String fundType;

    private String managementCompany;

    private String custodian;

    private Date inceptDate;

    private BigDecimal issueShare;

    private BigDecimal mFee;

    private BigDecimal cFee;

    private BigDecimal sFee;

    private BigDecimal latestAssetSize;

    private String status;

    private Date createdAt;

    private Date updatedAt;
}