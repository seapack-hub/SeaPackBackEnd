package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.KnowledgeBase;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {

    List<KnowledgeBase> selectList(@Param("status") Integer status, @Param("keyword") String keyword);

    KnowledgeBase selectById(@Param("id") Long id);

    KnowledgeBase selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(KnowledgeBase knowledgeBase);

    int update(KnowledgeBase knowledgeBase);

    int updateStats(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
