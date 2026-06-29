package org.seaPack.service.blog;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.blog.BlogArticleMapper;
import org.seaPack.model.blog.BlogArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 博客文章服务
 * <p>提供文章的分页查询、CRUD、状态管理。</p>
 */
@Service
public class BlogArticleService {

    @Autowired
    private BlogArticleMapper articleMapper;

    /**
     * 分页查询文章列表
     *
     * @param category 分类筛选
     * @param status   状态筛选
     * @param keyword  标题/摘要关键词
     * @param isTop    是否置顶
     */
    public PageInfo<BlogArticle> getArticleList(int pageNum, int pageSize,
                                                 String category, Integer status,
                                                 String keyword, Integer isTop) {
        PageHelper.startPage(pageNum, pageSize);
        List<BlogArticle> list = articleMapper.selectArticleList(category, status, keyword, isTop);
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 查询文章详情
     */
    public BlogArticle getArticleById(Long id) {
        return articleMapper.selectArticleById(id);
    }

    /**
     * 新增文章
     */
    @Transactional
    public int insertArticle(BlogArticle article) {
        return articleMapper.insertArticle(article);
    }

    /**
     * 更新文章
     */
    @Transactional
    public int updateArticle(BlogArticle article) {
        return articleMapper.updateArticle(article);
    }

    /**
     * 删除文章
     */
    @Transactional
    public int deleteArticleById(Long id) {
        return articleMapper.deleteArticleById(id);
    }
}
