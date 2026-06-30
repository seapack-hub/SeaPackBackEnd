package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SkillParam;

import java.util.List;

@Mapper
public interface SkillParamMapper {

    List<SkillParam> selectBySkillId(@Param("skillId") Long skillId);

    SkillParam selectById(@Param("id") Long id);

    int insert(SkillParam param);

    int update(SkillParam param);

    int deleteById(@Param("id") Long id);

    int deleteBySkillId(@Param("skillId") Long skillId);
}
