package org.seaPack.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.seaPack.components.ExcelExportUtil;
import org.seaPack.dto.ExportRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExcelExportUtil excelExportUtil;
    private final Validator validator;

    @PostMapping("/excel")
    public void exportExcel(
            @Valid @RequestBody ExportRequest request,
            HttpServletResponse response) throws Exception {

        // 二次校验（防止绕过前端校验）
        Set<ConstraintViolation<ExportRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }

        Workbook workbook = null;
        try {
            workbook = excelExportUtil.generateWorkbook(request);

            // 安全文件名：移除非法字符 + URL编码
            String safeName = request.getFileName()
                    .replaceAll("[\\\\/:*?\"<>|]", "_")
                    .replaceAll("\\s+", "_");
            String encodedName = URLEncoder.encode(safeName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 响应头
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
