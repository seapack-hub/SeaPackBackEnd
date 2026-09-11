package org.seaPack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ChromaDB 连接配置属性
 * <p>对应 application.properties 中的 ai.chroma.* 配置项。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.chroma")
public class ChromaDbProperties {

    /**
     * ChromaDB REST API 地址
     * <p>例如: http://124.222.194.201:8000</p>
     */
    private String baseUrl = "http://124.222.194.201:8000";

    /**
     * API Key（可选，当前版本 LangChain4j ChromaDB 不支持，保留配置备用）
     * <p>ChromaDB 认证需要通过其他方式（如网络层、反向代理）配置</p>
     */
    private String apiKey;

    /**
     * Collection 名称前缀
     * <p>每个知识库会创建独立的 Collection，命名格式为: {prefix}{knowledgeId}
     * 例如: knowledge_1, knowledge_2, ...</p>
     */
    private String collectionPrefix = "knowledge_";

    /**
     * 根据知识库 ID 获取对应的 Collection 名称
     *
     * @param knowledgeId 知识库 ID
     * @return Collection 名称
     */
    public String getCollectionName(Long knowledgeId) {
        return collectionPrefix + knowledgeId;
    }
}
