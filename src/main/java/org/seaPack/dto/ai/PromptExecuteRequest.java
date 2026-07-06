package org.seaPack.dto.ai;

import lombok.Data;

import java.util.Map;

/**
 * 提示词模板执行请求 DTO
 * <p>接收模板 ID 和变量值键值对，用于渲染 prompt 并调用 LLM。</p>
 */
@Data
public class PromptExecuteRequest {

    /** 模板 ID */
    private Long templateId;

    /** 变量值键值对，key 对应 template_variable.var_name */
    private Map<String, Object> params;

    /**
     * 用户补充消息
     * 附加在 prompt_template 之后，用于临时输入额外指令
     */
    private String userMessage;
}
