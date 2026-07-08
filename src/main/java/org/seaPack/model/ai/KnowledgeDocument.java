package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 知识库文档实体
 * <p>对应 ai_knowledge_document 表，管理上传的文档及其解析/向量化状态。</p>
 */
@Entity
@Data
@Table(name = "ai_knowledge_document")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "knowledge_id")
    @Comment("所属知识库ID")
    private Long knowledgeId;

    @Column(name = "file_name")
    @Comment("原始文件名")
    private String fileName;

    @Column(name = "file_path")
    @Comment("存储路径")
    private String filePath;

    @Column(name = "file_size")
    @Comment("文件大小（字节）")
    private Long fileSize;

    @Column(name = "file_type")
    @Comment("文件类型：txt/pdf/docx/md")
    private String fileType;

    @Column(name = "content_type")
    @Comment("MIME 类型")
    private String contentType;

    @Column(name = "parse_status")
    @Comment("解析状态：0待解析 1解析中 2成功 3失败")
    private Integer parseStatus;

    @Column(name = "vector_status")
    @Comment("向量化状态：0待处理 1处理中 2成功 3失败")
    private Integer vectorStatus;

    @Column(name = "chunk_count")
    @Comment("生成分片数")
    private Integer chunkCount;

    @Column(name = "token_count")
    @Comment("文档总 Token 数")
    private Long tokenCount;

    @Column(name = "error_message")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "extra_metadata", columnDefinition = "JSON")
    @Comment("扩展元数据")
    private String extraMetadata;

    @Column(name = "created_by")
    @Comment("上传人")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("上传时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
