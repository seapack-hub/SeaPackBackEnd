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
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 模型配置
 * <p>根据配置文件中的激活提供商动态创建 ChatLanguageModel / StreamingChatLanguageModel / ChatMemory，
 * 供 @AiService（Assistant 接口）自动装配。</p>
 */
@Configuration
@EnableAsync
public class AiConfig {

    @Autowired
    private AIProperties aiProperties;

    /**
     * 同步对话模型
     * <p>使用 OpenAiChatModel（兼容 OpenAI 协议的所有服务，如 DeepSeek、通义千问等）。</p>
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(){

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
     * 流式对话模型
     * <p>阿里云（通义千问）使用 QwenStreamingChatModel，其余使用 OpenAiStreamingChatModel。</p>
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("未找到 AI 提供商配置: " + providerName);
        }

        if ("aliyun".equalsIgnoreCase(providerName)) {
            return QwenStreamingChatModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getChatModel())
                    .build();
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getChatModel())
                .build();
    }

    /**
     * 对话记忆（滑动窗口）
     * <p>保留最近 10 轮对话消息，用于维持多轮对话上下文。</p>
     */
    @Bean
    public MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}
