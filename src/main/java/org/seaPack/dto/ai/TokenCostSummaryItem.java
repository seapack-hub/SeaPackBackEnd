package org.seaPack.dto.ai;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 费用汇总 DTO
 * <p>按模型聚合的费用汇总数据。</p>
 */
@Data
public class TokenCostSummaryItem {

    /** 模型编码 */
    private String modelName;

    /** 调用次数 */
    private Long callCount;

    /** 输入 Token 数 */
    private Long tokensInput;

    /** 输出 Token 数 */
    private Long tokensOutput;

    /** 总 Token 数 */
    private Long tokensTotal;

    /** 总费用(元) */
    private BigDecimal totalCostYuan;

    /** 平均耗时(毫秒) */
    private Long avgDurationMs;
}
