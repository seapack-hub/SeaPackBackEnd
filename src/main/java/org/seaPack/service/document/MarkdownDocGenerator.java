package org.seaPack.service.document;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Markdown 文档生成器
 * <p>将 Markdown 文本转换为 PDF 文件。</p>
 *
 * <h3>参数</h3>
 * <ul>
 *   <li>content (String, 必填): Markdown 原始文本</li>
 *   <li>title (String, 可选): 文档标题（显示在 PDF 首页）</li>
 * </ul>
 */
@Slf4j
@Component
public class MarkdownDocGenerator implements DocumentGenerator {

    @Value("${file.upload.dir:uploads/files}")
    private String uploadDir;

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownDocGenerator() {
        this.parser = Parser.builder()
                .extensions(Arrays.asList(
                        TablesExtension.create(),
                        StrikethroughExtension.create(),
                        TocExtension.create()))
                .build();
        this.renderer = HtmlRenderer.builder().build();
    }

    @Override
    public String getTemplateType() {
        return "markdown";
    }

    @Override
    public GeneratedFile generate(Map<String, Object> params) throws Exception {
        String content = getParam(params, "content", "");
        String title = getParam(params, "title", "文档");

        if (content.isBlank()) {
            throw new IllegalArgumentException("content 参数不能为空");
        }

        // 1. Markdown → HTML
        String htmlBody = renderer.render(parser.parse(content));

        // 2. 包装完整 HTML（含 CSS 样式）
        String fullHtml = buildFullHtml(title, htmlBody);

        // 3. HTML → PDF
        byte[] pdfBytes = convertHtmlToPdf(fullHtml);

        // 4. 保存到 uploads/files/
        String filename = UUID.randomUUID() + ".pdf";
        Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, pdfBytes);

        String url = "/files/" + filename;
        String displayTitle = title.replaceAll("[/\\\\:*?\"<>|]", "_");
        String fileName = displayTitle + ".pdf";

        log.info("Markdown 文档生成完成: title={}, size={}bytes, url={}", title, pdfBytes.length, url);

        return new GeneratedFile(url, fileName, pdfBytes.length);
    }

    private String buildFullHtml(String title, String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body {
                            font-family: "Microsoft YaHei", "SimSun", "Noto Sans CJK SC", "WenQuanYi Zen Hei", sans-serif;
                            font-size: 14px;
                            line-height: 1.8;
                            color: #333;
                            margin: 40px;
                        }
                        h1 { font-size: 24px; color: #1a1a1a; border-bottom: 2px solid #333; padding-bottom: 8px; margin-bottom: 24px; }
                        h2 { font-size: 20px; color: #2c3e50; margin-top: 32px; }
                        h3 { font-size: 16px; color: #34495e; margin-top: 24px; }
                        p { margin: 12px 0; }
                        code {
                            background: #f5f5f5;
                            padding: 2px 6px;
                            border-radius: 3px;
                            font-family: "Consolas", monospace;
                            font-size: 13px;
                        }
                        pre {
                            background: #f5f5f5;
                            padding: 16px;
                            border-radius: 6px;
                            overflow-x: auto;
                            line-height: 1.5;
                        }
                        pre code { background: none; padding: 0; }
                        table { border-collapse: collapse; width: 100%%; margin: 16px 0; }
                        th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }
                        th { background: #f0f0f0; font-weight: 600; }
                        blockquote {
                            border-left: 4px solid #3498db;
                            margin: 16px 0;
                            padding: 8px 16px;
                            background: #f8f9fa;
                            color: #555;
                        }
                        a { color: #3498db; text-decoration: none; }
                        hr { border: none; border-top: 1px solid #ddd; margin: 24px 0; }
                        img { max-width: 100%%; }
                    </style>
                </head>
                <body>
                    <h1>%s</h1>
                    %s
                </body>
                </html>
                """.formatted(title, bodyHtml);
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        var builder = new PdfRendererBuilder()
                .withHtmlContent(html, null)
                .toStream(baos);

        // 跨平台字体加载：根据 OS 选择字体路径
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // Windows
            registerFont(builder, "C:/Windows/Fonts/msyh.ttc", "Microsoft YaHei");
            registerFont(builder, "C:/Windows/Fonts/simsun.ttc", "SimSun");
        } else {
            // Linux / macOS：尝试常见中文字体路径
            registerFont(builder, "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc", "Noto Sans CJK");
            registerFont(builder, "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc", "Noto Sans CJK");
            registerFont(builder, "/usr/share/fonts/noto-cjk/NotoSansCJK-Regular.ttc", "Noto Sans CJK");
            registerFont(builder, "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc", "WenQuanYi Zen Hei");
            registerFont(builder, "/usr/share/fonts/wqy-zenhei/wqy-zenhei.ttc", "WenQuanYi Zen Hei");
        }

        builder.run();
        return baos.toByteArray();
    }

    /** 安全注册字体：文件存在才加载，不存在则跳过 */
    private void registerFont(PdfRendererBuilder builder, String path, String family) {
        java.io.File fontFile = new java.io.File(path);
        if (fontFile.exists()) {
            builder.useFont(fontFile, family);
            log.debug("加载 PDF 字体: {} -> {}", path, family);
        } else {
            log.warn("字体文件不存在，跳过: {}", path);
        }
    }

    private String getParam(Map<String, Object> params, String key, String defaultValue) {
        if (params == null) return defaultValue;
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
