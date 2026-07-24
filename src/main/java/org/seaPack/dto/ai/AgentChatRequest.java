package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Agent 对话请求 DTO
 * <p>接收 Agent ID、用户消息及可选的对话历史。</p>
 */
@Data
public class AgentChatRequest {

    /** Agent ID */
    private Long agentId;

    /** 场景ID（可选，用于场景级配置覆盖） */
    private Long sceneId;

    /** 用户消息 */
    private String message;

    /** 对话历史（可选，用于记忆模式） */
    private List<Map<String, String>> history;
}
