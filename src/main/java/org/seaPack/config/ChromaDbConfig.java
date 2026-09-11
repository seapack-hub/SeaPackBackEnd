package org.seaPack.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChromaDB 配置类
 * <p>创建 EmbeddingModel Bean，并提供按知识库 ID 动态获取 EmbeddingStore 的能力。</p>
 *
 * <p>Collection 命名策略（方案B）：</p>
 * <ul>
 *     <li>每个知识库创建独立的 Collection</li>
 *     <li>命名格式: {prefix}{knowledgeId}，如 knowledge_1, knowledge_2</li>
 *     <li>优势：数据隔离、便于管理、支持独立的向量配置</li>
 * </ul>
 */
@Slf4j
@Configuration
public class ChromaDbConfig {

    private final ChromaDbProperties chromaProperties;
    private final AIProperties aiProperties;

    /**
     * 缓存已创建的 EmbeddingStore 实例（按 Collection 名称）
     */
    private final Map<String, EmbeddingStore<TextSegment>> storeCache = new ConcurrentHashMap<>();

    public ChromaDbConfig(ChromaDbProperties chromaProperties, AIProperties aiProperties) {
        this.chromaProperties = chromaProperties;
        this.aiProperties = aiProperties;
    }

    /**
     * EmbeddingModel Bean（供向量化使用）
     * <p>使用专门的 embeddingProvider 配置创建 EmbeddingModel 实例。
     * 向量模型可能来自与文本模型不同的提供商（如文本用 mimo，向量化用 aliyun）。</p>
     */
    @Bean("embeddingModel")
    public EmbeddingModel embeddingModel() {
        // 优先使用 embeddingProvider，如果未配置则使用 activeProvider
        String providerName = aiProperties.getEmbeddingProvider();
        if (providerName == null || providerName.isEmpty()) {
            providerName = aiProperties.getActiveProvider();
        }

        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("未找到 AI 提供商配置: " + providerName);
        }

        if (config.getEmbeddingModel() == null || config.getEmbeddingModel().isEmpty()) {
            throw new RuntimeException("提供商 " + providerName + " 未配置 embedding-model");
        }

        log.info("初始化 EmbeddingModel: provider={}, model={}", providerName, config.getEmbeddingModel());

        return OpenAiEmbeddingModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getEmbeddingModel())
                .build();
    }

    /**
     * 获取指定知识库的 EmbeddingStore
     * <p>每个知识库对应一个独立的 ChromaDB Collection。</p>
     *
     * @param knowledgeId 知识库 ID
     * @return EmbeddingStore 实例
     */
    public EmbeddingStore<TextSegment> getEmbeddingStore(Long knowledgeId) {
        String collectionName = chromaProperties.getCollectionName(knowledgeId);

        return storeCache.computeIfAbsent(collectionName, name -> {
            log.info("创建 ChromaDB EmbeddingStore: collection={}", name);

            // LangChain4j ChromaEmbeddingStore 当前版本不支持 apiKey 参数
            // ChromaDB 认证需要通过其他方式（如网络层）配置
            return ChromaEmbeddingStore.builder()
                    .baseUrl(chromaProperties.getBaseUrl())
                    .collectionName(name)
                    .build();
        });
    }
}
