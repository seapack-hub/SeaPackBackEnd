package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SceneOrchestration;

import java.util.List;

/**
 * 场景编排 Mapper
 */
@Mapper
public interface SceneOrchestrationMapper {

    /** 查询场景下的所有编排 */
    List<SceneOrchestration> selectBySceneId(@Param("sceneId") Long sceneId);

    /** 根据 ID 查询编排 */
    SceneOrchestration selectById(@Param("id") Long id);

    /** 根据场景ID和编码查询编排 */
    SceneOrchestration selectBySceneAndCode(@Param("sceneId") Long sceneId, @Param("code") String code);

    /** 校验编码是否重复 */
    int countBySceneAndCode(@Param("sceneId") Long sceneId, @Param("code") String code,
                            @Param("excludeId") Long excludeId);

    /** 新增编排 */
    int insert(SceneOrchestration orchestration);

    /** 更新编排（只更新非空字段） */
    int update(SceneOrchestration orchestration);

    /** 删除编排 */
    int deleteById(@Param("id") Long id);

    /** 删除场景的所有编排（级联用） */
    int deleteBySceneId(@Param("sceneId") Long sceneId);
}
