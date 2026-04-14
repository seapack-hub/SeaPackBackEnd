package org.seaPack.dto;

import lombok.Data;

/**
 * 文档入库请求DTO
 * 用于接收文档入库接口的请求参数
 */
@Data
public class IngestRequest {
    
    /**
     * 命名空间
     * 用于隔离不同知识库，相同namespace的文档会存储在一起
     */
    private String namespace;
    
    /**
     * 文本内容
     * 要入库的文档文本内容，将被分割、向量化后存储
     */
    private String text;
}