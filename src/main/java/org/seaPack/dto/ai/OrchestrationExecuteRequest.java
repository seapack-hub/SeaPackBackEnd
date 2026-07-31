package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 编排执行请求 DTO
 * <p>对应前端 OrchestrationExecuteRequest，用于触发编排的流式执行。</p>
 */
@Data
public class OrchestrationExecuteRequest {

    /** 编排ID */
    private Long orchestrationId;

    /** 用户输入消息 */
    private String message;

    /** 对话历史（用于多轮记忆） */
    private List<Map<String, String>> history;

    /** 上下文变量（可选，用于 input_mapping 引用外部数据） */
    private Map<String, Object> context;

    /** 场景ID（前端传入，用于落库场景维度查询） */
    private Long sceneId;

    /** 对话ID：进入对话界面时生成一次，同一会话的所有轮次共享（前端生成） */
    private String conversationId;

    /** 消息ID：每条消息唯一（前端生成），用于精确定位某一轮对话 */
    private String requestId;
}
