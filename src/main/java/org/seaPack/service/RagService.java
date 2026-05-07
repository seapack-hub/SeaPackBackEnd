package org.seaPack.service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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

@Service
public class RagService {

    /**
     * 注入多模型配置类
     */
    private final AIProperties aiProperties;
    
    /**
     * 向量化模型，用于将文本转换为向量表示
     */
    private EmbeddingModel embeddingModel;

    /**
     * 命名空间到向量存储的映射，支持多知识库隔离
     * Key: 命名空间名称
     * Value: 该命名空间下的向量存储库
     */
    private final Map<String, EmbeddingStore<TextSegment>> namespaceStore = new ConcurrentHashMap<>();

    /**
     * 构造函数注入 AIProperties
     */
    public RagService(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * 初始化方法，在Bean构造完成后执行
     * 初始化embedding模型和chat模型
     */
    @PostConstruct
    public void init() {
        // 1. 获取当前激活的提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        if (config == null) {
            throw new RuntimeException("未找到 AI 提供商配置: " + providerName);
        }

        System.out.println("正在初始化 RAG 服务，使用提供商: " + providerName);

        // 2. 构建向量化模型
        // 利用 OpenAiEmbeddingModel 的兼容性，指向 DeepSeek 或 阿里云 的接口
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getEmbeddingModel())
                .build();


        System.out.println("RAG 服务初始化完成。");
    }

    /**
     * 文档入库方法，将文本转换为向量并存储
     * @param namespace 命名空间，用于隔离不同知识库
     * @param text 要入库的文本内容
     */
    public void ingestText(String namespace, String text) {
        // 获取或创建该命名空间对应的向量存储库
        EmbeddingStore<TextSegment> store = namespaceStore.computeIfAbsent(namespace, k -> new InMemoryEmbeddingStore<>());
       
        // 将文本转换为Document对象（使用构造方法）
        Document document = new Document(text);
        
        // 创建文档分割器，按段落分割，每段最大500字符，重叠100字符
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(500, 100);
        // 分割文档为多个文本片段
        List<TextSegment> segments = splitter.split(document);

        // 简单日志，便于排查
        System.out.println("[RagService] ingestText namespace=" + namespace + ", segments=" + segments.size());

        // 遍历每个文本片段，进行向量化并存储
        for (TextSegment segment : segments) {
            // 使用embedding模型将文本片段转换为向量
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            // 将向量和对应的文本片段存入向量存储库
            store.add(embedding, segment);
        }
    }

    /**
     * 清除指定命名空间的所有文档
     * @param namespace 要清除的命名空间名称
     */
    public void clearNamespace(String namespace) {
        namespaceStore.remove(namespace);
    }

    /**
     * 获取所有已存在的命名空间列表
     * @return 命名空间名称列表
     */
    public List<String> getNamespaces() {
        return List.copyOf(namespaceStore.keySet());
    }

    /**
     * 仅检索上下文，不生成回答
     * 供 ChatController 在流式对话前调用
     */
    public String getRelevantContext(String namespace, String question) {
        //根据命名空间来获取具体的向量存储实例
        EmbeddingStore<TextSegment> store = namespaceStore.get(namespace);
        if (store == null) {
            return null;
        }

        //将用户的自然语言问题（String question）转换成一个高维向量（Embedding）。
        Embedding queryEmbedding = embeddingModel.embed(question).content();

        //在向量数据库中查找与 queryEmbedding 最相似的向量。
        // findRelevant方法参数：
        // 参数一 referenceEmbedding：刚才生成的问题向量
        // 参数二 maxResults：表示只返回相似度最高的 3 个结果。这是为了控制上下文窗口的大小，防止给大模型喂太多无关信息。
        // 参数三 minScore：Min-Score（最小相似度阈值）。只有相似度得分高于 0.5（通常指余弦相似度）的结果才会被返回。这起到一个“过滤器”的作用，避免把不相关的内容强行塞给模型。
        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(queryEmbedding, 3, 0.5);

        if (matches.isEmpty()) {
            return null;
        }

        // 大模型通常接受一段连续的文本作为 Prompt 的上下文（Context）
        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }
}
