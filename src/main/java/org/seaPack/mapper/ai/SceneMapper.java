package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.Scene;

import java.util.List;

@Mapper
public interface SceneMapper {

    List<Scene> selectList(@Param("status") Integer status, @Param("keyword") String keyword);

    Scene selectById(@Param("id") Long id);

    Scene selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(Scene scene);

    int update(Scene scene);

    int incrementUseCount(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
