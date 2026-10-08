package org.seaPack.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用文件上传控制器
 * <p>
 * 提供图片等文件的上传接口，供博客编辑器、富文本编辑器等模块调用。
 * 上传的文件存储在本地 uploads/files/ 目录，通过 /files/** 静态资源映射公开访问。
 * </p>
 *
 * <h3>接口</h3>
 * <ul>
 * <li>POST /api/v1/files — 上传文件</li>
 * <li>DELETE /api/v1/files — 删除文件（按路径）</li>
 * <li>GET /api/v1/files/download — 下载文件（带原始文件名）</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/v1/files")
public class FileUploadController {

    @Value("${file.upload.dir:uploads/files}")
    private String uploadDir;

    @Value("${server.port:8090}")
    private int serverPort;

    /**
     * 下载文件（设置 Content-Disposition 附件名，浏览器保存时使用原始中文文件名而非 UUID）
     * <p>
     * 与 /files/** 静态映射指向同一存储目录；静态映射不带文件名信息，
     * 因此对话产物下载统一走本端点。
     * </p>
     *
     * @param filePath 文件路径（如 /files/xxx.pdf 或 xxx.pdf）
     * @param fileName 下载时使用的文件名（可选，不传则用存储文件名）
     */
    @GetMapping("/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @RequestParam("path") String filePath,
            @RequestParam(value = "fileName", required = false) String fileName) {
        try {
            String filename = filePath;
            if (filePath.contains("/files/")) {
                filename = filePath.substring(filePath.lastIndexOf("/files/") + 7);
            }
            // 防路径穿越：只允许 uploads/files 下的直接子文件
            filename = filename.replace("\\", "/");
            if (filename.contains("/") || filename.contains("..")) {
                return ResponseEntity.badRequest().build();
            }
            Path path = Paths.get(System.getProperty("user.dir"), uploadDir, filename);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return ResponseEntity.notFound().build();
            }

            String downloadName = (fileName != null && !fileName.isBlank()) ? fileName : filename;
            // RFC 5987：filename* 支持 UTF-8 中文文件名
            String encoded = java.net.URLEncoder.encode(downloadName, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("文件下载: path={}, downloadName={}", filename, downloadName);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(new org.springframework.core.io.FileSystemResource(path));
        } catch (IOException e) {
            log.error("文件下载失败: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 上传文件
     *
     * @param file 上传的文件（通过 multipart/form-data 传递）
     * @return 包含 name（原始文件名）和 url（可访问的完整 URL）的 Map
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
        }

        try {
            // 使用绝对路径，避免 file.transferTo 相对 Tomcat 临时目录解析
            Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名，保留原始扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;

            // 保存文件到磁盘
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // 构建可访问的 URL：/files/{filename}
            String url = "/files/" + filename;

            log.info("文件上传成功: originalName={}, savedAs={}, size={}bytes",
                    originalFilename, filename, file.getSize());

            // 返回格式与前端 FileAPI.upload 期望一致：{ name, url }
            // 外层 code/data 由全局响应包装器处理
            Map<String, Object> result = new HashMap<>();
            result.put("name", originalFilename);
            result.put("url", url);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径（如 /files/xxx.jpg 或 uploads/files/xxx.jpg）
     * @return 删除结果
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam String filePath) {
        try {
            // 支持多种路径格式：/files/xxx.jpg、uploads/files/xxx.jpg、xxx.jpg
            String filename = filePath;
            if (filePath.contains("/files/")) {
                filename = filePath.substring(filePath.lastIndexOf("/files/") + 7);
            }
            Path path = Paths.get(System.getProperty("user.dir"), uploadDir, filename);
            boolean deleted = Files.deleteIfExists(path);

            Map<String, Object> result = new HashMap<>();
            result.put("deleted", deleted);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "文件删除失败: " + e.getMessage()));
        }
    }
}
