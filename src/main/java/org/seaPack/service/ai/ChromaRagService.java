package org.seaPack.service.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.ChromaDbConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChromaDB 版 RAG 检索增强生成服务
 * <p>使用 ChromaDB 作为向量存储后端，支持持久化存储和大规模向量检索。</p>
 *
 * <p>与原有 {@link RagService}（内存版）的区别：</p>
 * <ul>
 *     <li>RagService：基于内存，数据重启丢失，适合测试</li>
 *     <li>ChromaRagService：基于 ChromaDB，数据持久化，适合生产</li>
 * </ul>
 *
 * @see RagService 内存版实现（保留不动）
 */
@Slf4j
@Service
public class ChromaRagService {

    private final ChromaDbConfig chromaDbConfig;
    private final EmbeddingModel embeddingModel;

    public ChromaRagService(
            ChromaDbConfig chromaDbConfig,
            @Qualifier("embeddingModel") EmbeddingModel embeddingModel) {
        this.chromaDbConfig = chromaDbConfig;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 将文本导入指定知识库的 Collection
     * <p>文本会先按段落分割（每段 500 字符，重叠 100 字符），
     * 然后逐段向量化后存入 ChromaDB。</p>
     *
     * @param knowledgeId 知识库 ID（自动转换为 Collection 名称）
     * @param text        待入库的文本内容
     */
    public void ingestText(Long knowledgeId, String text) {
        EmbeddingStore<TextSegment> store = chromaDbConfig.getEmbeddingStore(knowledgeId);

        Document document = Document.from(text);
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(500, 100);
        List<TextSegment> segments = splitter.split(document);

        log.info("ingestText to ChromaDB knowledgeId={}, segments={}", knowledgeId, segments.size());

        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            store.add(embedding, segment);
        }

        log.info("ingestText完成: knowledgeId={}, count={}", knowledgeId, segments.size());
    }

    /**
     * 检索指定知识库的相关上下文
     * <p>将问题向量化后在指定 Collection 中执行相似度搜索，返回 top-K 且得分 >= 0.5 的文本块。</p>
     *
     * @param knowledgeId 知识库 ID
     * @param question    用户问题
     * @param topK        返回条数
     * @return 相关上下文文本（多段以换行分隔），无匹配时返回 null
     */
    public String getRelevantContext(Long knowledgeId, String question, int topK) {
        EmbeddingStore<TextSegment> store = chromaDbConfig.getEmbeddingStore(knowledgeId);
        Embedding queryEmbedding = embeddingModel.embed(question).content();

        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(queryEmbedding, topK, 0.5);

        if (matches.isEmpty()) {
            return null;
        }

        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 检索指定知识库的相关上下文（默认返回 top-3）
     */
    public String getRelevantContext(Long knowledgeId, String question) {
        return getRelevantContext(knowledgeId, question, 3);
    }
}
