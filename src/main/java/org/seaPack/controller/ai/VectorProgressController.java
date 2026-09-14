package org.seaPack.controller.ai;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.VectorProgressManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 向量化进度 SSE 控制器
 * <p>前端通过 SSE 订阅文档向量化的实时进度。</p>
 *
 * <pre>
 * GET /ai/knowledge/vector-progress?token=xxx  订阅进度流
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
public class VectorProgressController {

    private final VectorProgressManager progressManager;

    /**
     * 订阅向量化进度（SSE）
     *
     * <p>前端在上传文档后，使用 fetch + ReadableStream 连接此端点，
     * 实时接收后端推送的解析、分片、向量化进度。</p>
     *
     * @param taskToken 上传时返回的任务 token
     * @return SSE 发射器
     */
    @GetMapping(value = "/vector-progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeProgress(@RequestParam String taskToken,
                                        HttpServletResponse response) {
        // 与 AiDialogController.orchestration() 保持一致的 SSE 响应头
        // 关键：X-Accel-Buffering: no 告诉 nginx 不要缓冲 SSE 响应
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        log.info("前端订阅向量化进度: token={}", taskToken);
        return progressManager.subscribe(taskToken);
    }

    /**
     * 手动查询文档向量化状态（SSE 断连时的降级方案）
     *
     * @param knowledgeId 知识库 ID
     * @param documentId  文档 ID
     * @return 文档当前状态
     */
    @GetMapping("/vector-status")
    public ResponseEntity<?> getVectorStatus(
            @RequestParam Long knowledgeId,
            @RequestParam Long documentId) {
        // 简单返回，前端轮询用
        return ResponseEntity.ok(Map.of(
                "knowledgeId", knowledgeId,
                "documentId", documentId,
                "message", "状态查询接口（备用）"
        ));
    }
}
