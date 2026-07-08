package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.RetrievalResult;
import org.seaPack.mapper.ai.KnowledgeBaseMapper;
import org.seaPack.mapper.ai.KnowledgeChunkMapper;
import org.seaPack.mapper.ai.KnowledgeDocumentMapper;
import org.seaPack.model.ai.KnowledgeBase;
import org.seaPack.model.ai.KnowledgeChunk;
import org.seaPack.model.ai.KnowledgeDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 知识库核心服务
 * <p>提供知识库 CRUD、文档上传/删除、分片管理及语义检索等功能。</p>
 */
@Service
public class KnowledgeBaseService {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private KnowledgeDocumentMapper documentMapper;

    @Autowired
    private KnowledgeChunkMapper chunkMapper;

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

    /** 删除知识库（级联删除文档和分片） */
    @Transactional
    public int deleteById(Long id) {
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

    /** 清空知识库（删除所有文档和分片） */
    @Transactional
    public int clear(Long id) {
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
     * <p>将文件保存到磁盘，创建文档记录（状态为待解析）。</p>
     */
    @Transactional
    public KnowledgeDocument uploadDocument(Long knowledgeId, MultipartFile file, Long userId) throws IOException {
        // 校验知识库存在
        KnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeId);
        if (kb == null) {
            throw new RuntimeException("知识库不存在: " + knowledgeId);
        }

        // 保存文件到磁盘
        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);
        String storedName = UUID.randomUUID().toString() + "." + fileType;
        Path uploadPath = Paths.get(uploadDir, String.valueOf(knowledgeId));
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(storedName);
        file.transferTo(filePath.toFile());

        // 创建文档记录
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setKnowledgeId(knowledgeId);
        doc.setFileName(originalFilename);
        doc.setFilePath(filePath.toString());
        doc.setFileSize(file.getSize());
        doc.setFileType(fileType);
        doc.setContentType(file.getContentType());
        doc.setParseStatus(0); // 待解析
        doc.setVectorStatus(0); // 待处理
        doc.setCreatedBy(userId);

        documentMapper.insert(doc);
        return doc;
    }

    /** 删除文档（含分片和文件） */
    @Transactional
    public int deleteDocument(Long knowledgeId, Long docId) {
        KnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc != null) {
            // 删除分片
            chunkMapper.deleteByDocumentId(docId);
            // 删除磁盘文件
            if (doc.getFilePath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(doc.getFilePath()));
                } catch (IOException ignored) {
                }
            }
        }
        int deleted = documentMapper.deleteById(docId);
        // 更新知识库统计
        knowledgeBaseMapper.updateStats(knowledgeId);
        return deleted;
    }

    /** 重新处理文档（重置状态为待解析） */
    @Transactional
    public int reprocessDocument(Long knowledgeId, Long docId) {
        // 清除旧分片
        chunkMapper.deleteByDocumentId(docId);
        // 重置状态
        documentMapper.updateStatus(docId, 0, 0);
        documentMapper.updateStats(docId, 0, 0L);
        // 更新知识库统计
        knowledgeBaseMapper.updateStats(knowledgeId);
        return 1;
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
     * 语义检索
     * <p>当前为关键词匹配实现，实际应接入向量数据库（如 Milvus/Weaviate/Chroma）做相似度检索。</p>
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

        // 当前使用关键词匹配作为占位实现
        List<KnowledgeChunk> chunks = chunkMapper.selectByKeyword(knowledgeId, query);
        List<RetrievalResult> results = new ArrayList<>();
        for (int i = 0; i < Math.min(chunks.size(), topK); i++) {
            KnowledgeChunk chunk = chunks.get(i);
            RetrievalResult result = new RetrievalResult();
            result.setContent(chunk.getContent());
            result.setScore(1.0 - i * 0.1); // 占位分数
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

    /** 获取文件扩展名 */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "txt";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
