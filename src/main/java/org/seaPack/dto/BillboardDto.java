package org.seaPack.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 龙虎榜明细 DTO
 * 对应东方财富数据中心龙虎榜接口返回的字段
 */
@Data
public class BillboardDto {

    /** 交易日期 */
    private String tradeDate;

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 上榜原因 */
    private String explain;

    /** 买入额（元） */
    private BigDecimal buyAmt;

    /** 卖出额（元） */
    private BigDecimal sellAmt;

    /** 净买额（元） */
    private BigDecimal netAmt;
}
