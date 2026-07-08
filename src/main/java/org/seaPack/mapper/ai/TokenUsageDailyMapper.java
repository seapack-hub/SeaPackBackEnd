package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.dto.ai.*;
import org.seaPack.model.ai.TokenUsageDaily;

import java.util.Date;
import java.util.List;

@Mapper
public interface TokenUsageDailyMapper {

    /**
     * 概览统计：按日期分组汇总
     * <p>返回今日和昨日的聚合数据</p>
     */
    List<TokenStatOverview> selectOverview(@Param("today") String today, @Param("yesterday") String yesterday);

    /**
     * 趋势数据：按天分组聚合
     */
    List<TokenTrendItem> selectTrend(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("modelName") String modelName,
                                     @Param("moduleKey") String moduleKey);

    /**
     * 模型占比：按 model_name 分组聚合
     */
    List<TokenModelPieItem> selectModelPie(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("modelName") String modelName,
                                           @Param("moduleKey") String moduleKey);

    /**
     * 场景柱状图：按 scene_id 分组聚合，JOIN ai_scene 获取名称
     */
    List<TokenSceneBarItem> selectSceneBar(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("modelName") String modelName,
                                           @Param("moduleKey") String moduleKey);

    /**
     * 费用汇总：按 model_name 分组聚合
     */
    List<TokenCostSummaryItem> selectCostSummary(@Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("modelName") String modelName,
                                                 @Param("moduleKey") String moduleKey);

    /**
     * 插入日统计记录
     */
    int insert(TokenUsageDaily record);

    /**
     * 按唯一键更新聚合数据（upsert）
     */
    int updateByUniqueKey(TokenUsageDaily record);
}
