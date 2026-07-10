package org.seaPack.controller.workflow;

import org.seaPack.model.workflow.WorkflowCategory;
import org.seaPack.service.workflow.WorkflowCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流分类管理控制器
 * <p>提供工作流分类 CRUD、树形结构查询、子分类查询、启停切换等接口。</p>
 */
@RestController
@RequestMapping("/workflows/categories")
public class WorkflowCategoryController {

    @Autowired
    private WorkflowCategoryService workflowCategoryService;

    /** 分类列表（平铺） */
    @GetMapping("/all")
    public List<WorkflowCategory> categoryList() {
        return workflowCategoryService.getCategoryList();
    }

    /** 分类树形结构 */
    @GetMapping("/tree")
    public List<WorkflowCategory> categoryTree() {
        return workflowCategoryService.getCategoryTree();
    }

    /** 查询子分类 */
    @GetMapping("/children/{parentId}")
    public List<WorkflowCategory> children(@PathVariable Long parentId) {
        return workflowCategoryService.getChildrenByParentId(parentId);
    }

    /** 查询分类详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<WorkflowCategory> detail(@PathVariable Long id) {
        WorkflowCategory category = workflowCategoryService.getById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    /** 新增分类 */
    @PostMapping("/insert")
    public ResponseEntity<?> insertCategory(@RequestBody WorkflowCategory category) {
        category.setCreatedBy(getCurrentUserId());
        workflowCategoryService.insertCategory(category);
        return ResponseEntity.ok(category);
    }

    /** 编辑分类 */
    @PostMapping("/update")
    public ResponseEntity<?> updateCategory(@RequestBody WorkflowCategory category) {
        if (category.getId() == null) {
            return ResponseEntity.badRequest().body("分类 ID 不能为空");
        }
        workflowCategoryService.updateCategory(category);
        return ResponseEntity.ok("操作成功");
    }

    /** 启停切换 */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        workflowCategoryService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    /** 删除分类 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            workflowCategoryService.deleteCategory(id);
            return ResponseEntity.ok("删除成功");
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
