package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SceneKnowledge;

import java.util.List;

@Mapper
public interface SceneKnowledgeMapper {

    List<SceneKnowledge> selectBySceneId(@Param("sceneId") Long sceneId);

    SceneKnowledge selectById(@Param("id") Long id);

    int countBySceneIdAndKnowledgeId(@Param("sceneId") Long sceneId, @Param("knowledgeId") Long knowledgeId);

    int insert(SceneKnowledge sceneKnowledge);

    int update(SceneKnowledge sceneKnowledge);

    int deleteById(@Param("id") Long id);

    int deleteBySceneId(@Param("sceneId") Long sceneId);
}
