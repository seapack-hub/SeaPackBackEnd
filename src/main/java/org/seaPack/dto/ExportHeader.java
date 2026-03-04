package org.seaPack.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExportHeader {
    @NotBlank
    private String label;   // 显示名：如"用户姓名"
    @NotBlank private String field;   // 数据key：如"userName"
    private Integer width;            // 列宽（字符数）
    private String format;            // 格式：date:yyyy-MM-dd, number:#,##0.00
    private Boolean bold = false;     // 是否加粗
    private String align = "left";    // left/center/right
}
