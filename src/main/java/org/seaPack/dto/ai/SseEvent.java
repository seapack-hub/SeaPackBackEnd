package org.seaPack.dto.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * SSE 事件协议工具类
 * <p>统一所有 AI 对话端点的 SSE 事件类型和发送方式。</p>
 */
@Slf4j
public class SseEvent {

    // ===== 事件类型常量 =====
    /** 步骤开始 */
    public static final String TYPE_STEP_START = "step_start";
    /** 步骤完成 */
    public static final String TYPE_STEP_DONE = "step_done";
    /** 步骤进度更新 */
    public static final String TYPE_STEP_PROGRESS = "step_progress";
    /** 步骤详细信息 */
    public static final String TYPE_STEP_DETAIL = "step_detail";
    /** 步骤异常 */
    public static final String TYPE_STEP_ERROR = "step_error";
    /** 内容块（流式 token） */
    public static final String TYPE_CONTENT = "content";
    /** 对话完成 */
    public static final String TYPE_DONE = "done";
    /** 错误 */
    public static final String TYPE_ERROR = "error";
    /** 用户终止 */
    public static final String TYPE_STOP = "stop";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送 SSE 事件
     *
     * @param emitter SSE 发射器
     * @param type    事件类型
     * @param data    事件数据（不含 type 字段，会自动注入）
     */
    public static void send(SseEmitter emitter, String type, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>(data);
            event.put("type", type);
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("发送 SSE 事件失败: type={}, {}", type, e.getMessage());
        }
    }

    /**
     * 发送 SSE 错误事件并关闭连接
     *
     * @param emitter SSE 发射器
     * @param message 错误消息
     */
    public static void sendError(SseEmitter emitter, String message) {
        try {
            Map<String, Object> event = Map.of(
                    "type", TYPE_ERROR,
                    "message", message
            );
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
            emitter.complete();
        } catch (Exception e) {
            log.warn("发送 SSE 错误事件失败: {}", e.getMessage());
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 构建 step_start 事件数据
     */
    public static Map<String, Object> stepStart(int stepIndex, String stepType, String stepName) {
        Map<String, Object> data = new HashMap<>();
        data.put("stepIndex", stepIndex);
        data.put("stepType", stepType);
        data.put("stepName", stepName);
        return data;
    }

    /**
     * 构建 step_done 事件数据
     */
    public static Map<String, Object> stepDone(int stepIndex, String stepType, String stepName,
                                                String status, long durationMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("stepIndex", stepIndex);
        data.put("stepType", stepType);
        data.put("stepName", stepName);
        data.put("status", status);
        data.put("durationMs", durationMs);
        return data;
    }

    /**
     * 构建 content 事件数据
     */
    public static Map<String, Object> content(String text) {
        return Map.of("text", text);
    }

    /**
     * 构建 done 事件数据
     */
    public static Map<String, Object> done(Map<String, Object> extraData) {
        Map<String, Object> data = new HashMap<>(extraData);
        return data;
    }

    /**
     * 构建 stop 事件数据
     */
    public static Map<String, Object> stop(String message, long durationMs) {
        return Map.of("message", message, "durationMs", durationMs);
    }
}
