package org.seaPack.service.macro;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.macro.MacroMonthlyMapper;
import org.seaPack.model.macro.MacroMonthly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 储备资产看板专用 Service
 * 指标码：FOREX_USD / FOREX_SDR / IMF_USD / SDR_USD / GOLD_USD / GOLD_OZ
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReserveAssetsService {

    private static final String FOREX_USD = "FOREX_USD";
    private static final String FOREX_SDR = "FOREX_SDR";
    private static final String IMF_USD  = "IMF_USD";
    private static final String SDR_USD  = "SDR_USD";
    private static final String GOLD_USD = "GOLD_USD";
    private static final String GOLD_OZ  = "GOLD_OZ";

    private static final List<String> ALL_CODES = List.of(
            FOREX_USD, FOREX_SDR, IMF_USD, SDR_USD, GOLD_USD, GOLD_OZ
    );

    private static final int SCALE = 2;

    @Autowired
    private MacroMonthlyMapper monthlyMapper;

    private Date monthsAgo(int n) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -n);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    private BigDecimal val(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    // ==================== 1. 概览（KPI 卡片 + 预警信号） ====================

    public Map<String, Object> getOverview() {
        // 取最近3个月数据用于环比计算
        List<MacroMonthly> rows = monthlyMapper.selectByIndicatorsAndDateRange(
                ALL_CODES, monthsAgo(3), new Date(), null);
        if (rows.isEmpty()) return Collections.emptyMap();

        // 按日期倒序
        List<MacroMonthly> sorted = rows.stream()
                .sorted(Comparator.comparing(MacroMonthly::getStatDate).reversed())
                .collect(Collectors.toList());

        // 按指标分组
        Map<String, List<MacroMonthly>> byCode = sorted.stream()
                .collect(Collectors.groupingBy(
                        MacroMonthly::getIndicatorCode,
                        LinkedHashMap::new,
                        Collectors.toList()));

        MacroMonthly latestFxUsd  = first(byCode.get(FOREX_USD));
        MacroMonthly prevFxUsd    = second(byCode.get(FOREX_USD));
        MacroMonthly latestFxSdr  = first(byCode.get(FOREX_SDR));
        MacroMonthly latestImf    = first(byCode.get(IMF_USD));
        MacroMonthly prevImf      = second(byCode.get(IMF_USD));
        MacroMonthly latestSdr    = first(byCode.get(SDR_USD));
        MacroMonthly prevSdr      = second(byCode.get(SDR_USD));
        MacroMonthly latestGoldUsd= first(byCode.get(GOLD_USD));
        MacroMonthly prevGoldUsd  = second(byCode.get(GOLD_USD));
        MacroMonthly latestGoldOz = first(byCode.get(GOLD_OZ));
        MacroMonthly prevGoldOz   = second(byCode.get(GOLD_OZ));

        BigDecimal fxUsd      = latestFxUsd != null ? val(latestFxUsd.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal fxUsdPrev  = prevFxUsd != null ? val(prevFxUsd.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal fxSdr      = latestFxSdr != null ? val(latestFxSdr.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal imfUsd     = latestImf != null ? val(latestImf.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal imfUsdPrev = prevImf != null ? val(prevImf.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal sdrUsd     = latestSdr != null ? val(latestSdr.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal sdrUsdPrev = prevSdr != null ? val(prevSdr.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal goldUsd    = latestGoldUsd != null ? val(latestGoldUsd.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal goldUsdPrev= prevGoldUsd != null ? val(prevGoldUsd.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal goldOz     = latestGoldOz != null ? val(latestGoldOz.getMetricValue()) : BigDecimal.ZERO;
        BigDecimal goldOzPrev = prevGoldOz != null ? val(prevGoldOz.getMetricValue()) : BigDecimal.ZERO;

        // 总储备 = 外汇 + IMF + SDR + 黄金
        BigDecimal totalUsd = fxUsd.add(imfUsd).add(sdrUsd).add(goldUsd);
        BigDecimal goldPct  = totalUsd.compareTo(BigDecimal.ZERO) > 0
                ? goldUsd.multiply(new BigDecimal(100)).divide(totalUsd, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String date = latestFxUsd != null
                ? new java.text.SimpleDateFormat("yyyy-MM").format(latestFxUsd.getStatDate())
                : "--";

        // 预警信号
        List<Map<String, String>> alerts = new ArrayList<>();
        BigDecimal fxChange = fxUsd.subtract(fxUsdPrev);
        if (fxChange.compareTo(new BigDecimal(-200)) < 0) {
            alerts.add(Map.of("name", "外汇急跌", "color", "#F56C6C", "desc", "单月外汇储备跌幅 > 200亿美元"));
        }
        // 检查连续3月增持黄金
        List<MacroMonthly> goldOzList = byCode.getOrDefault(GOLD_OZ, Collections.emptyList());
        if (goldOzList.size() >= 3) {
            boolean allIncrease = true;
            for (int i = 0; i < 3; i++) {
                BigDecimal cur = val(goldOzList.get(i).getMetricValue());
                BigDecimal nxt = val(goldOzList.get(Math.min(i + 1, goldOzList.size() - 1)).getMetricValue());
                if (cur.compareTo(nxt) <= 0) { allIncrease = false; break; }
            }
            if (allIncrease) {
                alerts.add(Map.of("name", "黄金增持", "color", "#E6A23C", "desc", "央行连续3月增持黄金"));
            }
        }
        if (goldPct.compareTo(new BigDecimal("10")) > 0) {
            alerts.add(Map.of("name", "结构转变", "color", "#E6A23C", "desc", "黄金占比突破10%，关注去美元化"));
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", date);
        r.put("fxUsd", fxUsd);
        r.put("fxUsdChange", fxChange);
        r.put("fxSdr", fxSdr);
        r.put("fxSdrChange", fxSdr.subtract(latestFxSdr != null && prevFxUsd != null ? val(latestFxSdr.getMetricValue()) : BigDecimal.ZERO));
        r.put("imfUsd", imfUsd);
        r.put("imfUsdChange", imfUsd.subtract(imfUsdPrev));
        r.put("sdrUsd", sdrUsd);
        r.put("sdrUsdChange", sdrUsd.subtract(sdrUsdPrev));
        r.put("goldUsd", goldUsd);
        r.put("goldUsdChange", goldUsd.subtract(goldUsdPrev));
        r.put("goldOz", goldOz);
        r.put("goldOzChange", goldOz.subtract(goldOzPrev));
        r.put("totalUsd", totalUsd);
        r.put("goldPct", goldPct);
        r.put("alerts", alerts);
        return r;
    }

    // ==================== 2. 趋势数据 ====================

    public Map<String, Object> getTrend(int months) {
        List<MacroMonthly> rows = monthlyMapper.selectByIndicatorsAndDateRange(
                ALL_CODES, monthsAgo(months), new Date(), null);

        List<String> dates = rows.stream()
                .map(r -> new java.text.SimpleDateFormat("yyyy-MM").format(r.getStatDate()))
                .distinct().sorted().collect(Collectors.toList());

        Map<String, Map<String, MacroMonthly>> idx = buildIndex(rows);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates);
        r.put("forexUsd",  takeMetric(idx, FOREX_USD, dates));
        r.put("forexSdr",  takeMetric(idx, FOREX_SDR, dates));
        r.put("imfUsd",    takeMetric(idx, IMF_USD, dates));
        r.put("sdrUsd",    takeMetric(idx, SDR_USD, dates));
        r.put("goldUsd",   takeMetric(idx, GOLD_USD, dates));
        r.put("goldOz",    takeMetric(idx, GOLD_OZ, dates));
        return r;
    }

    // ==================== 3. 明细表数据 ====================

    public Map<String, Object> getDetail(int months) {
        List<MacroMonthly> rows = monthlyMapper.selectByIndicatorsAndDateRange(
                ALL_CODES, monthsAgo(months), new Date(), null);

        List<String> dates = rows.stream()
                .map(r -> new java.text.SimpleDateFormat("yyyy-MM").format(r.getStatDate()))
                .distinct().sorted().collect(Collectors.toList());

        Map<String, Map<String, MacroMonthly>> idx = buildIndex(rows);

        List<Map<String, Object>> list = new ArrayList<>();
        for (String date : dates) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date);
            row.put("forexUsd", getVal(idx, FOREX_USD, date));
            row.put("forexSdr", getVal(idx, FOREX_SDR, date));
            row.put("imfUsd",   getVal(idx, IMF_USD, date));
            row.put("sdrUsd",   getVal(idx, SDR_USD, date));
            row.put("goldUsd",  getVal(idx, GOLD_USD, date));
            row.put("goldOz",   getVal(idx, GOLD_OZ, date));
            list.add(row);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", list.size());
        r.put("records", list);
        return r;
    }

    // ---- helpers ----

    private MacroMonthly first(List<MacroMonthly> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    private MacroMonthly second(List<MacroMonthly> list) {
        return (list != null && list.size() > 1) ? list.get(1) : null;
    }

    private Map<String, Map<String, MacroMonthly>> buildIndex(List<MacroMonthly> rows) {
        Map<String, Map<String, MacroMonthly>> idx = new HashMap<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
        for (MacroMonthly r : rows) {
            String d = sdf.format(r.getStatDate());
            idx.computeIfAbsent(r.getIndicatorCode(), k -> new LinkedHashMap<>()).put(d, r);
        }
        return idx;
    }

    private List<BigDecimal> takeMetric(Map<String, Map<String, MacroMonthly>> idx,
                                         String code, List<String> dates) {
        Map<String, MacroMonthly> m = idx.getOrDefault(code, Collections.emptyMap());
        return dates.stream()
                .map(d -> m.containsKey(d) ? val(m.get(d).getMetricValue()) : BigDecimal.ZERO)
                .collect(Collectors.toList());
    }

    private BigDecimal getVal(Map<String, Map<String, MacroMonthly>> idx,
                               String code, String date) {
        Map<String, MacroMonthly> m = idx.getOrDefault(code, Collections.emptyMap());
        MacroMonthly rec = m.get(date);
        return rec != null ? val(rec.getMetricValue()) : BigDecimal.ZERO;
    }
}
