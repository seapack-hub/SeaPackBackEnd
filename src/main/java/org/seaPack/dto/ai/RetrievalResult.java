package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 语义检索结果 DTO
 */
@Data
public class RetrievalResult {

    /** 分片内容 */
    private String content;

    /** 相似度分数 */
    private Double score;

    /** 来源文档名 */
    private String documentName;

    /** 分片ID */
    private Long chunkId;
}
