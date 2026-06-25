package org.seaPack.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单张图片结果 DTO
 * <p>包含可访问的图片 URL 及尺寸信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResult {
    private String url;
    private Integer width;
    private Integer height;
}
