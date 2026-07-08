package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 知识库分片实体
 * <p>对应 ai_knowledge_chunk 表，存储文档拆分后的文本分片及向量化关联信息。</p>
 */
@Entity
@Data
@Table(name = "ai_knowledge_chunk")
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "knowledge_id")
    @Comment("所属知识库ID")
    private Long knowledgeId;

    @Column(name = "document_id")
    @Comment("来源文档ID")
    private Long documentId;

    @Column(name = "vector_id")
    @Comment("向量数据库 Record ID")
    private String vectorId;

    @Column(name = "chunk_index")
    @Comment("分片序号（从0开始）")
    private Integer chunkIndex;

    @Column(name = "content", columnDefinition = "TEXT")
    @Comment("分片文本内容")
    private String content;

    @Column(name = "token_count")
    @Comment("分片 Token 数")
    private Integer tokenCount;

    @Column(name = "source_page")
    @Comment("来源页码（PDF适用）")
    private Integer sourcePage;

    @Column(name = "source_section")
    @Comment("来源章节标题")
    private String sourceSection;

    @Column(name = "extra_metadata", columnDefinition = "JSON")
    @Comment("扩展元数据")
    private String extraMetadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
