package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowVersion;

import java.util.List;

@Mapper
public interface WorkflowVersionMapper {

    List<WorkflowVersion> selectByWorkflowId(@Param("workflowId") Long workflowId);

    WorkflowVersion selectByVersion(@Param("workflowId") Long workflowId, @Param("version") Integer version);

    /** 查询指定工作流的最新版本 */
    WorkflowVersion selectLatest(@Param("workflowId") Long workflowId);

    int insert(WorkflowVersion version);

    int deleteById(@Param("id") Long id);

    /** 按工作流 ID 删除所有版本 */
    int deleteByWorkflowId(@Param("workflowId") Long workflowId);

    /** 统计指定工作流的版本数量 */
    int countByWorkflowId(@Param("workflowId") Long workflowId);
}
