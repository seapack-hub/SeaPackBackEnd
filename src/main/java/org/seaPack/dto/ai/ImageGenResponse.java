package org.seaPack.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 图片生成同步响应 DTO
 * <p>同步模式下直接返回生成的图片列表。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenResponse {
    private List<ImageResult> images;
}
