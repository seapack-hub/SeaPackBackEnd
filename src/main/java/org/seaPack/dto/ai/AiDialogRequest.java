package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统一 AI 对话请求 DTO
 * <p>覆盖所有 4 种对话模式的请求参数：
 * <ul>
 *   <li>streaming_llm - 流式 LLM 对话</li>
 *   <li>llm_chat - 非流式 LLM 对话</li>
 *   <li>agent_stream - Agent 流式对话</li>
 *   <li>orchestration - 编排对话</li>
 * </ul>
 * </p>
 */
@Data
public class AiDialogRequest {

    // ===== 模式选择 =====
    /** 对话模式：streaming_llm / llm_chat / agent_stream / orchestration */
    private String mode;

    // ===== LLM 通用 =====
    /** 消息列表（OpenAI 格式） */
    private List<ChatRequest.MessageDTO> messages;
    /** 用户问题（备用，messages 为空时使用） */
    private String question;
    /** 知识库命名空间（用于 RAG 检索） */
    private String namespace;

    // ===== Agent 相关 =====
    /** Agent ID */
    private Long agentId;
    /** 场景 ID（可选，用于场景级配置覆盖） */
    private Long sceneId;

    // ===== 编排相关 =====
    /** 编排 ID */
    private Long orchestrationId;
    /** 上下文变量（可选，用于 input_mapping 引用外部数据） */
    private Map<String, Object> context;

    // ===== 通用选项 =====
    /** 对话历史（用于多轮记忆） */
    private List<Map<String, String>> history;
}
