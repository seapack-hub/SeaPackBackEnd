package org.seaPack.controller.workflow;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.workflow.WorkflowInstance;
import org.seaPack.model.workflow.WorkflowNodeLog;
import org.seaPack.service.workflow.WorkflowInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流执行实例管理控制器
 * <p>提供工作流执行/调试、实例列表/详情查询、节点日志查询、实例暂停/恢复/取消等接口。</p>
 */
@RestController
@RequestMapping("/workflows")
public class WorkflowInstanceController {

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    /** 执行工作流 */
    @PostMapping("/run/{id}")
    public ResponseEntity<?> run(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            String inputParams = body != null && body.get("inputParams") != null
                    ? body.get("inputParams").toString() : null;
            WorkflowInstance instance = workflowInstanceService.runWorkflow(id, inputParams, "manual", getCurrentUserId());
            return ResponseEntity.ok(instance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 调试工作流 */
    @PostMapping("/debug/{id}")
    public ResponseEntity<?> debug(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            String inputParams = body != null && body.get("inputParams") != null
                    ? body.get("inputParams").toString() : null;
            WorkflowInstance instance = workflowInstanceService.runWorkflow(id, inputParams, "manual", getCurrentUserId());
            return ResponseEntity.ok(instance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 分页查询实例列表 */
    @GetMapping("/instances/page/list")
    public PageInfo<WorkflowInstance> instancePageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long workflowId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return workflowInstanceService.getInstanceList(pageNum, pageSize, workflowId, status, keyword);
    }

    /** 查询实例详情 */
    @GetMapping("/instances/detail/{id}")
    public ResponseEntity<WorkflowInstance> instanceDetail(@PathVariable Long id) {
        WorkflowInstance instance = workflowInstanceService.getInstanceById(id);
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(instance);
    }

    /** 节点执行日志 */
    @GetMapping("/instances/logs/{instanceId}")
    public List<WorkflowNodeLog> nodeLogs(@PathVariable Long instanceId) {
        return workflowInstanceService.getNodeLogs(instanceId);
    }

    /** 取消执行 */
    @PostMapping("/instances/cancel/{instanceId}")
    public ResponseEntity<?> cancelInstance(@PathVariable Long instanceId) {
        workflowInstanceService.cancelInstance(instanceId);
        return ResponseEntity.ok("已取消");
    }

    /** 暂停执行 */
    @PostMapping("/instances/pause/{instanceId}")
    public ResponseEntity<?> pauseInstance(@PathVariable Long instanceId) {
        workflowInstanceService.pauseInstance(instanceId);
        return ResponseEntity.ok("已暂停");
    }

    /** 恢复执行 */
    @PostMapping("/instances/resume/{instanceId}")
    public ResponseEntity<?> resumeInstance(@PathVariable Long instanceId) {
        workflowInstanceService.resumeInstance(instanceId);
        return ResponseEntity.ok("已恢复");
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
