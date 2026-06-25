package org.seaPack.controller.ai;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.ImageGenRequest;
import org.seaPack.dto.ai.ImageGenResponse;
import org.seaPack.dto.ai.ImageTaskVO;
import org.seaPack.service.ai.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 图片生成控制器
 * <p>
 * 提供 AI 图片生成的同步和异步两种模式。
 * 同步模式直接返回图片 URL 列表，异步模式先返回 taskId 供前端轮询。
 * </p>
 *
 * <h3>调用方式</h3>
 * <ul>
 *   <li>同步: POST /api/ai/generate-image → 直接返回图片结果</li>
 *   <li>异步: POST /api/ai/generate-image?mode=async → 返回 taskId</li>
 *   <li>轮询: GET /api/ai/image-task/{taskId} → 查询任务状态</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class ImageGenerationController {

    @Autowired
    private ImageGenerationService imageGenerationService;

    /**
     * 生成 AI 图片
     * <p>
     * 支持同步和异步两种模式：
     * <ul>
     *   <li>同步模式（默认）：请求阻塞直到图片生成完毕，直接返回图片 URL 列表</li>
     *   <li>异步模式（mode=async）：立即返回 taskId，前端通过 GET /api/ai/image-task/{taskId} 轮询结果</li>
     * </ul>
     *
     * @param request 图片生成参数（prompt, n, size, style）
     * @param mode    可选参数 "async" 启用异步模式，默认同步
     * @return 同步返回 ImageGenResponse，异步返回 ImageTaskVO
     */
    @PostMapping("/generate-image")
    public ResponseEntity<?> generateImage(
            @RequestBody ImageGenRequest request,
            @RequestParam(defaultValue = "sync") String mode) {

        log.info("图片生成请求: prompt={}, n={}, size={}, style={}, mode={}",
                request.getPrompt(), request.getN(), request.getSize(), request.getStyle(), mode);

        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("prompt 不能为空");
        }

        if ("async".equalsIgnoreCase(mode)) {
            // 异步模式：创建后台任务，返回 taskId
            ImageTaskVO task = imageGenerationService.generateAsync(request);
            log.info("异步任务已创建: taskId={}", task.getTaskId());
            return ResponseEntity.ok(task);
        }

        // 同步模式：直接生成并返回结果
        try {
            ImageGenResponse response = imageGenerationService.generateSync(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("图片生成失败", e);
            return ResponseEntity.status(500).body("图片生成失败: " + e.getMessage());
        }
    }

    /**
     * 查询异步图片生成任务状态
     * <p>前端在异步模式下轮询此接口获取生成结果。</p>
     *
     * @param taskId 任务 ID（由 generateImage 异步模式返回）
     * @return 任务状态，含 processing / completed（带图片列表）/ failed（带错误信息）
     */
    @GetMapping("/image-task/{taskId}")
    public ResponseEntity<?> getImageTask(@PathVariable String taskId) {
        ImageTaskVO task = imageGenerationService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.status(404).body("任务不存在: " + taskId);
        }
        return ResponseEntity.ok(task);
    }
}
