package org.seaPack.service.workflow;

import org.seaPack.mapper.workflow.WorkflowCategoryMapper;
import org.seaPack.model.workflow.WorkflowCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流分类管理服务
 * <p>提供分类 CRUD、树形结构构建、子分类校验等功能。</p>
 */
@Service
public class WorkflowCategoryService {

    @Autowired
    private WorkflowCategoryMapper categoryMapper;

    /** 获取所有分类（平铺） */
    public List<WorkflowCategory> getCategoryList() {
        return categoryMapper.selectAll();
    }

    /** 获取分类树形结构 */
    public List<WorkflowCategory> getCategoryTree() {
        List<WorkflowCategory> all = categoryMapper.selectAll();
        return buildTree(all, 0L);
    }

    /** 按父级ID查询子分类 */
    public List<WorkflowCategory> getChildrenByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    /** 根据 ID 查询分类详情 */
    public WorkflowCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }

    /** 新增分类 */
    @Transactional
    public int insertCategory(WorkflowCategory category) {
        // 默认父级为 0（顶级）
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        // 默认启用
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        // 默认排序为 0
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        return categoryMapper.insert(category);
    }

    /** 更新分类 */
    @Transactional
    public int updateCategory(WorkflowCategory category) {
        return categoryMapper.update(category);
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        return categoryMapper.updateStatus(id, status);
    }

    /** 删除分类（校验是否有子分类） */
    @Transactional
    public int deleteCategory(Long id) {
        int childCount = categoryMapper.countChildren(id);
        if (childCount > 0) {
            throw new RuntimeException("该分类下有 " + childCount + " 个子分类，请先删除子分类");
        }
        return categoryMapper.deleteById(id);
    }

    // ===== 内部方法 =====

    /**
     * 递归构建分类树
     * @param all 所有分类（平铺）
     * @param parentId 当前父级ID
     * @return 子树列表
     */
    private List<WorkflowCategory> buildTree(List<WorkflowCategory> all, Long parentId) {
        // 按 parentId 分组
        Map<Long, List<WorkflowCategory>> grouped = all.stream()
                .collect(Collectors.groupingBy(WorkflowCategory::getParentId));

        return buildChildren(grouped, parentId);
    }

    /**
     * 递归构建子节点
     */
    private List<WorkflowCategory> buildChildren(Map<Long, List<WorkflowCategory>> grouped, Long parentId) {
        List<WorkflowCategory> children = grouped.getOrDefault(parentId, new ArrayList<>());
        for (WorkflowCategory child : children) {
            child.setChildren(buildChildren(grouped, child.getId()));
        }
        return children;
    }
}
