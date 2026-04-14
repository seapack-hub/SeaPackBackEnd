package org.seaPack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek配置类
 * 用于配置DeepSeek API的相关参数，支持与OpenAI协议兼容的API（如DeepSeek）
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    
    /**
     * DeepSeek API密钥
     * 用于身份验证，需要在DeepSeek平台申请
     */
    private String apiKey;
    
    /**
     * API Base URL地址
     * DeepSeek的API端点地址，如 https://api.deepseek.com
     */
    private String baseUrl;
    
    /**
     * 对话模型名称
     * 用于生成回答的模型，如 deepseek-chat
     */
    private String model;
    
    /**
     * 向量化模型名称
     * 用于将文本转换为向量的模型，如 text-embedding-3-small
     */
    private String embeddingModel;
}