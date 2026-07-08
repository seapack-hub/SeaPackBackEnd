package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI 知识库主表实体
 * <p>对应 ai_knowledge_base 表，定义知识库基础信息和向量化配置。</p>
 */
@Entity
@Data
@Table(name = "ai_knowledge_base")
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "name")
    @Comment("知识库名称")
    private String name;

    @Column(name = "code")
    @Comment("知识库编码")
    private String code;

    @Column(name = "description")
    @Comment("知识库描述")
    private String description;

    @Column(name = "icon")
    @Comment("图标")
    private String icon;

    @Column(name = "embedding_model")
    @Comment("向量化模型编码")
    private String embeddingModel;

    @Column(name = "chunk_size")
    @Comment("分片大小（字符数）")
    private Integer chunkSize;

    @Column(name = "chunk_overlap")
    @Comment("分片重叠字符数")
    private Integer chunkOverlap;

    @Column(name = "separator")
    @Comment("分片分隔符")
    private String separator;

    @Column(name = "document_count")
    @Comment("文档总数")
    private Integer documentCount;

    @Column(name = "chunk_count")
    @Comment("分片总数")
    private Integer chunkCount;

    @Column(name = "total_tokens")
    @Comment("总 Token 消耗")
    private Long totalTokens;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "created_by")
    @Comment("创建人")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
