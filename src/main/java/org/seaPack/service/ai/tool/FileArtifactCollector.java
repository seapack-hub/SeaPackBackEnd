package org.seaPack.service.ai.tool;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 文件产物收集器
 * <p>
 * 按 SSE 连接（SseEmitter）维度收集技能执行产出的文件（outputType=file），
 * 供编排/Agent 对话完成时在 done 事件中汇总下发（files 字段），
 * 供执行会话落库时写入 traceSnapshot。
 * </p>
 * <p>
 * 使用 WeakHashMap：SSE 连接结束后 emitter 可被 GC，对应条目自动清理，不泄漏内存。
 * 并行编排多线程共用同一 emitter，因此对 store 与列表均做同步。
 * </p>
 */
@Component
public class FileArtifactCollector {

    private final Map<SseEmitter, List<Map<String, Object>>> store =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * 记录一个文件产物
     *
     * @param emitter 当前 SSE 连接
     * @param file    文件信息（url / fileName / fileSize / skillName）
     */
    public void record(SseEmitter emitter, Map<String, Object> file) {
        if (emitter == null || file == null) {
            return;
        }
        store.computeIfAbsent(emitter, k -> Collections.synchronizedList(new ArrayList<>())).add(file);
    }

    /**
     * 取出并清除当前连接收集到的所有文件产物
     *
     * @return 文件列表；无产物时返回 null
     */
    public List<Map<String, Object>> drain(SseEmitter emitter) {
        if (emitter == null) {
            return null;
        }
        List<Map<String, Object>> files = store.remove(emitter);
        if (files == null || files.isEmpty()) {
            return null;
        }
        synchronized (files) {
            return new ArrayList<>(files);
        }
    }
}
