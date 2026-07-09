package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowInstance;

import java.util.List;

@Mapper
public interface WorkflowInstanceMapper {

    List<WorkflowInstance> selectList(@Param("workflowId") Long workflowId,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    WorkflowInstance selectById(@Param("id") Long id);

    int insert(WorkflowInstance instance);

    int update(WorkflowInstance instance);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateProgress(@Param("id") Long id,
                        @Param("currentNodeId") String currentNodeId,
                        @Param("completedNodes") String completedNodes,
                        @Param("completedCount") Integer completedCount);

    int deleteById(@Param("id") Long id);

    int deleteByInstanceId(@Param("workflowId") Long workflowId);
}
