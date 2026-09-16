package org.seaPack.service.document;

import lombok.Data;

import java.util.Map;

/**
 * 文档生成请求 DTO
 * <p>前端/技能系统调用 /api/v1/documents/generate 时传递。</p>
 */
@Data
public class DocumentGenerateRequest {

    /** 文档类型（如 markdown、word_doc、excel_report 等） */
    private String templateType;

    /** 文档生成参数（动态键值对，由 templateType 决定具体含义） */
    private Map<String, Object> params;
}
