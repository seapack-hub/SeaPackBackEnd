package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 图片生成请求 DTO
 * <p>对应前端 POST /api/ai/generate-image 的请求体，
 * 格式兼容 OpenAI DALL-E 3 API。</p>
 */
@Data
public class ImageGenRequest {
    private String prompt;
    private Integer n = 1;
    private String size = "1024x1024";
    private String style = "vivid";
}
