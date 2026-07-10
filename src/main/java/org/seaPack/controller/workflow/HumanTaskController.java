package org.seaPack.controller.workflow;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.workflow.HumanTask;
import org.seaPack.service.workflow.HumanTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人工任务管理控制器
 * <p>提供人工任务的分页查询、详情、处理（审批/驳回/转办）、待办查询等接口。</p>
 */
@RestController
@RequestMapping("/workflows/humanTasks")
public class HumanTaskController {

    @Autowired
    private HumanTaskService humanTaskService;

    /** 分页查询任务列表 */
    @GetMapping("/page/list")
    public PageInfo<HumanTask> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long instanceId) {
        return humanTaskService.getPageList(pageNum, pageSize, taskType, status, instanceId, keyword);
    }

    /** 查询任务详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<HumanTask> detail(@PathVariable Long id) {
        HumanTask task = humanTaskService.getById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    /** 处理任务（审批/驳回/转办） */
    @PostMapping("/handle/{id}")
    public ResponseEntity<?> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String action = (String) body.get("action");
            String result = body.get("result") != null ? body.get("result").toString() : null;
            String comment = (String) body.get("comment");
            Long delegateTo = body.get("delegateTo") != null
                    ? Long.valueOf(body.get("delegateTo").toString()) : null;

            HumanTask task = humanTaskService.handleTask(id, action, result, comment, delegateTo);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 我的待办任务 */
    @GetMapping("/myPending")
    public PageInfo<HumanTask> myPending(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        return humanTaskService.getMyPending(pageNum, pageSize, userId);
    }

    /** 新增任务（供工作流引擎内部调用） */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody HumanTask task) {
        humanTaskService.insert(task);
        return ResponseEntity.ok(task);
    }

    /** 删除任务 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        humanTaskService.deleteById(id);
        return ResponseEntity.ok("删除成功");
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
