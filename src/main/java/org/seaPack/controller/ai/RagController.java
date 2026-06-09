package org.seaPack.controller.ai;

import org.seaPack.components.FileParserUtil;
import org.seaPack.config.Result;
import org.seaPack.dto.ai.ChatRequest;
import org.seaPack.dto.ai.IngestRequest;
import org.seaPack.service.ai.RagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @DeleteMapping("/namespace/{namespace}")
    public Result<Void> clearNamespace(@PathVariable String namespace) {
        ragService.clearNamespace(namespace);
        return Result.success();
    }

    @GetMapping("/namespaces")
    public Result<List<String>> getNamespaces() {
        return Result.success(ragService.getNamespaces());
    }

    @PostMapping("/ingest-file")
    public Result<Void> ingestFile(
            @RequestParam("namespace") String namespace,
            @RequestParam("file") MultipartFile file) {

        System.out.println(">>> 正在接收文件: " + file.getOriginalFilename() + " 到空间: [" + namespace + "]");

        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            String text = FileParserUtil.parseFile(file.getInputStream(), file.getOriginalFilename());
            ragService.ingestText(namespace, text);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }
}