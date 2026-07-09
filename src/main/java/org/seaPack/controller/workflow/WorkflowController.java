package org.seaPack.controller.workflow;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.workflow.WorkflowDefinition;
import org.seaPack.service.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工作流定义管理控制器
 * <p>提供工作流定义 CRUD、画布数据保存/读取、启停切换等接口。</p>
 */
@RestController
@RequestMapping("/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    /** 分页查询工作流列表 */
    @GetMapping("/page/list")
    public PageInfo<WorkflowDefinition> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return workflowService.getList(pageNum, pageSize, categoryId, status, keyword);
    }

    /** 全量工作流列表 */
    @GetMapping("/all")
    public java.util.List<WorkflowDefinition> all(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return workflowService.getAll(categoryId, status);
    }

    /** 查询工作流详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<WorkflowDefinition> detail(@PathVariable Long id) {
        WorkflowDefinition def = workflowService.getById(id);
        if (def == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(def);
    }

    /** 新增工作流 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody WorkflowDefinition definition) {
        if (workflowService.isCodeDuplicate(definition.getCode(), null)) {
            return ResponseEntity.badRequest().body("工作流编码已存在: " + definition.getCode());
        }
        definition.setCreatedBy(getCurrentUserId());
        workflowService.insert(definition);
        return ResponseEntity.ok(definition);
    }

    /** 编辑工作流 */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody WorkflowDefinition definition) {
        if (definition.getId() == null) {
            return ResponseEntity.badRequest().body("工作流 ID 不能为空");
        }
        if (definition.getCode() != null && workflowService.isCodeDuplicate(definition.getCode(), definition.getId())) {
            return ResponseEntity.badRequest().body("工作流编码已存在: " + definition.getCode());
        }
        workflowService.update(definition);
        return ResponseEntity.ok(definition);
    }

    /** 删除工作流 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        workflowService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /** 复制工作流 */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            WorkflowDefinition copy = workflowService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 启停切换 */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        workflowService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    // ===== 画布定义保存/读取 =====

    /** 保存工作流定义（含画布数据） */
    @PostMapping("/saveDefinition/{id}")
    public ResponseEntity<?> saveDefinition(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            String nodes = data.get("nodes") != null ? data.get("nodes").toString() : null;
            String edges = data.get("edges") != null ? data.get("edges").toString() : null;
            String nodeConfigs = data.get("nodeConfigs") != null ? data.get("nodeConfigs").toString() : null;
            String edgeConfigs = data.get("edgeConfigs") != null ? data.get("edgeConfigs").toString() : null;
            String variables = data.get("variables") != null ? data.get("variables").toString() : null;
            String viewport = data.get("viewport") != null ? data.get("viewport").toString() : null;

            workflowService.saveDefinition(id, nodes, edges, nodeConfigs, edgeConfigs, variables, viewport);
            return ResponseEntity.ok("保存成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 获取工作流定义 */
    @GetMapping("/getDefinition/{id}")
    public ResponseEntity<WorkflowDefinition> getDefinition(@PathVariable Long id) {
        WorkflowDefinition def = workflowService.getDefinition(id);
        if (def == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(def);
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
