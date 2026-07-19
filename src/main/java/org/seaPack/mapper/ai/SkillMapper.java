package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.Skill;

import java.util.List;

@Mapper
public interface SkillMapper {

    List<Skill> selectList(@Param("categoryId") Long categoryId,
                            @Param("skillType") String skillType,
                            @Param("status") Integer status,
                            @Param("keyword") String keyword);

    List<Skill> selectAll(@Param("status") Integer status);

    Skill selectById(@Param("id") Long id);

    Skill selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int countByCategoryId(@Param("categoryId") Long categoryId);

    int insert(Skill skill);

    int update(Skill skill);

    int incrementUseCount(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
