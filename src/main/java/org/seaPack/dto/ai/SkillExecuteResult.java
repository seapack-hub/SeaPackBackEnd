package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;

/**
 * 技能执行结果
 * <p>封装技能执行的输出文本和统计元数据。</p>
 */
@Data
public class SkillExecuteResult {

    /** 拼接的执行结果文本 */
    private String output;

    /** 总技能数（Agent 关联的已启用技能） */
    private int totalSkillCount;

    /** 成功执行数 */
    private int executedCount;

    /** 失败数 */
    private int failedCount;

    /** 执行的技能名称列表 */
    private List<String> skillNames;

    /** 执行耗时（毫秒） */
    private long durationMs;
}
