package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;

/**
 * 链路追踪快照 DTO
 * <p>包含完整的调用链路步骤列表和汇总指标。</p>
 */
@Data
public class AgentTraceSnapshot {

    /** 调用链路步骤列表 */
    private List<AgentTraceStep> steps;

    /** 总耗时（毫秒） */
    private Long totalDurationMs;

    /** Token 汇总 */
    private TotalTokens totalTokens;

    @Data
    public static class TotalTokens {
        private Integer prompt;
        private Integer completion;
    }
}
