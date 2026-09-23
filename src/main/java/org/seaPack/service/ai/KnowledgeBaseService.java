package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.AgentTraceStepResult;
import org.seaPack.dto.ai.RetrievalResult;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.mapper.ai.AgentKnowledgeMapper;
import org.seaPack.mapper.ai.KnowledgeBaseMapper;
import org.seaPack.mapper.ai.KnowledgeChunkMapper;
import org.seaPack.mapper.ai.KnowledgeDocumentMapper;
import org.seaPack.model.ai.AgentKnowledge;
import org.seaPack.model.ai.KnowledgeBase;
import org.seaPack.model.ai.KnowledgeChunk;
import org.seaPack.model.ai.KnowledgeDocument;
import org.seaPack.config.VectorProgressManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 知识库核心服务
 * <p>提供知识库 CRUD、文档上传/删除、分片管理及语义检索等功能。</p>
 * <p>已集成 ChromaDB 向量检索，文档上传后自动异步向量化入库。</p>
 */
@Slf4j
@Service
public class KnowledgeBaseService {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private KnowledgeDocumentMapper documentMapper;

    @Autowired
    private KnowledgeChunkMapper chunkMapper;

    @Autowired
    private KnowledgeVectorService knowledgeVectorService;

    @Autowired
    private VectorProgressManager progressManager;

    @Autowired
    private AgentKnowledgeMapper agentKnowledgeMapper;

    @Value("${ai.knowledge.upload-dir:uploads/knowledge}")
    private String uploadDir;

    // ===== 知识库 CRUD =====

    /** 分页查询知识库列表 */
    public PageInfo<KnowledgeBase> getList(int pageNum, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<KnowledgeBase> list = knowledgeBaseMapper.selectList(status, keyword);
        return new PageInfo<>(list);
    }

    /** 全量查询已启用的知识库列表（下拉选择用） */
    public List<KnowledgeBase> getAll() {
        return knowledgeBaseMapper.selectList(1, null);
    }

    /** 根据 ID 查询知识库详情 */
    public KnowledgeBase getById(Long id) {
        return knowledgeBaseMapper.selectById(id);
    }

    /** 校验知识库编码是否已存在 */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return knowledgeBaseMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增知识库 */
    @Transactional
    public int insert(KnowledgeBase knowledgeBase) {
        return knowledgeBaseMapper.insert(knowledgeBase);
    }

    /** 更新知识库 */
    @Transactional
    public int update(KnowledgeBase knowledgeBase) {
        return knowledgeBaseMapper.update(knowledgeBase);
    }

    /** 删除知识库（级联删除文档、分片和向量数据） */
    @Transactional
    public int deleteById(Long id) {
        // 获取所有文档，清理向量数据
        List<KnowledgeDocument> documents = documentMapper.selectByKnowledgeId(id);
        for (KnowledgeDocument doc : documents) {
            try {
                knowledgeVectorService.deleteVectorsByDocument(id, doc.getId());
            } catch (Exception e) {
                log.warn("删除知识库向量数据失败: knowledgeId={}, docId={}, error={}", id, doc.getId(), e.getMessage());
            }
        }
        // 删除数据库记录
        chunkMapper.deleteByKnowledgeId(id);
        documentMapper.deleteByKnowledgeId(id);
        return knowledgeBaseMapper.deleteById(id);
    }

    /** 复制知识库（创建副本，不复制文档） */
    @Transactional
    public KnowledgeBase copy(Long id) {
        KnowledgeBase source = knowledgeBaseMapper.selectById(id);
        if (source == null) {
            throw new RuntimeException("知识库不存在: " + id);
        }

        KnowledgeBase copy = new KnowledgeBase();
        copy.setName(source.getName() + "（副本）");
        copy.setCode(source.getCode() + "_copy");
        copy.setDescription(source.getDescription());
        copy.setIcon(source.getIcon());
        copy.setEmbeddingModel(source.getEmbeddingModel());
        copy.setChunkSize(source.getChunkSize());
        copy.setChunkOverlap(source.getChunkOverlap());
        copy.setSeparator(source.getSeparator());
        copy.setStatus(source.getStatus());
        copy.setSortOrder(source.getSortOrder());
        copy.setCreatedBy(source.getCreatedBy());

        knowledgeBaseMapper.insert(copy);
        return copy;
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(id);
        kb.setStatus(status);
        return knowledgeBaseMapper.update(kb);
    }

    /** 清空知识库（删除所有文档、分片和向量数据） */
    @Transactional
    public int clear(Long id) {
        // 获取所有文档，清理向量数据
        List<KnowledgeDocument> documents = documentMapper.selectByKnowledgeId(id);
        for (KnowledgeDocument doc : documents) {
            try {
                knowledgeVectorService.deleteVectorsByDocument(id, doc.getId());
            } catch (Exception e) {
                log.warn("清空知识库向量数据失败: knowledgeId={}, docId={}, error={}", id, doc.getId(), e.getMessage());
            }
        }
        // 删除数据库记录
        chunkMapper.deleteByKnowledgeId(id);
        documentMapper.deleteByKnowledgeId(id);
        return knowledgeBaseMapper.updateStats(id);
    }

    // ===== 文档管理 =====

    /** 获取知识库下的文档列表 */
    public PageInfo<KnowledgeDocument> getDocuments(Long knowledgeId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<KnowledgeDocument> list = documentMapper.selectByKnowledgeId(knowledgeId);
        return new PageInfo<>(list);
    }

    /**
     * 上传文档
     * <p>将文件保存到磁盘，创建文档记录，并触发异步向量化。</p>
     *
     * @return 包含 docId 和 taskToken 的 Map，供前端订阅进度
     */
    @Transactional
    public Map<String, Object> uploadDocumentWithToken(Long knowledgeId, MultipartFile file, Long userId) throws IOException {
        // 校验知识库存在
        KnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeId);
        if (kb == null) {
            throw new RuntimeException("知识库不存在: " + knowledgeId);
        }

        // 保存文件到磁盘
        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);
        String storedName = UUID.randomUUID().toString() + "." + fileType;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir, String.valueOf(knowledgeId));
        log.info("文件上传目录: {}", uploadPath);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(storedName);
        file.transferTo(filePath.toFile());
        log.info("文件保存成功: {}", filePath);

        // 创建文档记录（存储相对路径，便于跨环境部署）
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setKnowledgeId(knowledgeId);
        doc.setFileName(originalFilename);
        String relativePath = Paths.get(uploadDir, String.valueOf(knowledgeId), storedName).toString();
        doc.setFilePath(relativePath);
        doc.setFileSize(file.getSize());
        doc.setFileType(fileType);
        doc.setContentType(file.getContentType());
        doc.setParseStatus(0);
        doc.setVectorStatus(0);
        doc.setCreatedBy(userId);

        documentMapper.insert(doc);
        log.info("文档上传成功: knowledgeId={}, docId={}, fileName={}", knowledgeId, doc.getId(), originalFilename);

        // 生成 taskToken 用于进度追踪
        String taskToken = UUID.randomUUID().toString();
        progressManager.register(taskToken, doc.getId());

        // 触发异步向量化
        try {
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            knowledgeVectorService.asyncVectorizeFromFile(knowledgeId, doc.getId(), filePath.toString(), originalFilename, contentType, taskToken);
            log.info("已触发异步向量化任务: knowledgeId={}, docId={}", knowledgeId, doc.getId());
        } catch (Exception e) {
            log.error("触发异步向量化失败: knowledgeId={}, docId={}", knowledgeId, doc.getId(), e);
            documentMapper.updateStatus(doc.getId(), 0, 3);
            documentMapper.updateError(doc.getId(), "触发向量化失败: " + e.getMessage());
            progressManager.complete(taskToken, false, "触发向量化失败: " + e.getMessage());
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("doc", doc);
        result.put("taskToken", taskToken);
        return result;
    }

    /**
     * 上传文档（兼容旧接口）
     */
    @Transactional
    public KnowledgeDocument uploadDocument(Long knowledgeId, MultipartFile file, Long userId) throws IOException {
        Map<String, Object> result = uploadDocumentWithToken(knowledgeId, file, userId);
        return (KnowledgeDocument) result.get("doc");
    }

    /** 删除文档（含分片、向量数据和文件） */
    @Transactional
    public int deleteDocument(Long knowledgeId, Long docId) {
        KnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc != null) {
            // 清理向量数据
            try {
                knowledgeVectorService.deleteVectorsByDocument(knowledgeId, docId);
            } catch (Exception e) {
                log.warn("删除文档向量数据失败: knowledgeId={}, docId={}, error={}", knowledgeId, docId, e.getMessage());
            }
            // 删除分片
            chunkMapper.deleteByDocumentId(docId);
            // 删除磁盘文件
            if (doc.getFilePath() != null) {
                try {
                    Files.deleteIfExists(resolveFilePath(doc.getFilePath()));
                } catch (IOException ignored) {
                }
            }
        }
        int deleted = documentMapper.deleteById(docId);
        // 更新知识库统计
        knowledgeBaseMapper.updateStats(knowledgeId);
        return deleted;
    }

    /**
     * 重新处理文档（清理旧向量，重置状态并重新向量化）
     *
     * @param knowledgeId 知识库 ID
     * @param docId       文档 ID
     * @return 包含 taskToken 的 Map，供前端订阅进度
     */
    @Transactional
    public Map<String, Object> reprocessDocument(Long knowledgeId, Long docId) {
        String taskToken = null;

        // 清理旧向量数据
        try {
            knowledgeVectorService.deleteVectorsByDocument(knowledgeId, docId);
        } catch (Exception e) {
            log.warn("清理旧向量数据失败: knowledgeId={}, docId={}, error={}", knowledgeId, docId, e.getMessage());
        }
        // 清除旧分片
        chunkMapper.deleteByDocumentId(docId);
        // 重置状态为待解析
        documentMapper.updateStatus(docId, 0, 0);
        documentMapper.updateStats(docId, 0, 0L);
        // 更新知识库统计
        knowledgeBaseMapper.updateStats(knowledgeId);

        // 获取文档信息，从磁盘重新读取并触发向量化
        KnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc != null && doc.getFilePath() != null) {
            try {
                Path filePath = resolveFilePath(doc.getFilePath());
                if (Files.exists(filePath)) {
                    String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";
                    String fileName = doc.getFileName() != null ? doc.getFileName() : "unknown.txt";
                    // 生成 taskToken
                    taskToken = UUID.randomUUID().toString();
                    progressManager.register(taskToken, docId);
                    // 异步向量化（从文件路径）
                    knowledgeVectorService.asyncVectorizeFromFile(knowledgeId, docId, doc.getFilePath(), fileName, contentType, taskToken);
                    log.info("已触发重新向量化任务: knowledgeId={}, docId={}", knowledgeId, docId);
                } else {
                    log.warn("文档文件不存在: {}", doc.getFilePath());
                    documentMapper.updateStatus(docId, 0, 3);
                    documentMapper.updateError(docId, "文档文件不存在: " + doc.getFilePath());
                }
            } catch (Exception e) {
                log.error("重新向量化失败: knowledgeId={}, docId={}", knowledgeId, docId, e);
                documentMapper.updateStatus(docId, 0, 3);
                documentMapper.updateError(docId, "重新向量化失败: " + e.getMessage());
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("taskToken", taskToken);
        result.put("doc", doc);
        return result;
    }

    // ===== 分片管理 =====

    /** 获取知识库的分片列表 */
    public PageInfo<KnowledgeChunk> getChunks(Long knowledgeId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<KnowledgeChunk> list = chunkMapper.selectByKnowledgeId(knowledgeId);
        return new PageInfo<>(list);
    }

    // ===== 语义检索 =====

    /**
     * 语义检索（基于 ChromaDB 向量检索）
     * <p>使用向量相似度检索，返回与查询文本最相关的分片。</p>
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

        // 使用向量检索
        try {
            return knowledgeVectorService.retrieve(knowledgeId, query, topK);
        } catch (Exception e) {
            log.warn("向量检索失败，降级为关键词匹配: knowledgeId={}, error={}", knowledgeId, e.getMessage());
            // 降级为关键词匹配
            return retrieveByKeyword(knowledgeId, query, topK);
        }
    }

    /**
     * 关键词匹配检索（降级方案）
     */
    private List<RetrievalResult> retrieveByKeyword(Long knowledgeId, String query, Integer topK) {
        List<KnowledgeChunk> chunks = chunkMapper.selectByKeyword(knowledgeId, query);
        List<RetrievalResult> results = new ArrayList<>();
        for (int i = 0; i < Math.min(chunks.size(), topK); i++) {
            KnowledgeChunk chunk = chunks.get(i);
            RetrievalResult result = new RetrievalResult();
            result.setContent(chunk.getContent());
            result.setScore(1.0 - i * 0.1); // 降级分数
            result.setChunkId(chunk.getId());
            results.add(result);
        }
        return results;
    }

    // ===== 统计更新 =====

    /** 手动触发知识库统计更新 */
    @Transactional
    public void refreshStats(Long knowledgeId) {
        knowledgeBaseMapper.updateStats(knowledgeId);
    }

    /**
     * 解析文件路径，兼容旧的绝对路径和新的相对路径
     * <p>新上传的文档存储相对路径（相对于 user.dir），读取时动态拼接。
     * 旧数据可能存储了绝对路径，需要兼容处理。</p>
     *
     * @param storedPath 数据库中存储的路径
     * @return 解析后的绝对路径
     */
    private Path resolveFilePath(String storedPath) {
        Path path = Paths.get(storedPath);
        if (path.isAbsolute()) {
            // 旧数据：已经是绝对路径，直接使用
            return path;
        }
        // 新数据：相对路径，拼接 user.dir
        return Paths.get(System.getProperty("user.dir"), storedPath);
    }

    /** 获取文件扩展名 */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "txt";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // ===== Agent 知识库检索 =====

    /**
     * 从 Agent 关联的知识库中检索相关内容（支持 SSE 流式进度）
     *
     * @param agent      当前 Agent
     * @param query      用户查询
     * @param stepIndex  当前步骤序号
     * @param emitter    SSE 发射器
     * @return 步骤执行结果
     */
    public AgentTraceStepResult retrieveKnowledge(org.seaPack.model.ai.Agent agent, String query, int stepIndex, SseEmitter emitter) {
        long stepStart = System.currentTimeMillis();
        StringBuilder knowledgeBuilder = new StringBuilder();
        int totalChunks = 0;
        List<Map<String, Object>> knowledgeDetails = new ArrayList<>();

        if (emitter != null) {
            SseEvent.send(emitter, "step_progress", Map.of(
                    "stepIndex", stepIndex,
                    "stepType", "knowledge_retrieval",
                    "message", "正在检索关联的知识库..."
            ));
        }

        List<AgentKnowledge> enabledKnowledge = agentKnowledgeMapper.selectByAgentId(agent.getId()).stream()
                .filter(k -> k.getEnabled() != null && k.getEnabled() == 1)
                .sorted(Comparator.comparingInt(k -> k.getSortOrder() != null ? k.getSortOrder() : 0))
                .collect(Collectors.toList());

        for (AgentKnowledge ak : enabledKnowledge) {
            String knowledgeName = ak.getKnowledgeName() != null ? ak.getKnowledgeName() : "知识库";

            if (emitter != null) {
                SseEvent.send(emitter, "step_progress", Map.of(
                        "stepIndex", stepIndex,
                        "stepType", "knowledge_retrieval",
                        "message", "正在检索知识库: " + knowledgeName
                ));
            }

            int topK = ak.getRetrievalCount() != null ? ak.getRetrievalCount() : 3;
            List<RetrievalResult> results = retrieve(ak.getKnowledgeId(), query, topK);

            Map<String, Object> knowledgeDetail = new HashMap<>();
            knowledgeDetail.put("knowledgeId", ak.getKnowledgeId());
            knowledgeDetail.put("knowledgeName", knowledgeName);
            knowledgeDetail.put("retrievalCount", topK);
            knowledgeDetail.put("actualCount", results.size());

            if (!results.isEmpty()) {
                knowledgeBuilder.append("【").append(knowledgeName).append("】\n");
                List<Map<String, Object>> chunkDetails = new ArrayList<>();
                for (RetrievalResult r : results) {
                    knowledgeBuilder.append("- ").append(r.getContent()).append("\n");
                    totalChunks++;

                    Map<String, Object> chunkDetail = new HashMap<>();
                    String content = r.getContent();
                    chunkDetail.put("content", content.length() > 200 ? content.substring(0, 200) + "..." : content);
                    chunkDetail.put("score", r.getScore());
                    chunkDetails.add(chunkDetail);
                }
                knowledgeBuilder.append("\n");
                knowledgeDetail.put("chunks", chunkDetails);
            } else {
                knowledgeDetail.put("message", "未检索到相关内容");
            }
            knowledgeDetails.add(knowledgeDetail);

            if (emitter != null) {
                SseEvent.send(emitter, "step_detail", Map.of(
                        "stepIndex", stepIndex,
                        "stepType", "knowledge_retrieval",
                        "detailType", "knowledge_result",
                        "knowledgeId", ak.getKnowledgeId(),
                        "knowledgeName", knowledgeName,
                        "foundCount", results.size(),
                        "chunks", results.isEmpty() ? List.of() : results.stream()
                                .map(r -> Map.<String, Object>of(
                                        "contentPreview", r.getContent().length() > 200
                                                ? r.getContent().substring(0, 200) + "..."
                                                : r.getContent(),
                                        "score", r.getScore() != null ? r.getScore() : 0.0
                                )).collect(Collectors.toList())
                ));
            }
        }

        if (emitter != null) {
            SseEvent.send(emitter, "step_progress", Map.of(
                    "stepIndex", stepIndex,
                    "stepType", "knowledge_retrieval",
                    "message", "知识库检索完成，共命中 " + totalChunks + " 条"
            ));
        }

        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("knowledge_retrieval");
        step.setStepName("知识库检索");
        step.setStatus(totalChunks > 0 ? "success" : "skip");
        step.setDurationMs(System.currentTimeMillis() - stepStart);
        step.setOutput(knowledgeBuilder.toString());
        Map<String, Object> meta = new HashMap<>();
        meta.put("knowledgeCount", enabledKnowledge.size());
        meta.put("chunkCount", totalChunks);
        meta.put("knowledgeDetails", knowledgeDetails);
        step.setMetadata(meta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = knowledgeBuilder.toString();
        result.nextStepIndex = stepIndex + 1;
        return result;
    }
}
