package org.seaPack.components;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.seaPack.dto.ExportHeader;
import org.seaPack.dto.ExportRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ExcelExportUtil {

    // 公式注入防护：对危险前缀添加单引号转义
    private static final Pattern FORMULA_PATTERN = Pattern.compile("^[=+-@].*");

    public Workbook generateWorkbook(ExportRequest request) {
        Workbook workbook = new XSSFWorkbook(); // 小数据量用XSSFWorkbook；>5000行建议改用SXSSFWorkbook
        Sheet sheet = workbook.createSheet(request.getSheetName());

        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // 写入表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < request.getHeaders().size(); i++) {
            ExportHeader header = request.getHeaders().get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header.getLabel());
            cell.setCellStyle(headerStyle);
            if (header.getWidth() != null) {
                sheet.setColumnWidth(i, Math.min(header.getWidth() * 256, 20000)); // 限制最大列宽
            }
        }

        // 写入数据
        for (int i = 0; i < request.getDataList().size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> data = request.getDataList().get(i);
            for (int j = 0; j < request.getHeaders().size(); j++) {
                ExportHeader header = request.getHeaders().get(j);
                Cell cell = row.createCell(j);
                Object val = data.get(header.getField());
                setCellValue(cell, val, header.getFormat());
                cell.setCellStyle(dataStyle);
            }
        }

        // 自动列宽（谨慎使用，大数据量耗时）
        if (Boolean.TRUE.equals(request.getAutoWidth()) && request.getDataList().size() < 1000) {
            for (int i = 0; i < request.getHeaders().size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i), 20000));
            }
        }

        // 可选：添加备注（操作人、导出时间）
        if (StrUtil.isNotBlank(request.getCreator())) {
            int noteRow = request.getDataList().size() + 2;
            Row noteRowObj = sheet.createRow(noteRow);
            Cell noteCell = noteRowObj.createCell(1); // B列
            noteCell.setCellValue(String.format("导出人：%s | 导出时间：%s",
                    request.getCreator(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            // 可选：设置浅灰底色提升可读性
            CellStyle noteStyle = workbook.createCellStyle();
            noteStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            noteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            noteCell.setCellStyle(noteStyle);
        }

        return workbook;
    }

    private void setCellValue(Cell cell, Object value, String format) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        String strVal = String.valueOf(value).trim();

        // 格式化处理（示例）
        if (StrUtil.isNotBlank(format)) {
            if (format.startsWith("date:") && value instanceof String) {
                try {
                    // 前端传ISO时间，后端格式化
                    cell.setCellValue(LocalDate.parse((String) value));
                    // 可进一步设置CellStyle.setDataFormat(...)
                    return;
                } catch (Exception e) {
                    log.warn("日期格式化失败: {}", value);
                }
            } else if (format.startsWith("number:") && NumberUtils.isCreatable(strVal)) {
                cell.setCellValue(Double.parseDouble(strVal));
                return;
            }
        }

        cell.setCellValue(strVal);
    }

    // 样式创建方法（略，含边框/对齐/字体等）
    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
