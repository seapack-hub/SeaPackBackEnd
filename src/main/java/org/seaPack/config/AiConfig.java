package org.seaPack.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Autowired
    private AIProperties aiProperties;

    @Bean
    public ChatLanguageModel chatLanguageModel(){

        // 1. 获取当前激活的提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("未找到 AI 提供商配置: " + providerName);
        }

        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getChatModel())
                .build();
    }

    /**
     * 新增：配置流式对话模型
     * 注意：Bean 的名称必须叫 streamingChatLanguageModel，或者在 @AiService 中显式指定
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        // 1. 同样获取当前激活的提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("未找到 AI 提供商配置: " + providerName);
        }

        if ("aliyun".equalsIgnoreCase(providerName)) {
            return QwenStreamingChatModel.builder()
                    .apiKey(config.getApiKey()) // 阿里云只需要 apiKey (即 DashScope 的 API Key)
                    .modelName(config.getChatModel()) // 例如 "qwen-plus"
                    .build();
        }

        // 2. 使用 OpenAiStreamingChatModel 构建流式模型（同样兼容千问等大模型）
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getChatModel())
                .build();
    }

    /**
     * 将 ChatMemory 注册为 Spring Bean
     * 注意：Bean 的名字默认是方法名 "chatMemory"
     */
    @Bean
    public MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10); // 保留最近 10 轮对话
    }
}
