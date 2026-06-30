package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SkillModuleBinding;

import java.util.List;

@Mapper
public interface SkillModuleBindingMapper {

    List<SkillModuleBinding> selectBySkillId(@Param("skillId") Long skillId);

    List<SkillModuleBinding> selectByModuleKey(@Param("moduleKey") String moduleKey,
                                                 @Param("status") Integer status);

    SkillModuleBinding selectById(@Param("id") Long id);

    int insert(SkillModuleBinding binding);

    int update(SkillModuleBinding binding);

    int deleteById(@Param("id") Long id);

    int deleteBySkillId(@Param("skillId") Long skillId);
}
