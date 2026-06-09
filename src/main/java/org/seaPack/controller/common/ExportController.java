package org.seaPack.controller.common;

import jakarta.servlet.http.HttpServletResponse; // HTTP 响应（用于文件下载）
import jakarta.validation.ConstraintViolation; // Bean Validation 校验约束违规
import jakarta.validation.Valid; // 启用参数校验
import jakarta.validation.Validator; // Bean Validator 校验器
import lombok.RequiredArgsConstructor; // 生成全参构造器（final 字段）
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.apache.poi.ss.usermodel.Workbook; // POI Excel 工作簿
import org.seaPack.components.ExcelExportUtil; // Excel 导出工具
import org.seaPack.dto.common.ExportRequest; // 导出请求 DTO
import org.springframework.web.bind.annotation.PostMapping; // POST 请求映射
import org.springframework.web.bind.annotation.RequestBody; // 请求体绑定
import org.springframework.web.bind.annotation.RequestMapping; // 请求路径映射
import org.springframework.web.bind.annotation.RestController; // REST 控制器

import java.net.URLEncoder; // URL 编码
import java.nio.charset.StandardCharsets; // UTF-8 字符集常量
import java.util.Set; // Set 集合

/**
 * Excel 导出控制器
 * 接收导出请求，动态生成 Excel 文件并输出到 HTTP 响应流供前端下载。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/export") // 请求基础路径
@RequiredArgsConstructor // 生成构造器注入
public class ExportController {

    private final ExcelExportUtil excelExportUtil; // Excel 导出工具
    private final Validator validator; // Jakarta Bean Validator

    /**
     * 导出 Excel 文件到 HTTP 响应流
     * @param request  导出请求（含表头定义、数据行、文件名等）
     * @param response HTTP 响应（输出 Excel 文件流供下载）
     */
    @PostMapping("/excel")
    public void exportExcel(
            @Valid @RequestBody ExportRequest request,
            HttpServletResponse response) throws Exception {

        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }

        Workbook workbook = null;
        try {
            workbook = excelExportUtil.generateWorkbook(request);

            String safeName = request.getFileName()
                    .replaceAll("[\\\\/:*?\"<>|]", "_")
                    .replaceAll("\\s+", "_");
            String encodedName = URLEncoder.encode(safeName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedName + ".xlsx\"; filename*=UTF-8''" + encodedName + ".xlsx");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (Exception e) { log.warn("Workbook关闭异常", e); }
            }
        }
    }
}