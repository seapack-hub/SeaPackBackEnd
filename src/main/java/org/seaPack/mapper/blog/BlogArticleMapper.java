package org.seaPack.mapper.blog;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.blog.BlogArticle;

import java.util.List;

/**
 * 博客文章 Mapper
 * <p>提供 blog_article 表的分页查询、CRUD 及状态管理。</p>
 */
@Mapper
public interface BlogArticleMapper {

    /**
     * 分页查询文章列表
     *
     * @param category 分类筛选（可选）
     * @param status   状态筛选（可选）
     * @param keyword  标题/摘要关键词（可选）
     * @param isTop    置顶筛选（可选）
     * @return 文章列表（已按 isTop DESC, sort DESC, create_time DESC 排序）
     */
    List<BlogArticle> selectArticleList(@Param("category") String category,
                                         @Param("status") Integer status,
                                         @Param("keyword") String keyword,
                                         @Param("isTop") Integer isTop);

    /**
     * 根据主键查询文章
     */
    BlogArticle selectArticleById(@Param("id") Long id);

    /**
     * 新增文章
     */
    int insertArticle(BlogArticle article);

    /**
     * 更新文章
     */
    int updateArticle(BlogArticle article);

    /**
     * 根据主键删除文章
     */
    int deleteArticleById(@Param("id") Long id);
}
