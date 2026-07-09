package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.WorkflowSchedule;

import java.util.List;

@Mapper
public interface WorkflowScheduleMapper {

    /** 分页查询调度列表（支持关键词、状态、工作流ID筛选） */
    List<WorkflowSchedule> selectList(@Param("workflowId") Long workflowId,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    /** 根据 ID 查询调度详情 */
    WorkflowSchedule selectById(@Param("id") Long id);

    /** 新增调度 */
    int insert(WorkflowSchedule schedule);

    /** 更新调度 */
    int update(WorkflowSchedule schedule);

    /** 删除调度 */
    int deleteById(@Param("id") Long id);

    /** 更新启停状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 更新执行信息（上次执行时间、下次执行时间、执行次数） */
    int updateRunInfo(@Param("id") Long id,
                      @Param("lastRunAt") java.util.Date lastRunAt,
                      @Param("nextRunAt") java.util.Date nextRunAt,
                      @Param("runCount") Integer runCount);

    /** 查询需要触发的调度（状态启用且 nextRunAt <= 当前时间） */
    List<WorkflowSchedule> selectReadySchedules();
}
