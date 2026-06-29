package org.seaPack.service.blog;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.blog.BlogProjectMapper;
import org.seaPack.model.blog.BlogProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 博客开源项目服务
 * <p>提供项目的分页查询及 CRUD。</p>
 */
@Service
public class BlogProjectService {

    @Autowired
    private BlogProjectMapper projectMapper;

    /**
     * 分页查询项目列表
     *
     * @param status  状态筛选
     * @param keyword 名称/描述关键词
     */
    public PageInfo<BlogProject> getProjectList(int pageNum, int pageSize,
                                                  Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BlogProject> list = projectMapper.selectProjectList(status, keyword);
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 查询项目
     */
    public BlogProject getProjectById(Long id) {
        return projectMapper.selectProjectById(id);
    }

    /**
     * 新增项目
     */
    @Transactional
    public int insertProject(BlogProject project) {
        return projectMapper.insertProject(project);
    }

    /**
     * 更新项目
     */
    @Transactional
    public int updateProject(BlogProject project) {
        return projectMapper.updateProject(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public int deleteProjectById(Long id) {
        return projectMapper.deleteProjectById(id);
    }
}
