package org.seaPack.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件解析工具类
 * 核心功能：根据文件后缀名，自动解析 TXT / PDF / DOCX 格式文件，提取纯文本内容
 */
@Component
@Slf4j
public class FileParserUtil {

    /**
     * 根据文件后缀解析文件内容（对外提供的核心方法）
     * @param inputStream 文件输入流（读取文件的数据源）
     * @param fileName 文件名（用于判断文件格式）
     * @return 解析后的纯文本字符串
     * @throws Exception 解析过程中可能抛出的IO异常、格式不支持异常等
     */
    public static String parseFile(InputStream inputStream, String fileName) throws Exception {
        // 将文件名转为小写，统一判断文件后缀，避免大小写干扰（如.TXT、.Pdf）
        String lowerName = fileName.toLowerCase();

        // 判断是否为TXT文件
        if (lowerName.endsWith(".txt")) {
            // 读取输入流所有字节，直接转为字符串（TXT纯文本解析）
            return new String(inputStream.readAllBytes());
        }
        // 判断是否为PDF文件
        else if (lowerName.endsWith(".pdf")) {
            // 调用私有方法，专门解析PDF文件
            return parsePdf(inputStream);
        }
        // 判断是否为DOCX文件（Word 2007及以上版本）
        else if (lowerName.endsWith(".docx")) {
            // 调用私有方法，专门解析DOCX文件
            return parseDocx(inputStream);
        }
        // 不支持的文件格式，抛出非法参数异常
        else {
            throw new IllegalArgumentException("不支持的文件格式: " + fileName);
        }
    }

    /**
     * 私有方法：专门解析PDF文件，提取文本内容
     * @param inputStream PDF文件输入流
     * @return PDF解析后的纯文本
     * @throws Exception PDF解析异常、IO异常
     */
    private static String parsePdf(InputStream inputStream) throws Exception {
        // try-with-resources语法：自动关闭PDDocument资源，避免内存泄漏
        // 加载输入流，创建PDF文档对象
        try (PDDocument document = PDDocument.load(inputStream)) {
            // 创建PDF文本提取器
            PDFTextStripper stripper = new PDFTextStripper();
            // 从PDF文档中提取所有纯文本并返回
            return stripper.getText(document);
        }
    }

    /**
     * 私有方法：专门解析DOCX文件（Word文档），提取文本内容
     * @param inputStream DOCX文件输入流
     * @return DOCX解析后的纯文本（段落用换行分隔）
     * @throws Exception DOCX解析异常、IO异常
     */
    private static String parseDocx(InputStream inputStream) throws Exception {
        // try-with-resources语法：自动关闭XWPFDocument资源
        // 加载输入流，创建DOCX文档对象
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            // 流式处理：获取所有段落 -> 提取段落文本 -> 过滤空文本 -> 收集为List集合
            List<String> texts = document.getParagraphs().stream()
                    // 将每个段落对象转换为文本内容
                    .map(XWPFParagraph::getText)
                    // 过滤：只保留非null、非空白的文本
                    .filter(text -> text != null && !text.trim().isEmpty())
                    // 将有效文本收集到List集合中
                    .collect(Collectors.toList());

            // 将所有有效段落用换行符\n拼接，返回完整文本
            return String.join("\n", texts);
        }
    }
}