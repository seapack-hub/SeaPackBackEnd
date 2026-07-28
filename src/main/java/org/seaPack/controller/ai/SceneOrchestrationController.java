package org.seaPack.controller.ai;

import jakarta.servlet.http.HttpServletResponse;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.seaPack.service.ai.OrchestrationExecuteService;
import org.seaPack.service.ai.SceneOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 场景编排控制器
 * <p>提供编排策略和步骤的增删改查、复制、排序等功能。</p>
 */
@RestController
@RequestMapping("/ai/orchestrations")
public class SceneOrchestrationController {

    @Autowired
    private SceneOrchestrationService orchestrationService;

    @Autowired
    private OrchestrationExecuteService executeService;

    // ===== 编排 CRUD =====

    /**
     * 查询场景下的所有编排
     *
     * @param sceneId 场景ID
     * @return 编排列表
     */
    @GetMapping("/list")
    public List<SceneOrchestration> list(@RequestParam Long sceneId) {
        return orchestrationService.getList(sceneId);
    }

    /**
     * 查询编排详情（含步骤）
     *
     * @param id 编排ID
     * @return 编排详情（含步骤列表），不存在返回 404
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<SceneOrchestration> detail(@PathVariable Long id) {
        SceneOrchestration orch = orchestrationService.getDetailWithSteps(id);
        if (orch == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orch);
    }

    /**
     * 新增编排（校验编码唯一性）
     *
     * @param orchestration 编排实体
     * @return 新增后的编排（含自增ID），编码重复返回 400
     */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody SceneOrchestration orchestration) {
        if (orchestrationService.isCodeDuplicate(orchestration.getSceneId(), orchestration.getCode(), null)) {
            return ResponseEntity.badRequest().body("编排编码已存在: " + orchestration.getCode());
        }
        orchestration.setCreatedBy(getCurrentUserId());
        orchestrationService.insert(orchestration);
        return ResponseEntity.ok(orchestration);
    }

    /**
     * 编辑编排（校验编码唯一性）
     *
     * @param orchestration 编排实体（ID必填）
     * @return 更新后的编排，校验失败返回 400
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody SceneOrchestration orchestration) {
        if (orchestration.getId() == null) {
            return ResponseEntity.badRequest().body("编排 ID 不能为空");
        }
        if (orchestration.getCode() != null
                && orchestrationService.isCodeDuplicate(orchestration.getSceneId(), orchestration.getCode(), orchestration.getId())) {
            return ResponseEntity.badRequest().body("编排编码已存在: " + orchestration.getCode());
        }
        orchestrationService.update(orchestration);
        return ResponseEntity.ok(orchestration);
    }

    /**
     * 删除编排（级联删除步骤）
     *
     * @param id 编排ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        orchestrationService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 启停切换
     *
     * @param id   编排ID
     * @param body 请求体，包含 status 字段（1-启用 0-禁用）
     * @return 操作结果
     */
    @PostMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        orchestrationService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    /**
     * 复制编排（含步骤）
     *
     * @param id 源编排ID
     * @return 复制后的编排
     */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            SceneOrchestration copy = orchestrationService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/execute-stream")
    public SseEmitter executeStream(@RequestBody OrchestrationExecuteRequest request,
                                    HttpServletResponse response) {
        // 设置 SSE 响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(600000L);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                executeService.execute(request, emitter);
            } catch (Exception e) {
                try {
                    Map<String, Object> errorEvent = new java.util.HashMap<>();
                    errorEvent.put("type", "error");
                    errorEvent.put("errorMessage", e.getMessage());
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValueAsString(errorEvent), MediaType.APPLICATION_JSON));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });
        executor.shutdown();

        return emitter;
    }

    // ===== 步骤管理 =====

    /**
     * 查询编排的所有步骤
     *
     * @param orchestrationId 编排ID
     * @return 步骤列表
     */
    @GetMapping("/{orchId}/steps")
    public List<SceneOrchestrationStep> getSteps(@PathVariable("orchId") Long orchestrationId) {
        return orchestrationService.getSteps(orchestrationId);
    }

    /**
     * 新增步骤
     *
     * @param orchestrationId 编排ID
     * @param step            步骤实体
     * @return 新增的步骤（含自增ID），序号冲突返回 400
     */
    @PostMapping("/{orchId}/steps")
    public ResponseEntity<?> addStep(@PathVariable("orchId") Long orchestrationId, @RequestBody SceneOrchestrationStep step) {
        try {
            step.setOrchestrationId(orchestrationId);
            orchestrationService.addStep(step);
            return ResponseEntity.ok(step);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新步骤
     *
     * @param orchestrationId 编排ID
     * @param id              步骤ID
     * @param step            要更新的字段
     * @return 更新后的步骤
     */
    @PostMapping("/{orchId}/steps/{id}/update")
    public ResponseEntity<?> updateStep(@PathVariable("orchId") Long orchestrationId,
                                         @PathVariable Long id,
                                         @RequestBody SceneOrchestrationStep step) {
        try {
            step.setId(id);
            step.setOrchestrationId(orchestrationId);
            orchestrationService.updateStep(step);
            return ResponseEntity.ok(step);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除步骤
     *
     * @param orchestrationId 编排ID
     * @param id              步骤ID
     * @return 操作结果
     */
    @PostMapping("/{orchId}/steps/{id}/delete")
    public ResponseEntity<?> deleteStep(@PathVariable("orchId") Long orchestrationId, @PathVariable Long id) {
        orchestrationService.deleteStep(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 批量排序步骤
     *
     * @param orchestrationId 编排ID
     * @param body            请求体，包含 sortedIds（按新排序排列的步骤ID列表）
     * @return 操作结果
     */
    @PostMapping("/{orchId}/steps/sort")
    public ResponseEntity<?> sortSteps(@PathVariable("orchId") Long orchestrationId,
                                        @RequestBody Map<String, List<Long>> body) {
        List<Long> sortedIds = body.get("sortedIds");
        if (sortedIds == null || sortedIds.isEmpty()) {
            return ResponseEntity.badRequest().body("sortedIds 不能为空");
        }
        orchestrationService.sortSteps(orchestrationId, sortedIds);
        return ResponseEntity.ok("排序成功");
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     *
     * @return 用户 ID，未登录返回 null
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
