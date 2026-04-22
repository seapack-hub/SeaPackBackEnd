package org.seaPack.dto;

import lombok.Data;

import java.util.List;

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

    // 新增内部类来映射 messages 数组中的对象
    @Data
    public static class MessageDTO {
        /**
         * 角色
         * 区分消息来源（如用户、AI助手、系统）
         */
        private String role;
        /**
         * 内容
         * 角色发送的实际文本内容，传递用户提问、AI回复或系统提示等具体信息
         */
        private String content;
    }

    // 接收前端传来的 messages 列表
    private List<MessageDTO> messages;
}