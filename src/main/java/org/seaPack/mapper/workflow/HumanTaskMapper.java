package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.HumanTask;

import java.util.Date;
import java.util.List;

@Mapper
public interface HumanTaskMapper {

    /** 分页查询任务列表（支持关键词、任务类型、状态、实例ID筛选） */
    List<HumanTask> selectList(@Param("taskType") String taskType,
                                @Param("status") Integer status,
                                @Param("instanceId") Long instanceId,
                                @Param("keyword") String keyword);

    /** 根据 ID 查询任务详情 */
    HumanTask selectById(@Param("id") Long id);

    /** 查询当前用户待办任务（assigneeIds JSON 包含 userId） */
    List<HumanTask> selectMyPending(@Param("userId") Long userId,
                                     @Param("status") Integer status);

    /** 新增任务 */
    int insert(HumanTask task);

    /** 更新任务 */
    int update(HumanTask task);

    /** 更新任务状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 删除任务 */
    int deleteById(@Param("id") Long id);
}
