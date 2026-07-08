package org.seaPack.dto.ai;

import lombok.Data;

import java.util.Map;

/**
 * 调试执行请求 DTO
 */
@Data
public class SkillDebugRequest {

    /** 输入参数键值对 */
    private Map<String, Object> params;

    /** 用户补充指令 */
    private String userMessage;
}
