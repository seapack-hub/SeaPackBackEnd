package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.KnowledgeDocument;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper {

    List<KnowledgeDocument> selectByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    KnowledgeDocument selectById(@Param("id") Long id);

    int insert(KnowledgeDocument document);

    int update(KnowledgeDocument document);

    int updateStatus(@Param("id") Long id,
                     @Param("parseStatus") Integer parseStatus,
                     @Param("vectorStatus") Integer vectorStatus);

    int updateStats(@Param("id") Long id,
                    @Param("chunkCount") Integer chunkCount,
                    @Param("tokenCount") Long tokenCount);

    int deleteById(@Param("id") Long id);

    int deleteByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    int countByKnowledgeId(@Param("knowledgeId") Long knowledgeId);
}
