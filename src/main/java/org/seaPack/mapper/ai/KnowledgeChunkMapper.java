package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.KnowledgeChunk;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {

    List<KnowledgeChunk> selectByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    List<KnowledgeChunk> selectByDocumentId(@Param("documentId") Long documentId);

    KnowledgeChunk selectById(@Param("id") Long id);

    int insert(KnowledgeChunk chunk);

    int batchInsert(@Param("list") List<KnowledgeChunk> list);

    int deleteById(@Param("id") Long id);

    int deleteByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    int deleteByDocumentId(@Param("documentId") Long documentId);

    int countByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    /** 语义检索占位：实际需结合向量数据库实现 */
    List<KnowledgeChunk> selectByKeyword(@Param("knowledgeId") Long knowledgeId, @Param("query") String query);
}
