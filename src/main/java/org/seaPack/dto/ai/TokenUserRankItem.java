package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 用户 Token 消耗排行 DTO
 * <p>按用户维度聚合的 Token 消耗排行数据。</p>
 */
@Data
public class TokenUserRankItem {

    /** 用户ID */
    private Long userId;

    /** 用户名（JOIN sys_user 返回） */
    private String userName;

    /** 调用次数 */
    private Long callCount;

    /** 输入 Token 数 */
    private Long tokensInput;

    /** 输出 Token 数 */
    private Long tokensOutput;

    /** 总 Token 数 */
    private Long tokensTotal;

    /** 总费用(元) */
    private java.math.BigDecimal totalCostYuan;
}
