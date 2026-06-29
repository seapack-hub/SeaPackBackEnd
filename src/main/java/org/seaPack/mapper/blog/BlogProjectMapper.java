package org.seaPack.mapper.blog;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.blog.BlogProject;

import java.util.List;

/**
 * 博客开源项目 Mapper
 * <p>提供 blog_project 表的分页查询及 CRUD。</p>
 */
@Mapper
public interface BlogProjectMapper {

    /**
     * 分页查询项目列表
     *
     * @param status  状态筛选（可选）
     * @param keyword 名称/描述关键词（可选）
     * @return 项目列表（按 sort ASC, create_time DESC 排序）
     */
    List<BlogProject> selectProjectList(@Param("status") Integer status,
                                         @Param("keyword") String keyword);

    /**
     * 根据主键查询项目
     */
    BlogProject selectProjectById(@Param("id") Long id);

    /**
     * 新增项目
     */
    int insertProject(BlogProject project);

    /**
     * 更新项目
     */
    int updateProject(BlogProject project);

    /**
     * 根据主键删除项目
     */
    int deleteProjectById(@Param("id") Long id);
}
