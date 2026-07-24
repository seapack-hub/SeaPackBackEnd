package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SceneAgent;

import java.util.List;

@Mapper
public interface SceneAgentMapper {

    List<SceneAgent> selectBySceneId(@Param("sceneId") Long sceneId);

    SceneAgent selectById(@Param("id") Long id);

    int countBySceneIdAndAgentId(@Param("sceneId") Long sceneId, @Param("agentId") Long agentId);

    int insert(SceneAgent sceneAgent);

    int update(SceneAgent sceneAgent);

    int deleteById(@Param("id") Long id);

    int deleteBySceneId(@Param("sceneId") Long sceneId);

    /** 查询所有默认 Agent（is_default = 1），含 Agent 名称和编码 */
    List<SceneAgent> selectDefaultAgents();
}
