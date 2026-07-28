package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SceneOrchestrationStep;

import java.util.List;

/**
 * 场景编排步骤 Mapper
 */
@Mapper
public interface SceneOrchestrationStepMapper {

    /** 查询编排的所有步骤（按 step_index 升序） */
    List<SceneOrchestrationStep> selectByOrchestrationId(@Param("orchestrationId") Long orchestrationId);

    /** 根据 ID 查询步骤 */
    SceneOrchestrationStep selectById(@Param("id") Long id);

    /** 校验步骤序号是否重复 */
    int countByOrchAndIndex(@Param("orchestrationId") Long orchestrationId, @Param("stepIndex") Integer stepIndex,
                            @Param("excludeId") Long excludeId);

    /** 新增步骤 */
    int insert(SceneOrchestrationStep step);

    /** 更新步骤（只更新非空字段） */
    int update(SceneOrchestrationStep step);

    /** 删除步骤 */
    int deleteById(@Param("id") Long id);

    /** 删除编排的所有步骤（级联用） */
    int deleteByOrchestrationId(@Param("orchestrationId") Long orchestrationId);

    /** 批量更新步骤序号（排序用） */
    int batchUpdateStepIndex(@Param("orchestrationId") Long orchestrationId,
                              @Param("stepIds") List<Long> stepIds);

    /** 获取编排的最大步骤序号 */
    Integer selectMaxStepIndex(@Param("orchestrationId") Long orchestrationId);
}
