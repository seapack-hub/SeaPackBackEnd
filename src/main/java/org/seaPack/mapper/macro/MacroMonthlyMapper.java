package org.seaPack.mapper.macro;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.macro.MacroMonthly;

import java.util.Date;
import java.util.List;

@Mapper
public interface MacroMonthlyMapper {

    /** 批量插入（INSERT IGNORE，唯一键冲突自动跳过） */
    int batchInsertIgnore(@Param("list") List<MacroMonthly> list);

    /** 按指标编码+日期范围查询 */
    List<MacroMonthly> selectByIndicatorAndDateRange(
            @Param("indicatorCode") String indicatorCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dataVersion") Integer dataVersion);

    /** 按多个指标编码+日期范围查询（用于pivot） */
    List<MacroMonthly> selectByIndicatorsAndDateRange(
            @Param("indicatorCodes") List<String> indicatorCodes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("dataVersion") Integer dataVersion);

    /** 查询所有不同指标编码 */
    List<String> selectDistinctIndicatorCodes();

    /** 按日期+指标编码+版本查询单条（用于upsert判断） */
    MacroMonthly selectByUniqueKey(
            @Param("statDate") Date statDate,
            @Param("indicatorCode") String indicatorCode,
            @Param("dataVersion") Integer dataVersion);

    /** 更新指标值 */
    int updateMetricValue(@Param("record") MacroMonthly record);

    /** 按日期+指标编码删除 */
    int deleteByDateAndIndicator(
            @Param("statDate") Date statDate,
            @Param("indicatorCode") String indicatorCode);

    /** 按日期范围删除 */
    int deleteByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("indicatorCode") String indicatorCode);
}
