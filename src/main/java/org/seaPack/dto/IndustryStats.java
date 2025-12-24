package org.seaPack.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 行业统计信息DTO
 */
@Data
@Builder
public class IndustryStats {
    private Long totalCount;
    private java.time.LocalDateTime generatedTime;
}
