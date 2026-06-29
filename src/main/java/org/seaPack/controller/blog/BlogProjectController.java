package org.seaPack.controller.blog;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.blog.BlogProject;
import org.seaPack.service.blog.BlogProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 博客开源项目控制器
 * <p>提供项目的增删改查和分页列表接口。</p>
 */
@RestController
@RequestMapping("/blog/projects")
public class BlogProjectController {

    @Autowired
    private BlogProjectService projectService;

    /**
     * 分页查询项目列表
     *
     * @param status  状态（可选，0隐藏 1显示）
     * @param keyword 名称/描述关键词（可选）
     */
    @GetMapping
    public PageInfo<BlogProject> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return projectService.getProjectList(pageNum, pageSize, status, keyword);
    }

    /**
     * 查询项目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlogProject> detail(@PathVariable Long id) {
        BlogProject project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }

    /**
     * 新增项目
     */
    @PostMapping
    public ResponseEntity<Integer> insert(@RequestBody BlogProject project) {
        return ResponseEntity.ok(projectService.insertProject(project));
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Integer> update(@PathVariable Long id, @RequestBody BlogProject project) {
        project.setId(id);
        return ResponseEntity.ok(projectService.updateProject(project));
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.deleteProjectById(id));
    }
}
