package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SceneAgentConfig;

import java.util.List;

/**
 * 场景级 Agent 运行配置 Mapper
 */
@Mapper
public interface SceneAgentConfigMapper {

    /** 查询场景的所有 Agent 配置 */
    List<SceneAgentConfig> selectBySceneId(@Param("sceneId") Long sceneId);

    /** 查询场景下指定 Agent 的配置 */
    SceneAgentConfig selectBySceneAndAgent(@Param("sceneId") Long sceneId, @Param("agentId") Long agentId);

    /** 根据 ID 查询配置 */
    SceneAgentConfig selectById(@Param("id") Long id);

    /** 新增配置 */
    int insert(SceneAgentConfig config);

    /** 更新配置 */
    int update(SceneAgentConfig config);

    /** 删除配置 */
    int deleteById(@Param("id") Long id);

    /** 删除场景的所有配置（级联删除用） */
    int deleteBySceneId(@Param("sceneId") Long sceneId);

    /** 删除场景下指定 Agent 的配置 */
    int deleteBySceneAndAgent(@Param("sceneId") Long sceneId, @Param("agentId") Long agentId);

    /** 校验是否已存在（scene_id + agent_id） */
    int countBySceneAndAgent(@Param("sceneId") Long sceneId, @Param("agentId") Long agentId);
}
