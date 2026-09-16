package org.seaPack.controller.document;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.service.document.DocumentGenerateRequest;
import org.seaPack.service.document.DocumentGenerator;
import org.seaPack.service.document.GeneratedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档生成控制器
 * <p>统一入口，根据 templateType 路由到对应的 DocumentGenerator。</p>
 *
 * <h3>接口</h3>
 * <ul>
 *   <li>POST /v1/documents/generate — 生成文档</li>
 *   <li>GET /v1/documents/types — 查询支持的文档类型列表</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/v1/documents")
public class DocumentGenerateController {

    /** Spring 自动收集所有 DocumentGenerator 实现 */
    @Autowired
    private List<DocumentGenerator> generatorList;

    private Map<String, DocumentGenerator> generatorMap;

    @jakarta.annotation.PostConstruct
    public void init() {
        generatorMap = new HashMap<>();
        for (DocumentGenerator g : generatorList) {
            generatorMap.put(g.getTemplateType(), g);
        }
        log.info("已注册文档生成器: {}", generatorMap.keySet());
    }

    /**
     * 生成文档
     *
     * @param request 包含 templateType 和 params
     * @return 生成结果（url、fileName、fileSize）
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody DocumentGenerateRequest request) {
        if (request.getTemplateType() == null || request.getTemplateType().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "templateType 不能为空"));
        }

        DocumentGenerator generator = generatorMap.get(request.getTemplateType());
        if (generator == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "不支持的文档类型: " + request.getTemplateType(),
                    "available", generatorMap.keySet()));
        }

        try {
            log.info("文档生成请求: type={}, params={}", request.getTemplateType(), request.getParams());
            GeneratedFile file = generator.generate(request.getParams());

            Map<String, Object> result = new HashMap<>();
            result.put("url", file.getUrl());
            result.put("fileName", file.getFileName());
            result.put("fileSize", file.getFileSize());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("文档生成参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("文档生成失败: type={}, error={}", request.getTemplateType(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "文档生成失败: " + e.getMessage()));
        }
    }

    /**
     * 查询支持的文档类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> listTypes() {
        Map<String, Object> result = new HashMap<>();
        result.put("types", generatorMap.keySet());
        return ResponseEntity.ok(result);
    }
}
