package org.seaPack.service.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.seaPack.config.AIProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RAG 检索增强生成服务
 * <p>支持多命名空间隔离的知识库，提供文本向量化入库与语义检索能力。
 * 底层使用内存向量存储，适合中小规模知识库场景。</p>
 */
@Service
public class RagService {

    private final AIProperties aiProperties;

    private EmbeddingModel embeddingModel;

    private final Map<String, EmbeddingStore<TextSegment>> namespaceStore = new ConcurrentHashMap<>();

    public RagService(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * 初始化向量化模型
     * <p>根据当前激活的 AI 提供商配置创建 EmbeddingModel 实例。</p>
     */
    @PostConstruct
    public void init() {
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("未找到 AI 提供商配置: " + providerName);
        }

        System.out.println("正在初始化 RAG 服务，使用提供商: " + providerName);

        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getEmbeddingModel())
                .build();

        System.out.println("RAG 服务初始化完成。");
    }

    /**
     * 将文本导入指定命名空间
     * <p>文本会先按段落分割（每段 500 字符，重叠 100 字符），
     * 然后逐段向量化后存入该命名空间的向量存储中。</p>
     * @param namespace 命名空间
     * @param text 待入库的文本内容
     */
    public void ingestText(String namespace, String text) {
        EmbeddingStore<TextSegment> store = namespaceStore.computeIfAbsent(namespace, k -> new InMemoryEmbeddingStore<>());

        Document document = new Document(text);

        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(500, 100);
        List<TextSegment> segments = splitter.split(document);

        System.out.println("[RagService] ingestText namespace=" + namespace + ", segments=" + segments.size());

        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            store.add(embedding, segment);
        }
    }

    /**
     * 清空指定命名空间下的所有知识库数据
     */
    public void clearNamespace(String namespace) {
        namespaceStore.remove(namespace);
    }

    /**
     * 获取所有知识库命名空间
     */
    public List<String> getNamespaces() {
        return List.copyOf(namespaceStore.keySet());
    }

    /**
     * 检索与问题最相关的知识库上下文
     * <p>将问题向量化后在指定命名空间中执行相似度搜索，返回 top-3 且得分 &ge; 0.5 的文本块。</p>
     * @param namespace 命名空间
     * @param question 用户问题
     * @return 相关上下文文本（多段以换行分隔），无匹配时返回 null
     */
    public String getRelevantContext(String namespace, String question) {
        EmbeddingStore<TextSegment> store = namespaceStore.get(namespace);
        if (store == null) {
            return null;
        }

        Embedding queryEmbedding = embeddingModel.embed(question).content();

        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(queryEmbedding, 3, 0.5);

        if (matches.isEmpty()) {
            return null;
        }

        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }
}