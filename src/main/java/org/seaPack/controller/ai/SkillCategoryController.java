package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.ai.SkillCategory;
import org.seaPack.service.ai.SkillCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * AI 技能分类控制器
 * <p>提供分类的增删改查接口，所有接口需携带 JWT Token（除登录接口外）。</p>
 */
@RestController
@RequestMapping("/api/ai/skill-categories")
public class SkillCategoryController {

    @Autowired
    private SkillCategoryService categoryService;

    /**
     * 分页查询分类列表
     *
     * @param keyword 名称/编码关键词（可选）
     * @param status  状态筛选（可选，1启用 0禁用）
     */
    @GetMapping
    public PageInfo<SkillCategory> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return categoryService.getList(pageNum, pageSize, keyword, status);
    }

    /** 查询分类详情 */
    @GetMapping("/{id}")
    public ResponseEntity<SkillCategory> detail(@PathVariable Long id) {
        SkillCategory category = categoryService.getById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    /**
     * 新增分类
     * <p>新增前校验 code 是否唯一。</p>
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody SkillCategory category) {
        if (categoryService.isCodeDuplicate(category.getCode(), null)) {
            return ResponseEntity.badRequest().body("分类编码已存在: " + category.getCode());
        }
        category.setCreatedBy(getCurrentUserId());
        categoryService.insert(category);
        return ResponseEntity.ok(category);
    }

    /**
     * 更新分类
     * <p>更新时校验 code 是否被其他分类占用。</p>
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SkillCategory category) {
        if (category.getCode() != null && categoryService.isCodeDuplicate(category.getCode(), id)) {
            return ResponseEntity.badRequest().body("分类编码已存在: " + category.getCode());
        }
        category.setId(id);
        categoryService.update(category);
        return ResponseEntity.ok(category);
    }

    /**
     * 删除分类
     * <p>删除前检查该分类下是否有引用技能，有则返回错误提示。</p>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            categoryService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     * <p>由 JwtAuthenticationFilter 在请求拦截时写入。</p>
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
