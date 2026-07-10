package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowCategory;

import java.util.List;

@Mapper
public interface WorkflowCategoryMapper {

    /** 查询所有分类（平铺） */
    List<WorkflowCategory> selectAll();

    /** 按父级ID查询子分类 */
    List<WorkflowCategory> selectByParentId(@Param("parentId") Long parentId);

    /** 根据 ID 查询分类详情 */
    WorkflowCategory selectById(@Param("id") Long id);

    /** 统计某分类下的直接子分类数量 */
    int countChildren(@Param("parentId") Long parentId);

    /** 新增分类 */
    int insert(WorkflowCategory category);

    /** 更新分类 */
    int update(WorkflowCategory category);

    /** 更新启停状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 删除分类 */
    int deleteById(@Param("id") Long id);
}
