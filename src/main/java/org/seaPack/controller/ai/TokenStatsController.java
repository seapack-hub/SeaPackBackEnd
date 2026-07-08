package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.*;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.service.ai.TokenStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Token 用量统计控制器
 * <p>提供概览、趋势、模型占比、场景柱状图、费用汇总及最近调用记录接口。</p>
 */
@RestController
@RequestMapping("/ai/token-stats")
public class TokenStatsController {

    @Autowired
    private TokenStatsService tokenStatsService;

    /**
     * 概览统计
     * <p>返回今日与昨日的调用次数、Token 总数、费用及成功率对比。</p>
     */
    @GetMapping("/overview")
    public TokenStatOverview overview() {
        return tokenStatsService.getOverview();
    }

    /**
     * 趋势数据（按天聚合）
     *
     * @param startDate 起始日期 YYYY-MM-DD
     * @param endDate   结束日期 YYYY-MM-DD
     * @param modelName 模型编码（可选）
     * @param moduleKey 模块标识（可选）
     */
    @GetMapping("/trend")
    public List<TokenTrendItem> trend(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String moduleKey) {
        return tokenStatsService.getTrend(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 模型占比
     */
    @GetMapping("/model-pie")
    public List<TokenModelPieItem> modelPie(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String moduleKey) {
        return tokenStatsService.getModelPie(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 场景调用柱状图
     */
    @GetMapping("/scene-bar")
    public List<TokenSceneBarItem> sceneBar(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String moduleKey) {
        return tokenStatsService.getSceneBar(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 费用汇总表（按模型聚合）
     */
    @GetMapping("/cost-summary")
    public List<TokenCostSummaryItem> costSummary(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String moduleKey) {
        return tokenStatsService.getCostSummary(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 最近调用记录（分页）
     */
    @GetMapping("/recent-calls")
    public PageInfo<TokenUsageLog> recentCalls(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String moduleKey,
            @RequestParam(required = false) String status) {
        return tokenStatsService.getRecentCalls(pageNum, pageSize, startDate, endDate, modelName, moduleKey, status);
    }
}
