package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 模型占比 DTO
 * <p>各模型的 Token 消耗占比。</p>
 */
@Data
public class TokenModelPieItem {

    /** 模型编码 */
    private String modelName;

    /** 总 Token 数 */
    private Long tokensTotal;

    /** 调用次数 */
    private Long callCount;
}
