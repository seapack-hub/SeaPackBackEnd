package org.seaPack.dto;

import lombok.Data;

/**
 * 问答请求DTO
 * 用于接收问答接口的请求参数
 */
@Data
public class ChatRequest {
    
    /**
     * 命名空间
     * 指定在哪个知识库中检索相关文档
     */
    private String namespace;
    
    /**
     * 用户问题
     * 需要RAG系统回答的问题
     */
    private String question;
}