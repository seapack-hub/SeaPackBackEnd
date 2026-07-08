package org.seaPack.dto.ai;

import lombok.Data;

/**
 * 语义检索请求 DTO
 */
@Data
public class RetrievalRequest {

    /** 检索查询文本 */
    private String query;

    /** 返回片段数 */
    private Integer topK;
}
