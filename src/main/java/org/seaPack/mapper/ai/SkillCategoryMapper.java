package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SkillCategory;

import java.util.List;

@Mapper
public interface SkillCategoryMapper {

    List<SkillCategory> selectList(@Param("keyword") String keyword,
                                    @Param("status") Integer status);

    SkillCategory selectById(@Param("id") Long id);

    SkillCategory selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(SkillCategory category);

    int update(SkillCategory category);

    int deleteById(@Param("id") Long id);
}
