package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.PromptTemplate;

import java.util.List;

/**
 * 提示词模板 Mapper
 */
@Mapper
public interface PromptTemplateMapper {

    List<PromptTemplate> selectList(@Param("category") String category,
                                     @Param("status") Integer status,
                                     @Param("keyword") String keyword);

    List<PromptTemplate> selectAll();

    PromptTemplate selectById(@Param("id") Long id);

    List<PromptTemplate> selectByIds(@Param("ids") List<Long> ids);

    PromptTemplate selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(PromptTemplate template);

    int update(PromptTemplate template);

    int incrementUseCount(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);
}
