package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SkillDebugLog;

import java.util.List;

@Mapper
public interface SkillDebugLogMapper {

    List<SkillDebugLog> selectList(@Param("skillId") Long skillId, @Param("status") String status);

    SkillDebugLog selectById(@Param("id") Long id);

    int insert(SkillDebugLog log);

    int deleteById(@Param("id") Long id);
}
