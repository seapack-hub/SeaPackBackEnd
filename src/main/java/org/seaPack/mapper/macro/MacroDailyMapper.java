package org.seaPack.mapper.macro;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.macro.MacroDaily;

import java.util.Date;
import java.util.List;

@Mapper
public interface MacroDailyMapper {

    int batchInsertIgnore(@Param("list") List<MacroDaily> list);

    List<MacroDaily> selectByIndicatorAndDateRange(
            @Param("indicatorCode") String indicatorCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<MacroDaily> selectByIndicatorsAndDateRange(
            @Param("indicatorCodes") List<String> indicatorCodes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<String> selectDistinctIndicatorCodes();

    MacroDaily selectByUniqueKey(
            @Param("statDate") Date statDate,
            @Param("indicatorCode") String indicatorCode);

    int updateMetricValue(@Param("record") MacroDaily record);

    int deleteByDateAndIndicator(
            @Param("statDate") Date statDate,
            @Param("indicatorCode") String indicatorCode);

    int deleteByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("indicatorCode") String indicatorCode);
}
