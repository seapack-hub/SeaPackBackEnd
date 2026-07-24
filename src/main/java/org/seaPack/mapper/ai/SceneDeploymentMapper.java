package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SceneDeployment;

import java.util.List;

/**
 * 场景部署配置 Mapper
 */
@Mapper
public interface SceneDeploymentMapper {

    /** 查询场景的所有部署 */
    List<SceneDeployment> selectBySceneId(@Param("sceneId") Long sceneId);

    /** 查询指定模块+位置的所有部署（仅已启用状态） */
    List<SceneDeployment> selectByModuleAndPosition(@Param("moduleKey") String moduleKey,
                                                     @Param("positionKey") String positionKey);

    /** 根据 ID 查询部署 */
    SceneDeployment selectById(@Param("id") Long id);

    /** 新增部署 */
    int insert(SceneDeployment deployment);

    /** 更新部署 */
    int update(SceneDeployment deployment);

    /** 删除部署 */
    int deleteById(@Param("id") Long id);

    /** 删除场景的所有部署（级联删除用） */
    int deleteBySceneId(@Param("sceneId") Long sceneId);

    /** 校验唯一约束（scene_id + module_key + position_key） */
    int countByUnique(@Param("sceneId") Long sceneId,
                       @Param("moduleKey") String moduleKey,
                       @Param("positionKey") String positionKey);
}
