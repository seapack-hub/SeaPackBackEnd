package org.seaPack.service.workflow;

import org.seaPack.mapper.workflow.WorkflowDefinitionMapper;
import org.seaPack.mapper.workflow.WorkflowInstanceMapper;
import org.seaPack.mapper.workflow.WorkflowStatsMapper;
import org.seaPack.model.workflow.WorkflowStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 工作流统计服务
 * <p>提供工作流执行统计的总览、按工作流/日期维度查询、热门排行榜等功能。</p>
 */
@Service
public class WorkflowStatsService {

    @Autowired
    private WorkflowStatsMapper statsMapper;

    @Autowired
    private WorkflowDefinitionMapper definitionMapper;

    @Autowired
    private WorkflowInstanceMapper instanceMapper;

    /** 线程安全的日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 总览统计
     * <p>聚合工作流定义数、实例总数、运行中实例数、今日实例数、成功率、平均耗时。</p>
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        // 工作流总数（从 definition 表统计）
        // 注意：这里简化处理，使用 statsMapper 聚合
        // 实际项目中可直接用 definitionMapper 做 count 查询
        List<WorkflowStats> allStats = statsMapper.selectByDateRange(null, null);

        // 统计维度
        Set<Long> workflowIds = new HashSet<>();
        long totalInstances = 0;
        long runningInstances = 0;
        long todayInstances = 0;
        long successCount = 0;
        long totalDurationMs = 0;
        long durationCount = 0;

        String today = LocalDate.now().format(DATE_FORMATTER);

        for (WorkflowStats stat : allStats) {
            workflowIds.add(stat.getWorkflowId());
            totalInstances += stat.getTotalRuns() != null ? stat.getTotalRuns() : 0;
            successCount += stat.getSuccessCount() != null ? stat.getSuccessCount() : 0;
            runningInstances += stat.getRunningCount() != null ? stat.getRunningCount() : 0;

            if (stat.getAvgDurationMs() != null && stat.getTotalRuns() != null) {
                totalDurationMs += (long) stat.getAvgDurationMs() * stat.getTotalRuns();
                durationCount += stat.getTotalRuns();
            }

            // 今日统计
            if (stat.getStatDate() != null) {
                String statDateStr = stat.getStatDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FORMATTER);
                if (statDateStr.equals(today)) {
                    todayInstances += stat.getTotalRuns() != null ? stat.getTotalRuns() : 0;
                }
            }
        }

        overview.put("totalWorkflows", workflowIds.size());
        overview.put("totalInstances", totalInstances);
        overview.put("runningInstances", runningInstances);
        overview.put("todayInstances", todayInstances);
        overview.put("successRate", totalInstances > 0 ? Math.round(successCount * 100.0 / totalInstances) : 0);
        overview.put("avgDurationMs", durationCount > 0 ? totalDurationMs / durationCount : 0);

        return overview;
    }

    /**
     * 按工作流查询统计
     * @param workflowId 工作流ID（可选，null 则查全部）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计列表
     */
    public List<WorkflowStats> getByWorkflow(Long workflowId, String startDate, String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        return statsMapper.selectByWorkflowAndDate(workflowId, start, end);
    }

    /**
     * 按日期查询统计
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计列表
     */
    public List<WorkflowStats> getByDate(String startDate, String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        return statsMapper.selectByDateRange(start, end);
    }

    /**
     * 热门工作流排行
     * @param limit 返回条数，默认 10
     * @return 排行列表
     */
    public List<Map<String, Object>> getTopWorkflows(int limit) {
        List<WorkflowStats> statsList = statsMapper.selectTopWorkflows(limit);
        List<Map<String, Object>> result = new ArrayList<>();

        for (WorkflowStats stat : statsList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("workflowId", stat.getWorkflowId());
            item.put("workflowName", stat.getWorkflowName());
            item.put("totalRuns", stat.getTotalRuns());
            // 成功率 = successCount / totalRuns * 100
            int totalRuns = stat.getTotalRuns() != null ? stat.getTotalRuns() : 0;
            int successCount = stat.getSuccessCount() != null ? stat.getSuccessCount() : 0;
            item.put("successRate", totalRuns > 0 ? Math.round(successCount * 100.0 / totalRuns) : 0);
            result.add(item);
        }

        return result;
    }

    /**
     * 记录工作流执行统计（供 WorkflowInstanceService 在实例完成时调用）
     * @param workflowId 工作流ID
     * @param durationMs 执行耗时（毫秒）
     * @param success 是否成功
     */
    @Transactional
    public void recordExecution(Long workflowId, Integer durationMs, boolean success) {
        WorkflowStats stats = new WorkflowStats();
        stats.setWorkflowId(workflowId);
        stats.setStatDate(new Date());
        stats.setTotalRuns(1);
        stats.setSuccessCount(success ? 1 : 0);
        stats.setFailedCount(success ? 0 : 1);
        stats.setRunningCount(0);
        stats.setAvgDurationMs(durationMs);
        stats.setMaxDurationMs(durationMs);
        stats.setMinDurationMs(durationMs);
        stats.setTotalTokens(0);
        stats.setHumanTasksCount(0);

        statsMapper.upsert(stats);
    }

    // ===== 内部方法 =====

    /** 解析日期字符串，返回 Date，格式 yyyy-MM-dd */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }
}
