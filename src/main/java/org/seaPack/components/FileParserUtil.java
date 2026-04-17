package org.seaPack.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
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

        try {
            if (lowerName.endsWith(".txt")) {
                return parseTxt(inputStream);
            } else if (lowerName.endsWith(".pdf")) {
                return parsePdf(inputStream);
            } else if (lowerName.endsWith(".docx")) {
                return parseDocx(inputStream);
            } else {
                throw new IllegalArgumentException("不支持的文件格式: " + fileName);
            }
        } catch (Exception e) {
            // 统一捕获异常，打印详细日志，方便排查是哪个文件出的问题
            log.error("文件解析失败: {}", fileName, e);
            throw new Exception("文件解析失败: " + e.getMessage());
        }
    }

    /**
    * TXT 解析：显式指定 UTF-8
    */
    private static String parseTxt(InputStream inputStream) throws Exception {
        // 使用 ByteArrayOutputStream 确保完整读取
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        // 强制使用 UTF-8 转换，防止默认编码导致的乱码
        return result.toString("UTF-8");
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
            if (document.getNumberOfPages() == 0) {
                return "";
            }
            // 创建PDF文本提取器
            PDFTextStripper stripper = new PDFTextStripper();

            // 1. 设置换行符，防止段落粘连
            stripper.setSortByPosition(true); // 按位置排序，保持阅读顺序

            // 2. 获取原始文本（这里会产生 WARN 日志，但不会崩溃）
            String text = stripper.getText(document);

            // 3. 【核心修复】清洗文本
            // PDFBox 2.0 遇到无法映射的字体（如公式）时，通常会输出 '?' 或者乱码符号（如 '????'）
            // 我们需要把这些无效字符清理掉，或者替换为空格，防止后续 RAG 分块出错

            if (text != null) {
                // 替换连续的问号（通常是无法识别的数学符号）为空格
                text = text.replaceAll("\\?{2,}", " ");

                // 替换不可见的控制字符（除了换行和制表符）
                text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

                // 去除多余的空行
                text = text.replaceAll("\n\\s*\n", "\n");
            }

            return text;
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