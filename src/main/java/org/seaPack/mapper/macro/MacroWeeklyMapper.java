package org.seaPack.mapper.macro;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.macro.MacroWeekly;

import java.util.Date;
import java.util.List;

@Mapper
public interface MacroWeeklyMapper {

    int batchInsertIgnore(@Param("list") List<MacroWeekly> list);

    List<MacroWeekly> selectByIndicatorAndDateRange(
            @Param("indicatorCode") String indicatorCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<MacroWeekly> selectByIndicatorsAndDateRange(
            @Param("indicatorCodes") List<String> indicatorCodes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<String> selectDistinctIndicatorCodes();

    MacroWeekly selectByUniqueKey(
            @Param("statDate") Date statDate,
            @Param("indicatorCode") String indicatorCode);

    int updateMetricValue(@Param("record") MacroWeekly record);

    int deleteByDateAndIndicator(
            @Param("statDate") Date statDate,
            @Param("indicatorCode") String indicatorCode);

    int deleteByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("indicatorCode") String indicatorCode);
}
