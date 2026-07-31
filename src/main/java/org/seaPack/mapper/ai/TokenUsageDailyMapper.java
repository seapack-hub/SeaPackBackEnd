package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.dto.ai.*;
import org.seaPack.model.ai.TokenUsageDaily;

import java.util.List;

/**
 * Token 日统计汇总 Mapper
 */
@Mapper
public interface TokenUsageDailyMapper {

    /**
     * 概览统计：按日期分组汇总
     * <p>返回今日和昨日的聚合数据</p>
     */
    List<TokenStatOverview> selectOverview(@Param("today") String today, @Param("yesterday") String yesterday);

    /**
     * 趋势数据：按天分组聚合
     *
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @param userId    用户ID（可选）
     * @param bizType   用途（可选）
     * @param modelName 模型编码（可选）
     * @return 趋势数据列表
     */
    List<TokenTrendItem> selectTrend(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("userId") Long userId,
                                     @Param("bizType") String bizType,
                                     @Param("modelName") String modelName);

    /**
     * 模型占比：按 model_name 分组聚合
     */
    List<TokenModelPieItem> selectModelPie(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("userId") Long userId,
                                           @Param("bizType") String bizType,
                                           @Param("modelName") String modelName);

    /**
     * 场景柱状图：按 scene_id 分组聚合，JOIN ai_scene 获取名称
     */
    List<TokenSceneBarItem> selectSceneBar(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("userId") Long userId,
                                           @Param("bizType") String bizType,
                                           @Param("modelName") String modelName);

    /**
     * 费用汇总：按 model_name 分组聚合
     */
    List<TokenCostSummaryItem> selectCostSummary(@Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("userId") Long userId,
                                                 @Param("bizType") String bizType,
                                                 @Param("modelName") String modelName);

    /**
     * 用户 Token 消耗排行
     *
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @param limit     返回条数
     * @return 用户排行列表
     */
    List<TokenUserRankItem> selectUserRanking(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("limit") int limit);

    /**
     * 插入日统计记录
     */
    int insert(TokenUsageDaily record);

    /**
     * 按唯一键更新聚合数据（upsert）
     */
    int updateByUniqueKey(TokenUsageDaily record);
}
