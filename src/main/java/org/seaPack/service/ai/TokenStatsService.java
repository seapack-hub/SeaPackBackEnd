package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.*;
import org.seaPack.mapper.ai.TokenUsageDailyMapper;
import org.seaPack.mapper.ai.TokenUsageLogMapper;
import org.seaPack.model.ai.TokenUsageDaily;
import org.seaPack.model.ai.TokenUsageLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Token 用量统计服务
 * <p>提供概览、趋势、模型占比、场景柱状图、费用汇总及调用明细查询等功能。</p>
 */
@Service
public class TokenStatsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private TokenUsageDailyMapper dailyMapper;

    @Autowired
    private TokenUsageLogMapper logMapper;

    /**
     * 概览统计
     * <p>分别查询今日和昨日的聚合数据，返回对比结果。</p>
     */
    public TokenStatOverview getOverview() {
        String today = LocalDate.now().format(DATE_FMT);
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FMT);
        List<TokenStatOverview> list = dailyMapper.selectOverview(today, yesterday);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        // 无数据时返回空默认值
        return new TokenStatOverview();
    }

    /**
     * 趋势数据（按天聚合）
     */
    public List<TokenTrendItem> getTrend(String startDate, String endDate, String modelName, String moduleKey) {
        return dailyMapper.selectTrend(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 模型占比
     */
    public List<TokenModelPieItem> getModelPie(String startDate, String endDate, String modelName, String moduleKey) {
        return dailyMapper.selectModelPie(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 场景调用柱状图
     */
    public List<TokenSceneBarItem> getSceneBar(String startDate, String endDate, String modelName, String moduleKey) {
        return dailyMapper.selectSceneBar(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 费用汇总表（按模型聚合）
     */
    public List<TokenCostSummaryItem> getCostSummary(String startDate, String endDate, String modelName, String moduleKey) {
        return dailyMapper.selectCostSummary(startDate, endDate, modelName, moduleKey);
    }

    /**
     * 最近调用记录（分页）
     */
    public PageInfo<TokenUsageLog> getRecentCalls(int pageNum, int pageSize, String startDate, String endDate,
                                                  String modelName, String moduleKey, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<TokenUsageLog> list = logMapper.selectList(startDate, endDate, modelName, moduleKey, status);
        return new PageInfo<>(list);
    }

    /**
     * 记录一次 AI 调用（写入明细 + 聚合到日统计）
     * <p>供 SkillService、PromptTemplateService、AgentService 等在执行后调用。</p>
     *
     * @param log 调用明细
     */
    @Transactional
    public void recordCall(TokenUsageLog log) {
        if (log == null) return;

        // 1. 写入明细表
        logMapper.insert(log);

        // 2. 聚合到日统计表
        TokenUsageDaily daily = new TokenUsageDaily();
        daily.setStatDate(log.getCallTime());
        daily.setModelName(log.getModelName());
        daily.setAgentId(log.getAgentId());
        daily.setSkillId(log.getSkillId());
        daily.setSceneId(log.getSceneId());
        daily.setModuleKey(log.getModuleKey());
        daily.setCallCount(1);
        daily.setSuccessCount("success".equals(log.getStatus()) ? 1 : 0);
        daily.setFailCount("fail".equals(log.getStatus()) ? 1 : 0);
        daily.setTokensInput(log.getTokensInput() != null ? log.getTokensInput().longValue() : 0L);
        daily.setTokensOutput(log.getTokensOutput() != null ? log.getTokensOutput().longValue() : 0L);
        daily.setTokensTotal(daily.getTokensInput() + daily.getTokensOutput());
        daily.setTotalDurationMs(log.getDurationMs() != null ? log.getDurationMs().longValue() : 0L);
        daily.setTotalCostYuan(log.getCostYuan());

        // 尝试更新已有记录，不存在则插入
        int updated = dailyMapper.updateByUniqueKey(daily);
        if (updated == 0) {
            dailyMapper.insert(daily);
        }
    }
}
