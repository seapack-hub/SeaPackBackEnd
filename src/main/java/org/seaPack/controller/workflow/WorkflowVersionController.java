package org.seaPack.controller.workflow;

import org.seaPack.model.workflow.WorkflowVersion;
import org.seaPack.service.workflow.WorkflowVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流版本管理控制器
 * <p>提供版本快照创建、版本列表/详情查询、版本删除、版本对比、版本回滚等接口。</p>
 */
@RestController
@RequestMapping("/workflows/{workflowId}/versions")
public class WorkflowVersionController {

    @Autowired
    private WorkflowVersionService workflowVersionService;

    /** 版本列表 */
    @GetMapping("/all")
    public List<WorkflowVersion> versionList(@PathVariable Long workflowId) {
        return workflowVersionService.getVersionList(workflowId);
    }

    /** 版本详情 */
    @GetMapping("/detail/{version}")
    public ResponseEntity<WorkflowVersion> versionDetail(@PathVariable Long workflowId, @PathVariable Integer version) {
        WorkflowVersion v = workflowVersionService.getVersionDetail(workflowId, version);
        if (v == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(v);
    }

    /** 获取最新版本 */
    @GetMapping("/latest")
    public ResponseEntity<WorkflowVersion> latestVersion(@PathVariable Long workflowId) {
        WorkflowVersion v = workflowVersionService.getLatestVersion(workflowId);
        if (v == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(v);
    }

    /** 创建版本 */
    @PostMapping("/insert")
    public ResponseEntity<?> createVersion(@PathVariable Long workflowId, @RequestBody Map<String, String> body) {
        try {
            String changeLog = body.get("changeLog");
            WorkflowVersion version = workflowVersionService.createVersion(workflowId, changeLog, getCurrentUserId());
            return ResponseEntity.ok(version);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 删除指定版本 */
    @DeleteMapping("/delete/{version}")
    public ResponseEntity<?> deleteVersion(@PathVariable Long workflowId, @PathVariable Integer version) {
        try {
            workflowVersionService.deleteVersion(workflowId, version);
            return ResponseEntity.ok("删除成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 版本对比 */
    @GetMapping("/compare")
    public ResponseEntity<?> compareVersions(
            @PathVariable Long workflowId,
            @RequestParam Integer versionA,
            @RequestParam Integer versionB) {
        try {
            WorkflowVersionService.VersionCompareResult result =
                    workflowVersionService.compareVersions(workflowId, versionA, versionB);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 回滚到指定版本 */
    @PostMapping("/rollback/{version}")
    public ResponseEntity<?> rollbackVersion(@PathVariable Long workflowId, @PathVariable Integer version) {
        try {
            workflowVersionService.rollbackToVersion(workflowId, version);
            return ResponseEntity.ok("回滚成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
