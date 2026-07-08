package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 场景柱状图 DTO
 * <p>各场景的调用次数和 Token 消耗。</p>
 */
@Data
public class TokenSceneBarItem {

    /** 场景ID */
    private Long sceneId;

    /** 场景名称 */
    private String sceneName;

    /** 调用次数 */
    private Long callCount;

    /** 总 Token 数 */
    private Long tokensTotal;
}
