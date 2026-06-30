package org.seaPack.dto.ai;

import lombok.Data;

import java.util.Map;

/**
 * 技能执行请求 DTO
 * <p>接收前端传入的技能参数和执行时的补充用户消息。</p>
 */
@Data
public class SkillExecuteRequest {

    /**
     * 技能参数键值对
     * key 对应 ai_skill_param.param_name，value 为用户填写的值
     */
    private Map<String, Object> params;

    /**
     * 用户补充消息
     * 附加在 prompt_template 之后，用于临时输入额外指令
     */
    private String userMessage;
}
