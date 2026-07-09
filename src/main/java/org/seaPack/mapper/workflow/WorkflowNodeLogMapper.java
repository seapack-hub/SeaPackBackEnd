package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowNodeLog;

import java.util.List;

@Mapper
public interface WorkflowNodeLogMapper {

    List<WorkflowNodeLog> selectByInstanceId(@Param("instanceId") Long instanceId);

    WorkflowNodeLog selectById(@Param("id") Long id);

    int insert(WorkflowNodeLog log);

    int update(WorkflowNodeLog log);

    int deleteByInstanceId(@Param("instanceId") Long instanceId);
}
