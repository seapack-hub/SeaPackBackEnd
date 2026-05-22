package org.seaPack.tools;


import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.seaPack.common.FileType;
import org.seaPack.dto.AgentContext;
import org.seaPack.service.ProgressService;
import org.springframework.stereotype.Component;
import dev.langchain4j.agent.tool.Tool;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class FileGeneratorTool {

    private final ProgressService progressService;

    public FileGeneratorTool(ProgressService progressService) {
        this.progressService = progressService;
    }

    @Tool("将搜集到的资料整理并生成指定格式的文档。请根据用户语境选择 fileType（如用户要表格则选 XLSX，要带格式文档选 DOCX）。若未提供 filePath，将根据类型自动生成默认文件名并保存在桌面。")
    public String generateReport(String filePath, FileType fileType, String content) {

        // 动态获取当前用户的桌面路径，跨平台兼容
        String DEFAULT_SAVE_DIR = System.getProperty("user.home") + File.separator + "Desktop";

        AgentContext context = progressService.getContext();
        if (context != null) {
            context.sendProgress("documents", "开始整理文档...");
            context.sendProgress("content", "📝 正在生成文档...");
        }

        try {
            // 如果 AI 没传路径，或者只传了文件名，就自动使用桌面作为默认地址
            if (filePath == null || filePath.trim().isEmpty() || !filePath.contains(File.separator)) {
                // 确保桌面目录存在（虽然桌面通常一定存在，但加上更稳健）
                File dir = new File(DEFAULT_SAVE_DIR);
                if (!dir.exists()) dir.mkdirs();

                // 如果连文件名都没提供，生成一个带时间戳的默认文件名
                if (filePath == null || filePath.trim().isEmpty()) {
                    filePath = DEFAULT_SAVE_DIR + File.separator + "report_" + System.currentTimeMillis() + ".docx";
                } else {
                    filePath = DEFAULT_SAVE_DIR + File.separator + filePath;
                }
            }

            // 2. 根据传入的 fileType 枚举分发到不同的生成逻辑
            switch (fileType) {
                case DOCX:
                    generateDocx(filePath, content);
                    break;
                case XLSX:
                    generateExcel(filePath, content);
                    break;
                case PDF:
                    generatePdf(filePath, content);
                    break;
                case MD:
                    generateMarkdown(filePath, content);
                    break;
                case TXT:
                default:
                    generateTxt(filePath, content);
                    break;
            }
            if (context != null) {
                context.sendProgress("content", "✅ 文档生成成功: " + filePath);
            }
            return "文件生成成功！保存路径：" + filePath;
        } catch (Exception e){
            context.sendProgress("content", "文档生成失败: " + e.getMessage());
            return "文件生成失败：" + e.getMessage();
        }
    }

    // 1. 生成 Word (.docx)
    private void generateDocx(String filePath, String content) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(content);
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
        }
    }

    // 2. 生成 Excel (.xlsx) - 将内容按行拆分写入单元格
    private void generateExcel(String filePath, String content) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("数据报表");
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(lines[i]);
            }
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }
    }

    // 3. 生成 PDF - 将内容包裹在简单的 HTML 标签中转换
    private void generatePdf(String filePath, String content) throws IOException {
        // 简单的 HTML 模板，实际项目中可以使用 FreeMarker 等模板引擎渲染更复杂的样式
        String htmlContent = "<html><body style='font-family: SimSun; font-size: 14px;'><pre>" + content + "</pre></body></html>";

        try (OutputStream os = new FileOutputStream(filePath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            // 解决中文乱码：指定中文字体文件路径（Windows下通常是 simsun.ttc）
            String fontPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "simsun.ttc";
            // 注意：生产环境建议将字体文件放在项目 resources 目录下通过流加载
            builder.useFont(new File(fontPath), "SimSun");
            builder.run();
        }
    }

    // 4. 生成 Markdown (.md) - 本质是带格式的文本
    private void generateMarkdown(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    // 5. 生成纯文本 (.txt)
    private void generateTxt(String filePath, String content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
