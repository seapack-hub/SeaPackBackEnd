package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.RetrievalRequest;
import org.seaPack.dto.ai.RetrievalResult;
import org.seaPack.model.ai.KnowledgeBase;
import org.seaPack.model.ai.KnowledgeChunk;
import org.seaPack.model.ai.KnowledgeDocument;
import org.seaPack.service.ai.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * AI 知识库管理控制器
 * <p>提供知识库 CRUD、文档上传/删除、分片查询及语义检索接口。</p>
 */
@RestController
@RequestMapping("/ai/knowledge-bases")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    // ===== 知识库主体 CRUD =====

    /** 分页查询知识库列表 */
    @GetMapping
    public PageInfo<KnowledgeBase> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return knowledgeBaseService.getList(pageNum, pageSize, status, keyword);
    }

    /** 全量知识库列表（下拉选择用） */
    @GetMapping("/all")
    public List<KnowledgeBase> all() {
        return knowledgeBaseService.getAll();
    }

    /** 查询知识库详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<KnowledgeBase> detail(@PathVariable Long id) {
        KnowledgeBase kb = knowledgeBaseService.getById(id);
        if (kb == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(kb);
    }

    /** 新增知识库 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody KnowledgeBase knowledgeBase) {
        if (knowledgeBaseService.isCodeDuplicate(knowledgeBase.getCode(), null)) {
            return ResponseEntity.badRequest().body("知识库编码已存在: " + knowledgeBase.getCode());
        }
        knowledgeBase.setCreatedBy(getCurrentUserId());
        knowledgeBaseService.insert(knowledgeBase);
        return ResponseEntity.ok(knowledgeBase);
    }

    /** 编辑知识库 */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody KnowledgeBase knowledgeBase) {
        if (knowledgeBase.getId() == null) {
            return ResponseEntity.badRequest().body("知识库 ID 不能为空");
        }
        if (knowledgeBase.getCode() != null && knowledgeBaseService.isCodeDuplicate(knowledgeBase.getCode(), knowledgeBase.getId())) {
            return ResponseEntity.badRequest().body("知识库编码已存在: " + knowledgeBase.getCode());
        }
        knowledgeBaseService.update(knowledgeBase);
        return ResponseEntity.ok(knowledgeBase);
    }

    /** 删除知识库 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        knowledgeBaseService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /** 复制知识库 */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            KnowledgeBase copy = knowledgeBaseService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 启停切换 */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        knowledgeBaseService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    /** 清空知识库（删除所有文档和分片） */
    @PostMapping("/{id}/clear")
    public ResponseEntity<?> clear(@PathVariable Long id) {
        knowledgeBaseService.clear(id);
        return ResponseEntity.ok("清空成功");
    }

    // ===== 文档管理 =====

    /** 获取知识库下的文档列表 */
    @GetMapping("/{knowledgeId}/documents")
    public PageInfo<KnowledgeDocument> getDocuments(
            @PathVariable Long knowledgeId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return knowledgeBaseService.getDocuments(knowledgeId, pageNum, pageSize);
    }

    /** 上传文档 */
    @PostMapping("/{knowledgeId}/documents/upload")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long knowledgeId,
            @RequestParam("file") MultipartFile file) {
        try {
            KnowledgeDocument doc = knowledgeBaseService.uploadDocument(knowledgeId, file, getCurrentUserId());
            return ResponseEntity.ok(doc);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 删除文档 */
    @DeleteMapping("/{knowledgeId}/documents/{docId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long knowledgeId, @PathVariable Long docId) {
        knowledgeBaseService.deleteDocument(knowledgeId, docId);
        return ResponseEntity.ok().build();
    }

    /** 重新处理文档（重新解析+向量化） */
    @PostMapping("/{knowledgeId}/documents/{docId}/reprocess")
    public ResponseEntity<?> reprocessDocument(@PathVariable Long knowledgeId, @PathVariable Long docId) {
        knowledgeBaseService.reprocessDocument(knowledgeId, docId);
        return ResponseEntity.ok("已重置为待处理状态");
    }

    // ===== 分片管理 =====

    /** 查看知识库的分片列表 */
    @GetMapping("/{knowledgeId}/chunks")
    public PageInfo<KnowledgeChunk> getChunks(
            @PathVariable Long knowledgeId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return knowledgeBaseService.getChunks(knowledgeId, pageNum, pageSize);
    }

    // ===== 语义检索 =====

    /** 测试检索 */
    @PostMapping("/{knowledgeId}/retrieve")
    public ResponseEntity<?> retrieve(@PathVariable Long knowledgeId, @RequestBody RetrievalRequest request) {
        try {
            List<RetrievalResult> results = knowledgeBaseService.retrieve(
                    knowledgeId, request.getQuery(), request.getTopK());
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
