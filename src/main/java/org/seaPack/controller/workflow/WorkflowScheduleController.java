package org.seaPack.controller.workflow;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.workflow.WorkflowSchedule;
import org.seaPack.service.workflow.WorkflowScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流调度管理控制器
 * <p>提供调度的分页查询、详情、新增、编辑、删除、启停切换、立即触发等接口。</p>
 */
@RestController
@RequestMapping("/workflows/schedules")
public class WorkflowScheduleController {

    @Autowired
    private WorkflowScheduleService workflowScheduleService;

    /** 分页查询调度列表 */
    @GetMapping("/page/list")
    public PageInfo<WorkflowSchedule> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long workflowId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return workflowScheduleService.getPageList(pageNum, pageSize, workflowId, status, keyword);
    }

    /** 查询调度详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<WorkflowSchedule> detail(@PathVariable Long id) {
        WorkflowSchedule schedule = workflowScheduleService.getById(id);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    /** 新增调度 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody WorkflowSchedule schedule) {
        schedule.setCreatedBy(getCurrentUserId());
        workflowScheduleService.insert(schedule);
        return ResponseEntity.ok(schedule);
    }

    /** 编辑调度 */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody WorkflowSchedule schedule) {
        if (schedule.getId() == null) {
            return ResponseEntity.badRequest().body("调度 ID 不能为空");
        }
        workflowScheduleService.update(schedule);
        return ResponseEntity.ok("操作成功");
    }

    /** 删除调度 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        workflowScheduleService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /** 启停切换 */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        workflowScheduleService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    /** 立即执行一次 */
    @PostMapping("/trigger/{id}")
    public ResponseEntity<?> trigger(@PathVariable Long id) {
        try {
            WorkflowSchedule schedule = workflowScheduleService.trigger(id);
            return ResponseEntity.ok(schedule);
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
