package org.seaPack.dto.ai;

import lombok.Data;

import java.util.Map;

/**
 * 技能调试请求体
 * <p>前端发送技能 ID 和用户填写的测试参数，后端通过 SSE 流式返回调用结果。</p>
 */
@Data
public class SkillTestRequest {

    /** 技能 ID */
    private Long skillId;

    /** 用户填写的测试参数（key-value 平铺结构） */
    private Map<String, Object> params;
}
