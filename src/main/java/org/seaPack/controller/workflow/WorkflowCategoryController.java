package org.seaPack.controller.workflow;

import org.seaPack.model.workflow.WorkflowCategory;
import org.seaPack.service.workflow.WorkflowCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流分类管理控制器
 * <p>提供工作流分类 CRUD 及编码唯一性校验等接口。</p>
 */
@RestController
@RequestMapping("/workflows/categories")
public class WorkflowCategoryController {

    @Autowired
    private WorkflowCategoryService workflowCategoryService;

    /** 分类列表 */
    @GetMapping("/all")
    public List<WorkflowCategory> categoryList() {
        return workflowCategoryService.getCategoryList();
    }

    /** 新增分类 */
    @PostMapping("/insert")
    public ResponseEntity<?> insertCategory(@RequestBody WorkflowCategory category) {
        if (workflowCategoryService.isCategoryCodeDuplicate(category.getCode(), null)) {
            return ResponseEntity.badRequest().body("分类编码已存在: " + category.getCode());
        }
        workflowCategoryService.insertCategory(category);
        return ResponseEntity.ok(category);
    }

    /** 编辑分类 */
    @PostMapping("/update")
    public ResponseEntity<?> updateCategory(@RequestBody WorkflowCategory category) {
        if (category.getId() == null) {
            return ResponseEntity.badRequest().body("分类 ID 不能为空");
        }
        if (category.getCode() != null && workflowCategoryService.isCategoryCodeDuplicate(category.getCode(), category.getId())) {
            return ResponseEntity.badRequest().body("分类编码已存在: " + category.getCode());
        }
        workflowCategoryService.updateCategory(category);
        return ResponseEntity.ok(category);
    }

    /** 删除分类 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        workflowCategoryService.deleteCategory(id);
        return ResponseEntity.ok("删除成功");
    }
}
