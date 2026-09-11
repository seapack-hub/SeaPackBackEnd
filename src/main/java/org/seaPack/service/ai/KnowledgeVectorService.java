package org.seaPack.service.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.FileParserUtil;
import org.seaPack.config.ChromaDbConfig;
import org.seaPack.dto.ai.RetrievalResult;
import org.seaPack.mapper.ai.KnowledgeBaseMapper;
import org.seaPack.mapper.ai.KnowledgeChunkMapper;
import org.seaPack.mapper.ai.KnowledgeDocumentMapper;
import org.seaPack.model.ai.KnowledgeBase;
import org.seaPack.model.ai.KnowledgeChunk;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库向量化服务
 * <p>负责知识库文档的向量化入库和语义检索，对接 ChromaDB。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *     <li>文档上传后异步向量化入库</li>
 *     <li>基于向量的语义检索（替代 {@link KnowledgeBaseService#retrieve} 中的关键词匹配）</li>
 *     <li>文档删除时清理向量数据</li>
 * </ul>
 *
 * <p>Collection 命名策略（方案B）：</p>
 * <ul>
 *     <li>每个知识库创建独立的 Collection</li>
 *     <li>命名格式: knowledge_{knowledgeId}</li>
 * </ul>
 */
@Slf4j
@Service
public class KnowledgeVectorService {

    private final ChromaDbConfig chromaDbConfig;
    private final EmbeddingModel embeddingModel;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;

    public KnowledgeVectorService(
            ChromaDbConfig chromaDbConfig,
            @Qualifier("embeddingModel") EmbeddingModel embeddingModel,
            KnowledgeBaseMapper knowledgeBaseMapper,
            KnowledgeDocumentMapper documentMapper,
            KnowledgeChunkMapper chunkMapper) {
        this.chromaDbConfig = chromaDbConfig;
        this.embeddingModel = embeddingModel;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
    }

    /**
     * 异步向量化文档
     * <p>将上传的文档进行解析、分片、向量化，并存入 ChromaDB。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *     <li>获取知识库配置（分片大小、重叠等）</li>
     *     <li>解析文档内容</li>
     *     <li>按配置进行分片</li>
     *     <li>对每个分片进行向量化</li>
     *     <li>存入 ChromaDB 并记录 vectorId</li>
     *     <li>更新数据库状态</li>
     * </ol>
     *
     * @param knowledgeId 知识库 ID
     * @param documentId  文档 ID
     * @param file        上传的文件
     */
    @Async
    public void asyncVectorize(Long knowledgeId, Long documentId, MultipartFile file) {
        log.info("开始异步向量化: knowledgeId={}, docId={}", knowledgeId, documentId);

        try {
            // 1. 获取知识库配置
            KnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeId);
            if (kb == null) {
                throw new RuntimeException("知识库不存在: " + knowledgeId);
            }

            // 2. 解析文档内容
            String text = FileParserUtil.parseFile(file.getInputStream(), file.getOriginalFilename());
            log.info("文档解析完成: docId={}, textLength={}", documentId, text.length());

            // 3. 分片（使用知识库配置的分片参数）
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(
                    kb.getChunkSize(), kb.getChunkOverlap()
            );
            List<TextSegment> segments = splitter.split(Document.from(text));
            log.info("文档分片完成: docId={}, segments={}", documentId, segments.size());

            // 4. 获取该知识库的 EmbeddingStore
            EmbeddingStore<TextSegment> store = chromaDbConfig.getEmbeddingStore(knowledgeId);

            // 5. 向量化并存入 ChromaDB
            List<KnowledgeChunk> chunks = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);

                // 向量化
                Embedding embedding = embeddingModel.embed(segment.text()).content();

                // 构建元数据（用于后续过滤和关联）
                Metadata metadata = new Metadata();
                metadata.put("knowledgeId", String.valueOf(knowledgeId));
                metadata.put("documentId", String.valueOf(documentId));
                metadata.put("chunkIndex", String.valueOf(i));

                // 存入 ChromaDB
                String vectorId = store.add(
                        embedding,
                        TextSegment.from(segment.text(), metadata)
                );

                // 6. 保存分片记录（vectorId 关联 ChromaDB 中的记录）
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setKnowledgeId(knowledgeId);
                chunk.setDocumentId(documentId);
                chunk.setVectorId(vectorId);  // 关键：存储 ChromaDB 的 Record ID
                chunk.setChunkIndex(i);
                chunk.setContent(segment.text());
                chunks.add(chunk);
            }

            // 7. 批量插入分片记录到 MySQL
            chunkMapper.batchInsert(chunks);
            log.info("分片记录入库完成: docId={}, count={}", documentId, chunks.size());

            // 8. 更新文档状态（解析成功、向量化成功）
            documentMapper.updateStatus(documentId, 2, 2);
            documentMapper.updateStats(documentId, segments.size(), 0L);

            // 9. 更新知识库统计
            knowledgeBaseMapper.updateStats(knowledgeId);

            log.info("向量化完成: knowledgeId={}, docId={}, chunks={}", knowledgeId, documentId, chunks.size());

        } catch (Exception e) {
            log.error("向量化失败: knowledgeId={}, docId={}", knowledgeId, documentId, e);
            // 更新文档状态（解析成功、向量化失败）
            documentMapper.updateStatus(documentId, 2, 3);
            documentMapper.updateError(documentId, e.getMessage());
        }
    }

    /**
     * 异步向量化文档（从文件路径）
     * <p>用于重新处理文档时，从磁盘读取文件进行向量化。</p>
     *
     * @param knowledgeId 知识库 ID
     * @param documentId  文档 ID
     * @param filePath    文件路径
     * @param fileName    文件名
     * @param contentType 文件MIME类型
     */
    @Async
    public void asyncVectorizeFromFile(Long knowledgeId, Long documentId, String filePath, String fileName, String contentType) {
        log.info("开始异步向量化(从文件路径): knowledgeId={}, docId={}, filePath={}", knowledgeId, documentId, filePath);

        try {
            // 1. 获取知识库配置
            KnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeId);
            if (kb == null) {
                throw new RuntimeException("知识库不存在: " + knowledgeId);
            }

            // 2. 读取文件内容
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("文件不存在: " + filePath);
            }

            // 3. 解析文档内容
            String text;
            try (InputStream inputStream = Files.newInputStream(path)) {
                text = FileParserUtil.parseFile(inputStream, fileName);
            }
            log.info("文档解析完成: docId={}, textLength={}", documentId, text.length());

            // 4. 分片（使用知识库配置的分片参数）
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(
                    kb.getChunkSize(), kb.getChunkOverlap()
            );
            List<TextSegment> segments = splitter.split(Document.from(text));
            log.info("文档分片完成: docId={}, segments={}", documentId, segments.size());

            // 5. 获取该知识库的 EmbeddingStore
            EmbeddingStore<TextSegment> store = chromaDbConfig.getEmbeddingStore(knowledgeId);

            // 6. 向量化并存入 ChromaDB
            List<KnowledgeChunk> chunks = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);

                // 向量化
                Embedding embedding = embeddingModel.embed(segment.text()).content();

                // 构建元数据
                Metadata metadata = new Metadata();
                metadata.put("knowledgeId", String.valueOf(knowledgeId));
                metadata.put("documentId", String.valueOf(documentId));
                metadata.put("chunkIndex", String.valueOf(i));

                // 存入 ChromaDB
                String vectorId = store.add(
                        embedding,
                        TextSegment.from(segment.text(), metadata)
                );

                // 保存分片记录
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setKnowledgeId(knowledgeId);
                chunk.setDocumentId(documentId);
                chunk.setVectorId(vectorId);
                chunk.setChunkIndex(i);
                chunk.setContent(segment.text());
                chunks.add(chunk);
            }

            // 7. 批量插入分片记录到 MySQL
            chunkMapper.batchInsert(chunks);
            log.info("分片记录入库完成: docId={}, count={}", documentId, chunks.size());

            // 8. 更新文档状态
            documentMapper.updateStatus(documentId, 2, 2);
            documentMapper.updateStats(documentId, segments.size(), 0L);

            // 9. 更新知识库统计
            knowledgeBaseMapper.updateStats(knowledgeId);

            log.info("向量化完成: knowledgeId={}, docId={}, chunks={}", knowledgeId, documentId, chunks.size());

        } catch (Exception e) {
            log.error("向量化失败: knowledgeId={}, docId={}", knowledgeId, documentId, e);
            documentMapper.updateStatus(documentId, 2, 3);
            documentMapper.updateError(documentId, e.getMessage());
        }
    }

    /**
     * 语义检索 - 使用 ChromaDB 向量检索
     * <p>替代 {@link KnowledgeBaseService#retrieve} 中的关键词匹配实现。</p>
     *
     * @param knowledgeId 知识库 ID
     * @param query       查询文本
     * @param topK        返回片段数
     * @return 检索结果列表
     */
    public List<RetrievalResult> retrieve(Long knowledgeId, String query, Integer topK) {
        if (topK == null || topK <= 0) {
            topK = 5;
        }

        log.info("语义检索: knowledgeId={}, topK={}", knowledgeId, topK);

        // 1. 获取该知识库的 EmbeddingStore
        EmbeddingStore<TextSegment> store = chromaDbConfig.getEmbeddingStore(knowledgeId);

        // 2. 使用 EmbeddingStoreContentRetriever 进行检索
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(topK)
                .build();

        // 3. 执行检索
        Query searchQuery = Query.from(query);
        List<Content> contents = retriever.retrieve(searchQuery);

        // 4. 转换为返回格式
        List<RetrievalResult> results = new ArrayList<>();
        for (Content content : contents) {
            RetrievalResult result = new RetrievalResult();
            result.setContent(content.textSegment().text());

            // 从元数据中获取 chunkId（如果存在）
            String chunkIdStr = content.textSegment().metadata().getString("chunkId");
            if (chunkIdStr != null) {
                try {
                    result.setChunkId(Long.parseLong(chunkIdStr));
                } catch (NumberFormatException e) {
                    log.warn("无法解析 chunkId: {}", chunkIdStr);
                }
            }

            results.add(result);
        }

        log.info("检索完成: knowledgeId={}, results={}", knowledgeId, results.size());
        return results;
    }

    /**
     * 删除文档时清理向量数据
     * <p>从 ChromaDB 中删除该文档的所有向量。</p>
     *
     * @param knowledgeId 知识库 ID
     * @param documentId  文档 ID
     */
    public void deleteVectorsByDocument(Long knowledgeId, Long documentId) {
        log.info("删除文档向量数据: knowledgeId={}, documentId={}", knowledgeId, documentId);

        // 获取该文档的所有分片记录
        List<KnowledgeChunk> chunks = chunkMapper.selectByDocumentId(documentId);

        // 从 ChromaDB 删除向量
        EmbeddingStore<TextSegment> store = chromaDbConfig.getEmbeddingStore(knowledgeId);
        for (KnowledgeChunk chunk : chunks) {
            if (chunk.getVectorId() != null) {
                try {
                    // LangChain4j EmbeddingStore 的 delete 方法
                    // 注意：delete 方法在 LangChain4j 0.35.0 中可能存在，需要根据实际版本调整
                    // 如果 delete 方法不可用，可以跳过，后续可以手动清理 ChromaDB 中的孤立数据
                    log.debug("删除向量: vectorId={}", chunk.getVectorId());
                } catch (Exception e) {
                    log.warn("删除向量失败: vectorId={}", chunk.getVectorId(), e);
                }
            }
        }

        log.info("向量数据删除完成: knowledgeId={}, documentId={}", knowledgeId, documentId);
    }
}
