package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowDefinition;

import java.util.List;

@Mapper
public interface WorkflowDefinitionMapper {

    List<WorkflowDefinition> selectList(@Param("categoryId") Long categoryId,
                                         @Param("status") Integer status,
                                         @Param("keyword") String keyword);

    WorkflowDefinition selectById(@Param("id") Long id);

    WorkflowDefinition selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(WorkflowDefinition definition);

    int update(WorkflowDefinition definition);

    int updateDefinition(@Param("id") Long id,
                          @Param("nodes") String nodes,
                          @Param("edges") String edges,
                          @Param("nodeConfigs") String nodeConfigs,
                          @Param("edgeConfigs") String edgeConfigs,
                          @Param("variables") String variables,
                          @Param("viewport") String viewport);

    int incrementVersion(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
