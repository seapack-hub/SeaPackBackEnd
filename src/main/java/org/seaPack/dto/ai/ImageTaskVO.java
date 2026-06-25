package org.seaPack.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 图片生成异步任务状态 DTO
 * <p>异步模式下通过 taskId 轮询任务进度。
 * status 取值：processing | completed | failed。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageTaskVO {
    private String taskId;
    private String status;
    private List<ImageResult> images;
    private String error;
}
