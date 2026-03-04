package org.seaPack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExportRequest {
    @NotBlank(message = "文件名不能为空")
    @Pattern(regexp = "^[\\w\\u4e00-\\u9fa5-]{1,100}$", message = "文件名仅支持中英文、数字、下划线、短横线")
    private String fileName = "导出文件";

    private String sheetName = "Sheet1";

    @Size(min = 1, max = 50, message = "表头列数需在1-50之间")
    private List<ExportHeader> headers;

    @Size(max = 10000, message = "数据行数不能超过10000行") // 防止内存溢出
    private List<Map<String, Object>> dataList;

    private Boolean autoWidth = true; // 是否自动列宽
    private String creator; // 操作人（用于水印/备注）
}
