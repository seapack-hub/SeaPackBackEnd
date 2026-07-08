package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 趋势数据 DTO
 * <p>按天聚合的 Token 消耗趋势。</p>
 */
@Data
public class TokenTrendItem {

    /** 日期 YYYY-MM-DD */
    private String date;

    /** 输入 Token 数 */
    private Long tokensInput;

    /** 输出 Token 数 */
    private Long tokensOutput;

    /** 总 Token 数 */
    private Long tokensTotal;

    /** 调用次数 */
    private Long callCount;
}
