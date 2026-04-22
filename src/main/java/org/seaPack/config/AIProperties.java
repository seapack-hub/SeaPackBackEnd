package org.seaPack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data // Lombok 自动生成 Getter/Setter/ToString
@Component
@ConfigurationProperties(prefix = "ai") // 对应配置文件中的 ai.*
public class AIProperties {

    /**
     * 当前激活的 AI 提供商名称
     * 对应配置文件中的: ai.active-provider (或 ai.activeProvider)
     * 例如: "deepseek", "aliyun", "baidu"
     */
    private String activeProvider;

    /**
     * 所有 AI 提供商的配置集合
     * 键(Key)为提供商名称，值(Value)为具体的配置对象
     * 对应配置文件中的: ai.providers.<provider-name>.*
     */
    private Map<String, ProviderConfig> providers;

    /**
     * 单个 AI 提供商的具体配置结构
     */
    @Data // 内部类也需要 @Data
    public static class ProviderConfig {
        /**
         * 调用 AI 服务所需的 API 密钥 (Secret Key)
         * 对应配置文件中的: ai.providers.<name>.api-key
         */
        private String apiKey;
        /**
         * AI 服务的接口基础地址
         * 对应配置文件中的: ai.providers.<name>.base-url
         * 例如: "https://api.deepseek.com/v1"
         */
        private String baseUrl;

        /**
         * 聊天模型名称
         * 对应配置文件中的: ai.providers.<name>.chat-model
         * 例如: "gpt-3.5-turbo", "gpt-4"
         */
        private String chatModel;

        /**
         * 向量化/嵌入模型名称
         * 对应配置文件中的: ai.providers.<name>.embedding-model
         * 用于文本向量化处理，例如: "text-embedding-ada-002"
         */
        private String embeddingModel;
    }
}
