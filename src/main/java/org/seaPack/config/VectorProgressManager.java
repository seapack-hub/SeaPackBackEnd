package org.seaPack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量化进度推送管理器
 * <p>管理 SSE 连接，支持按 taskToken 订阅向量化进度。</p>
 *
 * <p>使用流程：</p>
 * <ol>
 *     <li>上传/重新处理文档时生成 taskToken，存入 activeTasks</li>
 *     <li>前端通过 SSE 端点订阅该 token 的进度事件</li>
 *     <li>异步向量化任务在各阶段调用 push() 推送进度</li>
 *     <li>完成或失败后调用 complete() 关闭连接</li>
 * </ol>
 */
@Slf4j
@Component
public class VectorProgressManager {

    /** taskToken → SseEmitter，前端订阅后建立映射 */
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /** taskToken → documentId，用于前端匹配文档 */
    private final ConcurrentHashMap<String, Long> tokenDocMap = new ConcurrentHashMap<>();

    /**
     * 注册一个新的进度追踪任务
     *
     * @param taskToken  任务唯一标识
     * @param documentId 关联的文档 ID
     */
    public void register(String taskToken, Long documentId) {
        tokenDocMap.put(taskToken, documentId);
        log.debug("注册进度追踪: token={}, docId={}", taskToken, documentId);
    }

    /**
     * 前端订阅 SSE 连接
     *
     * @param taskToken 任务唯一标识
     * @return SseEmitter，供 Controller 返回
     */
    public SseEmitter subscribe(String taskToken) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 分钟超时
        emitters.put(taskToken, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(taskToken);
            log.debug("SSE 连接关闭: token={}", taskToken);
        });
        emitter.onTimeout(() -> {
            emitters.remove(taskToken);
            log.warn("SSE 连接超时: token={}", taskToken);
        });
        emitter.onError(e -> {
            emitters.remove(taskToken);
            log.error("SSE 连接错误: token={}", taskToken, e);
        });

        log.debug("前端订阅进度: token={}, 当前活跃连接数={}", taskToken, emitters.size());
        return emitter;
    }

    /**
     * 推送进度事件
     *
     * @param taskToken 任务唯一标识
     * @param phase     阶段标识（parsing / splitting / vectorizing / done / error）
     * @param message   进度描述消息
     * @param progress  进度百分比（0-100），-1 表示不确定
     */
    public void push(String taskToken, String phase, String message, int progress) {
        SseEmitter emitter = emitters.get(taskToken);
        if (emitter == null) {
            log.debug("无活跃 SSE 连接，跳过推送: token={}, phase={}", taskToken, phase);
            return;
        }

        Map<String, Object> data = Map.of(
                "phase", phase,
                "message", message,
                "progress", progress,
                "documentId", tokenDocMap.getOrDefault(taskToken, -1L)
        );

        try {
            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(data));
        } catch (Exception e) {
            log.warn("推送进度失败: token={}, error={}", taskToken, e.getMessage());
            emitters.remove(taskToken);
        }
    }

    /**
     * 完成追踪，关闭 SSE 连接
     *
     * @param taskToken 任务唯一标识
     * @param success   是否成功
     * @param message   最终消息
     */
    public void complete(String taskToken, boolean success, String message) {
        SseEmitter emitter = emitters.get(taskToken);
        if (emitter == null) {
            tokenDocMap.remove(taskToken);
            return;
        }

        Map<String, Object> data = Map.of(
                "phase", success ? "done" : "error",
                "message", message,
                "progress", success ? 100 : -1,
                "documentId", tokenDocMap.getOrDefault(taskToken, -1L)
        );

        try {
            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(data));
            emitter.complete();
        } catch (Exception e) {
            log.warn("完成推送失败: token={}", taskToken, e);
            try { emitter.complete(); } catch (Exception ignored) {}
        }

        emitters.remove(taskToken);
        tokenDocMap.remove(taskToken);
        log.info("进度追踪完成: token={}, success={}", taskToken, success);
    }

    /**
     * 获取关联的文档 ID
     */
    public Long getDocumentId(String taskToken) {
        return tokenDocMap.get(taskToken);
    }
}
