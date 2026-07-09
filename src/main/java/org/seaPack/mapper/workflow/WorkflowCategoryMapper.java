package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowCategory;

import java.util.List;

@Mapper
public interface WorkflowCategoryMapper {

    List<WorkflowCategory> selectAll();

    WorkflowCategory selectById(@Param("id") Long id);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(WorkflowCategory category);

    int update(WorkflowCategory category);

    int deleteById(@Param("id") Long id);
}
