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

@Service
public class RagService {

    private final AIProperties aiProperties;

    private EmbeddingModel embeddingModel;

    private final Map<String, EmbeddingStore<TextSegment>> namespaceStore = new ConcurrentHashMap<>();

    public RagService(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

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

    public void clearNamespace(String namespace) {
        namespaceStore.remove(namespace);
    }

    public List<String> getNamespaces() {
        return List.copyOf(namespaceStore.keySet());
    }

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