package org.seaPack.controller.blog;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.blog.BlogArticle;
import org.seaPack.service.blog.BlogArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 博客文章控制器
 * <p>提供文章的增删改查、分页列表和详情接口。</p>
 */
@RestController
@RequestMapping("/blog/articles")
public class BlogArticleController {

    @Autowired
    private BlogArticleService articleService;

    /**
     * 分页查询文章列表
     *
     * @param category 分类key（可选）
     * @param status   状态（可选，0草稿 1已发布）
     * @param keyword  标题/摘要关键词（可选）
     * @param isTop    是否置顶（可选）
     */
    @GetMapping
    public PageInfo<BlogArticle> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isTop) {
        return articleService.getArticleList(pageNum, pageSize, category, status, keyword, isTop);
    }

    /**
     * 查询文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlogArticle> detail(@PathVariable Long id) {
        BlogArticle article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    /**
     * 新增文章
     */
    @PostMapping
    public ResponseEntity<Integer> insert(@RequestBody BlogArticle article) {
        return ResponseEntity.ok(articleService.insertArticle(article));
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<Integer> update(@PathVariable Long id, @RequestBody BlogArticle article) {
        article.setId(id);
        return ResponseEntity.ok(articleService.updateArticle(article));
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.deleteArticleById(id));
    }
}
