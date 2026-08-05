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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
/**
 * Token 用量统计服务
 * <p>提供概览、趋势、模型占比、场景柱状图、费用汇总、用户排行及调用明细查询等功能。
 * recordCall() 在每次 LLM 调用完成后由各对话服务调用，同时写入明细表和日统计表。</p>
 */
@Service
public class TokenStatsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private TokenUsageDailyMapper dailyMapper;

    @Autowired
    private TokenUsageLogMapper logMapper;

    @Autowired
    private TokenQuotaService tokenQuotaService;

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
        return new TokenStatOverview();
    }

    /**
     * 趋势数据（按天聚合）
     */
    public List<TokenTrendItem> getTrend(String startDate, String endDate,
                                          Long userId, String bizType, String modelName) {
        return dailyMapper.selectTrend(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 模型占比
     */
    public List<TokenModelPieItem> getModelPie(String startDate, String endDate,
                                                Long userId, String bizType, String modelName) {
        return dailyMapper.selectModelPie(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 场景调用柱状图
     */
    public List<TokenSceneBarItem> getSceneBar(String startDate, String endDate,
                                                Long userId, String bizType, String modelName) {
        return dailyMapper.selectSceneBar(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 费用汇总表（按模型聚合）
     */
    public List<TokenCostSummaryItem> getCostSummary(String startDate, String endDate,
                                                      Long userId, String bizType, String modelName) {
        return dailyMapper.selectCostSummary(startDate, endDate, userId, bizType, modelName);
    }

    /**
     * 用户 Token 消耗排行
     *
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @param limit     返回条数（默认10）
     * @return 用户排行列表
     */
    public List<TokenUserRankItem> getUserRanking(String startDate, String endDate, int limit) {
        if (limit <= 0) limit = 10;
        return dailyMapper.selectUserRanking(startDate, endDate, limit);
    }

    /**
     * 最近调用记录（分页）
     */
    public PageInfo<TokenUsageLog> getRecentCalls(int pageNum, int pageSize,
                                                  String startDate, String endDate,
                                                  Long userId, String bizType,
                                                  String modelName, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<TokenUsageLog> list = logMapper.selectList(startDate, endDate, userId, bizType, modelName, status);
        return new PageInfo<>(list);
    }

    /**
     * 记录一次 LLM 调用（写入明细 + 实时聚合到日统计）
     * <p>每次 LLM 调用完成后由各对话服务调用，同时写入 ai_token_usage_log 和 ai_token_usage_daily。</p>
     *
     * @param log 调用明细（必须包含 userId、bizType、modelName 等核心字段）
     */
    public void recordCall(TokenUsageLog log) {
        if (log == null) return;

        // 1. 写入明细表
        logMapper.insert(log);

        // 2. 实时聚合到日统计表
        TokenUsageDaily daily = new TokenUsageDaily();
        daily.setStatDate(log.getCallTime() != null ? log.getCallTime() : new Date());
        daily.setUserId(log.getUserId());
        daily.setModelName(log.getModelName());
        daily.setBizType(log.getBizType());
        daily.setSceneId(log.getSceneId());
        daily.setAgentId(log.getAgentId());
        daily.setSkillId(log.getSkillId());
        daily.setCallCount(1);
        daily.setSuccessCount("success".equals(log.getStatus()) ? 1 : 0);
        daily.setFailCount("fail".equals(log.getStatus()) ? 1 : 0);
        daily.setTokensInput(log.getTokensInput() != null ? log.getTokensInput().longValue() : 0L);
        daily.setTokensOutput(log.getTokensOutput() != null ? log.getTokensOutput().longValue() : 0L);
        daily.setTokensTotal(daily.getTokensInput() + daily.getTokensOutput());
        daily.setTotalDurationMs(log.getDurationMs() != null ? log.getDurationMs().longValue() : 0L);
        daily.setTotalCostYuan(log.getCostYuan() != null ? log.getCostYuan() : BigDecimal.ZERO);

        // 尝试更新已有记录，不存在则插入
        int updated = dailyMapper.updateByUniqueKey(daily);
        if (updated == 0) {
            dailyMapper.insert(daily);
        }

        // 3. 扣减用户额度（记录到 ai_user_token_usage 表，供额度校验使用）
        try {
            long totalTokens = daily.getTokensInput() + daily.getTokensOutput();
            if (totalTokens > 0) {
                tokenQuotaService.recordUsage(log.getUserId(), totalTokens);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(TokenStatsService.class)
                    .error("额度扣减失败: userId={}", log.getUserId(), e);
        }
    }
}
