package org.seaPack.service.workflow;

import org.seaPack.mapper.workflow.WorkflowCategoryMapper;
import org.seaPack.model.workflow.WorkflowCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流分类管理服务
 * <p>提供分类 CRUD 及编码唯一性校验等功能。</p>
 */
@Service
public class WorkflowCategoryService {

    @Autowired
    private WorkflowCategoryMapper categoryMapper;

    /** 获取所有分类 */
    public List<WorkflowCategory> getCategoryList() {
        return categoryMapper.selectAll();
    }

    /** 新增分类 */
    @Transactional
    public int insertCategory(WorkflowCategory category) {
        return categoryMapper.insert(category);
    }

    /** 更新分类 */
    @Transactional
    public int updateCategory(WorkflowCategory category) {
        return categoryMapper.update(category);
    }

    /** 删除分类 */
    @Transactional
    public int deleteCategory(Long id) {
        return categoryMapper.deleteById(id);
    }

    /** 校验分类编码是否已存在 */
    public boolean isCategoryCodeDuplicate(String code, Long excludeId) {
        return categoryMapper.countByCode(code, excludeId) > 0;
    }
}
