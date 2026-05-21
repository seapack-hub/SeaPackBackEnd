package org.seaPack.dto;

import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class AgentContext {

    private final ResponseBodyEmitter emitter;

    private final AtomicBoolean isCompleted;

    public AgentContext(ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        this.emitter = emitter;
        this.isCompleted = isCompleted;
    }

    // 安全发送消息的封装
    public void sendProgress(String type, String message) {
        if (isCompleted.get()) return;
        try {
            String json = "data: {\"type\": \"" + type + "\", \"message\": \"" + message + "\"}\n\n";
            emitter.send(json, org.springframework.http.MediaType.TEXT_EVENT_STREAM);
        } catch (Exception e) {
            isCompleted.set(true);
        }
    }
}
