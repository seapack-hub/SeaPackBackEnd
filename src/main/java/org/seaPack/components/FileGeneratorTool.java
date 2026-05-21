package org.seaPack.components;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.seaPack.dto.AgentContext;
import org.seaPack.service.ProgressService;
import org.springframework.stereotype.Component;
import dev.langchain4j.agent.tool.Tool;

import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class FileGeneratorTool {

    private final ProgressService progressService;

    public FileGeneratorTool(ProgressService progressService) {
        this.progressService = progressService;
    }

    @Tool("将搜集到的资料整理并生成 Word (.docx) 文档保存到指定路径。参数 filePath 需要包含完整的文件名和后缀（如 report.docx），content 为文档的具体文本内容。")
    public String generateReport(String filePath, String content) {

        AgentContext context = progressService.getContext();
        if (context != null) {
            context.sendProgress("content", "📝 正在生成文档: " + filePath);
        }

        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(content);

            // 写入指定位置
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
            if (context != null) {
                context.sendProgress("content", "✅ 文档生成成功: " + filePath);
            }
            return "文件生成成功！保存路径：" + filePath;
        } catch (IOException e) {
            return "文件生成失败：" + e.getMessage();
        }
    }
}
