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
 * <p>提供概览、趋势、模型占比、场景柱状图、费用汇总、用户排行及最近调用记录接口。</p>
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
     * @param userId    用户ID（可选）
     * @param bizType   用途（可选）：orchestration / agent / chat / skill
     * @param modelName 模型编码（可选）
     */
    @GetMapping("/trend")
    public List<TokenTrendItem> trend(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String modelName) {
        return tokenStatsService.getTrend(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 模型占比
     */
    @GetMapping("/model-pie")
    public List<TokenModelPieItem> modelPie(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String modelName) {
        return tokenStatsService.getModelPie(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 场景调用柱状图
     */
    @GetMapping("/scene-bar")
    public List<TokenSceneBarItem> sceneBar(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String modelName) {
        return tokenStatsService.getSceneBar(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 费用汇总表（按模型聚合）
     */
    @GetMapping("/cost-summary")
    public List<TokenCostSummaryItem> costSummary(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String modelName) {
        return tokenStatsService.getCostSummary(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 用户 Token 消耗排行
     *
     * @param startDate 起始日期 YYYY-MM-DD
     * @param endDate   结束日期 YYYY-MM-DD
     * @param limit     返回条数（默认10）
     */
    @GetMapping("/user-ranking")
    public List<TokenUserRankItem> userRanking(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return tokenStatsService.getUserRanking(startDate, endDate, limit);
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
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String status) {
        return tokenStatsService.getRecentCalls(pageNum, pageSize, startDate, endDate,
                userId, bizType, modelName, status);
    }
}
