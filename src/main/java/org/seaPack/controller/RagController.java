package org.seaPack.controller;

import org.seaPack.components.FileParserUtil;
import org.seaPack.config.Result;
import org.seaPack.dto.ChatRequest;
import org.seaPack.dto.IngestRequest;
import org.seaPack.service.RagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG控制器
 * 提供RAG应用的相关API接口，包括文档入库、问答、命名空间管理等
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    /**
     * RAG服务类，负责文档向量化、存储和问答处理
     */
    private final RagService ragService;

    /**
     * 构造函数，注入RagService
     * @param ragService RAG服务类
     */
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 文档入库接口
     * 将文本转换为向量并存储到指定命名空间
     * @param request 入库请求，包含namespace和text字段
     * @return 操作结果
     */
    @PostMapping("/ingest")
    public Result<Void> ingest(@RequestBody IngestRequest request) {
        // 调用RagService的文档入库方法
        ragService.ingestText(request.getNamespace(), request.getText());
        return Result.success();
    }

    /**
     * 问答接口
     * 根据问题检索相关文档并生成回答
     * @param request 问答请求，包含namespace和question字段
     * @return 生成的回答
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        // 调用RagService的问答方法，获取回答
        String answer = ragService.chat(request.getNamespace(), request.getQuestion());
        return Result.success(answer);
    }

    /**
     * 清除命名空间接口
     * 删除指定命名空间下的所有文档和向量数据
     * @param namespace 要清除的命名空间名称
     * @return 操作结果
     */
    @DeleteMapping("/namespace/{namespace}")
    public Result<Void> clearNamespace(@PathVariable String namespace) {
        // 调用RagService的清除命名空间方法
        ragService.clearNamespace(namespace);
        return Result.success();
    }

    /**
     * 获取命名空间列表接口
     * 返回所有已创建的知识库命名空间
     * @return 命名空间名称列表
     */
    @GetMapping("/namespaces")
    public Result<List<String>> getNamespaces() {
        // 调用RagService获取命名空间列表
        return Result.success(ragService.getNamespaces());
    }

    /**
     * 文件上传接口
     */
    @PostMapping("/ingest-file")
    public Result<Void> ingestFile(
            @RequestParam("namespace") String namespace,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            // 1. 使用工具类解析文件为文本
            String text = FileParserUtil.parseFile(file.getInputStream(), file.getOriginalFilename());

            // 2. 调用原有的文本入库逻辑
            ragService.ingestText(namespace, text);

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }
}