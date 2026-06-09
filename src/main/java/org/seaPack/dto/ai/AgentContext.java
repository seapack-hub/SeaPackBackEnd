package org.seaPack.dto.ai;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI Agent 上下文
 * <p>持有 SSE 发射器与完成状态，供工具类和 Agent 流程中推送实时进度到前端。</p>
 */
@Data
public class AgentContext {

    private final ResponseBodyEmitter emitter;

    private final AtomicBoolean isCompleted;

    public AgentContext(ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        this.emitter = emitter;
        this.isCompleted = isCompleted;
    }

    /**
     * 安全发送 SSE 进度消息（连接已断开时自动标记完成）
     * @param type    消息类型（如 "content"、"search"、"documents"）
     * @param message 消息内容
     */
    public void sendProgress(String type, String message) {
        if (isCompleted.get()) return;
        try {
            String json = "data: {\"type\": \"" + type + "\", \"message\": \"" + message + "\"}\n\n";
            emitter.send(json, MediaType.TEXT_EVENT_STREAM);
        } catch (Exception e) {
            isCompleted.set(true);
        }
    }
}
