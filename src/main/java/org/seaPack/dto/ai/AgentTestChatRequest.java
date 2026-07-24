package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 测试对话请求 DTO
 */
@Data
public class AgentTestChatRequest {

    /** Agent ID */
    private Long agentId;

    /** 场景ID（可选，用于场景级配置覆盖） */
    private Long sceneId;

    /** 用户消息 */
    private String message;

    /** 对话历史（可选） */
    private List<Map<String, String>> history;
}
