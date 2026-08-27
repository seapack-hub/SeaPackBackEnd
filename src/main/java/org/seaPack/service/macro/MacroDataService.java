package org.seaPack.service.macro;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.macro.*;
import org.seaPack.model.macro.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = false)
public class MacroDataService {

    @Autowired private MacroMonthlyMapper monthlyMapper;
    @Autowired private MacroDailyMapper dailyMapper;
    @Autowired private MacroWeeklyMapper weeklyMapper;
    @Autowired private SysMacroIndicatorMetaMapper metaMapper;

    // ==================== 查询接口 ====================

    @Transactional(readOnly = true)
    public PageInfo<MacroMonthly> queryMonthly(int pageNum, int pageSize, String indicatorCode, Date startDate, Date endDate, Integer dataVersion) {
        PageHelper.startPage(pageNum, pageSize);
        List<MacroMonthly> list = monthlyMapper.selectByIndicatorAndDateRange(indicatorCode, startDate, endDate, dataVersion);
        return new PageInfo<>(list);
    }

    @Transactional(readOnly = true)
    public PageInfo<MacroDaily> queryDaily(int pageNum, int pageSize, String indicatorCode, Date startDate, Date endDate) {
        PageHelper.startPage(pageNum, pageSize);
        List<MacroDaily> list = dailyMapper.selectByIndicatorAndDateRange(indicatorCode, startDate, endDate);
        return new PageInfo<>(list);
    }

    @Transactional(readOnly = true)
    public PageInfo<MacroWeekly> queryWeekly(int pageNum, int pageSize, String indicatorCode, Date startDate, Date endDate) {
        PageHelper.startPage(pageNum, pageSize);
        List<MacroWeekly> list = weeklyMapper.selectByIndicatorAndDateRange(indicatorCode, startDate, endDate);
        return new PageInfo<>(list);
    }

    /**
     * 统一查询接口：按频率+多个指标+日期范围，返回 pivot 格式
     * 返回: { dates: ["2024-01", ...], series: { "M0": [13.22, ...], "M1": [110.88, ...] } }
     */
    @Transactional(readOnly = true)
    public Map<String, Object> queryPivot(String frequency, List<String> indicatorCodes, Date startDate, Date endDate, Integer dataVersion) {
        List<? extends Object> rawList;
        switch (frequency) {
            case "daily":
                rawList = dailyMapper.selectByIndicatorsAndDateRange(indicatorCodes, startDate, endDate);
                break;
            case "weekly":
                rawList = weeklyMapper.selectByIndicatorsAndDateRange(indicatorCodes, startDate, endDate);
                break;
            default:
                rawList = monthlyMapper.selectByIndicatorsAndDateRange(indicatorCodes, startDate, endDate, dataVersion);
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TreeMap<String, Map<String, BigDecimal>> pivotMap = new TreeMap<>();

        for (Object obj : rawList) {
            String date;
            String code;
            BigDecimal value;
            if (obj instanceof MacroMonthly m) { date = sdf.format(m.getStatDate()); code = m.getIndicatorCode(); value = m.getMetricValue(); }
            else if (obj instanceof MacroDaily d) { date = sdf.format(d.getStatDate()); code = d.getIndicatorCode(); value = d.getMetricValue(); }
            else if (obj instanceof MacroWeekly w) { date = sdf.format(w.getStatDate()); code = w.getIndicatorCode(); value = w.getMetricValue(); }
            else continue;
            pivotMap.computeIfAbsent(date, k -> new LinkedHashMap<>()).put(code, value);
        }

        List<String> dates = new ArrayList<>(pivotMap.keySet());
        Map<String, List<BigDecimal>> series = new LinkedHashMap<>();
        for (String code : indicatorCodes) {
            series.put(code, dates.stream()
                    .map(d -> pivotMap.getOrDefault(d, Collections.emptyMap()).getOrDefault(code, BigDecimal.ZERO))
                    .collect(Collectors.toList()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("series", series);
        return result;
    }

    /** 查询所有指标元数据 */
    @Transactional(readOnly = true)
    public List<SysMacroIndicatorMeta> getAllMeta() {
        return metaMapper.selectAll();
    }

    /** 按频率查询指标元数据 */
    @Transactional(readOnly = true)
    public List<SysMacroIndicatorMeta> getMetaByFrequency(String frequency) {
        return metaMapper.selectByFrequency(frequency);
    }

    /** 查询已使用的指标编码列表 */
    @Transactional(readOnly = true)
    public List<String> getUsedIndicatorCodes(String frequency) {
        switch (frequency) {
            case "daily": return dailyMapper.selectDistinctIndicatorCodes();
            case "weekly": return weeklyMapper.selectDistinctIndicatorCodes();
            default: return monthlyMapper.selectDistinctIndicatorCodes();
        }
    }

    // ==================== 写入接口 ====================

    /** 批量导入月频数据（INSERT IGNORE） */
    @Transactional
    public int importMonthly(List<MacroMonthly> list) {
        if (list == null || list.isEmpty()) return 0;
        return monthlyMapper.batchInsertIgnore(list);
    }

    /** 批量导入日频数据 */
    @Transactional
    public int importDaily(List<MacroDaily> list) {
        if (list == null || list.isEmpty()) return 0;
        return dailyMapper.batchInsertIgnore(list);
    }

    /** 批量导入周频数据 */
    @Transactional
    public int importWeekly(List<MacroWeekly> list) {
        if (list == null || list.isEmpty()) return 0;
        return weeklyMapper.batchInsertIgnore(list);
    }

    // ==================== 单条操作接口 ====================

    /** 保存单条记录（upsert：存在则更新，不存在则插入） */
    @Transactional
    public int saveRecord(String frequency, Date statDate, String indicatorCode,
                          BigDecimal metricValue, BigDecimal metricValue2, BigDecimal momChange,
                          Integer dataVersion, String source, String extra) {
        switch (frequency) {
            case "daily": {
                MacroDaily existing = dailyMapper.selectByUniqueKey(statDate, indicatorCode);
                MacroDaily record = new MacroDaily();
                record.setStatDate(statDate);
                record.setIndicatorCode(indicatorCode);
                record.setMetricValue(metricValue);
                record.setMetricValue2(metricValue2);
                record.setMomChange(momChange);
                record.setSource(source != null ? source : "");
                record.setExtra(extra);
                if (existing != null) {
                    return dailyMapper.updateMetricValue(record);
                } else {
                    dailyMapper.batchInsertIgnore(List.of(record));
                    return 1;
                }
            }
            case "weekly": {
                MacroWeekly existing = weeklyMapper.selectByUniqueKey(statDate, indicatorCode);
                MacroWeekly record = new MacroWeekly();
                record.setStatDate(statDate);
                record.setIndicatorCode(indicatorCode);
                record.setMetricValue(metricValue);
                record.setMomChange(momChange);
                record.setSource(source != null ? source : "");
                record.setExtra(extra);
                if (existing != null) {
                    return weeklyMapper.updateMetricValue(record);
                } else {
                    weeklyMapper.batchInsertIgnore(List.of(record));
                    return 1;
                }
            }
            default: {
                MacroMonthly existing = monthlyMapper.selectByUniqueKey(statDate, indicatorCode, dataVersion);
                MacroMonthly record = new MacroMonthly();
                record.setStatDate(statDate);
                record.setIndicatorCode(indicatorCode);
                record.setMetricValue(metricValue);
                record.setMetricValue2(metricValue2);
                record.setMomChange(momChange);
                record.setDataVersion(dataVersion != null ? dataVersion : 1);
                record.setSource(source != null ? source : "");
                record.setExtra(extra);
                if (existing != null) {
                    return monthlyMapper.updateMetricValue(record);
                } else {
                    monthlyMapper.batchInsertIgnore(List.of(record));
                    return 1;
                }
            }
        }
    }

    /** 删除单条记录 */
    @Transactional
    public int deleteRecord(String frequency, Date statDate, String indicatorCode) {
        switch (frequency) {
            case "daily": return dailyMapper.deleteByDateAndIndicator(statDate, indicatorCode);
            case "weekly": return weeklyMapper.deleteByDateAndIndicator(statDate, indicatorCode);
            default: return monthlyMapper.deleteByDateAndIndicator(statDate, indicatorCode);
        }
    }

    /** 按日期范围批量删除 */
    @Transactional
    public int deleteByDateRange(String frequency, String indicatorCode, Date startDate, Date endDate) {
        switch (frequency) {
            case "daily": return dailyMapper.deleteByDateRange(startDate, endDate, indicatorCode);
            case "weekly": return weeklyMapper.deleteByDateRange(startDate, endDate, indicatorCode);
            default: return monthlyMapper.deleteByDateRange(startDate, endDate, indicatorCode);
        }
    }

}
