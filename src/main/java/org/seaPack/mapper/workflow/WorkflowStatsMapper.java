package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowStats;

import java.util.Date;
import java.util.List;

@Mapper
public interface WorkflowStatsMapper {

    /** 按工作流+日期查询统计 */
    List<WorkflowStats> selectByWorkflowAndDate(@Param("workflowId") Long workflowId,
                                                 @Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate);

    /** 按日期范围查询所有工作流统计 */
    List<WorkflowStats> selectByDateRange(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);

    /** 查询热门工作流排行（按总执行次数降序） */
    List<WorkflowStats> selectTopWorkflows(@Param("limit") int limit);

    /** 新增或更新统计（基于 workflow_id + stat_date 唯一键做 upsert） */
    int upsert(WorkflowStats stats);

    /** 删除指定日期之前的统计数据（用于数据清理） */
    int deleteBeforeDate(@Param("beforeDate") Date beforeDate);
}
