package org.seaPack.controller.ai;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.FileParserUtil;
import org.seaPack.config.Result;
import org.seaPack.dto.ai.IngestRequest;
import org.seaPack.service.ai.RagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 知识库控制器
 * <p>提供知识库命名空间管理、文档导入等功能，
 * 用于将文本/文件内容向量化存储并支持后续检索增强生成。</p>
 */
@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 清空指定命名空间下的所有知识库数据
     * @param namespace 命名空间名称
     */
    @DeleteMapping("/namespace/{namespace}")
    public Result<Void> clearNamespace(@PathVariable String namespace) {
        ragService.clearNamespace(namespace);
        return Result.success();
    }

    /**
     * 获取所有知识库命名空间列表
     * @return 命名空间名称列表
     */
    @GetMapping("/namespaces")
    public Result<List<String>> getNamespaces() {
        return Result.success(ragService.getNamespaces());
    }

    /**
     * 上传文件并导入到指定命名空间
     * <p>支持 txt / pdf / docx / csv / md 等格式，自动解析为文本后向量化存储。</p>
     * @param namespace 目标命名空间
     * @param file      上传文件
     */
    @PostMapping("/ingest-file")
    public Result<Void> ingestFile(
            @RequestParam("namespace") String namespace,
            @RequestParam("file") MultipartFile file) {

        log.info("正在接收文件: {} 到空间: [{}]", file.getOriginalFilename(), namespace);

        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            String text = FileParserUtil.parseFile(file.getInputStream(), file.getOriginalFilename());
            ragService.ingestText(namespace, text);
            return Result.success();
        } catch (Exception e) {
            log.error("文件解析失败: {}", e.getMessage(), e);
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }
}