package org.seaPack.service.macro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 货币供应量看板专用 Service
 * 所有数据通过 MacroDataService.queryPivot() 获取
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class MoneySupplyService {

    @Autowired
    private MacroDataService macroDataService;

    // ==================== 工具方法 ====================

    private Date monthsAgo(int n) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -n);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    private BigDecimal safeGet(Map<String, List<BigDecimal>> series, String key, int idx) {
        List<BigDecimal> list = series.getOrDefault(key, Collections.emptyList());
        return idx < list.size() ? list.get(idx) : BigDecimal.ZERO;
    }

    // ==================== 看板接口 ====================

    /** 总览（KPI 卡片：最新月 M0/M1/M2 余额 + 同比 + 上月同比） */
    public Map<String, Object> getOverview(int months) {
        Date start = monthsAgo(months);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("M0", "M1", "M2", "M0_YOY", "M1_YOY", "M2_YOY"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        if (dates.isEmpty()) return Collections.emptyMap();

        int last = dates.size() - 1;
        int prev = Math.max(0, last - 1);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", dates.get(last).substring(0, 7));
        r.put("m0", safeGet(series, "M0", last));
        r.put("m1", safeGet(series, "M1", last));
        r.put("m2", safeGet(series, "M2", last));
        r.put("m0Yoy", safeGet(series, "M0_YOY", last));
        r.put("m1Yoy", safeGet(series, "M1_YOY", last));
        r.put("m2Yoy", safeGet(series, "M2_YOY", last));
        r.put("prevM0Yoy", safeGet(series, "M0_YOY", prev));
        r.put("prevM1Yoy", safeGet(series, "M1_YOY", prev));
        r.put("prevM2Yoy", safeGet(series, "M2_YOY", prev));
        return r;
    }

    /** 趋势（双Y轴：余额 + 同比增速） */
    public Map<String, Object> getTrend(int months) {
        Date start = monthsAgo(months);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("M0", "M1", "M2", "M0_YOY", "M1_YOY", "M2_YOY"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates.stream().map(d -> d.substring(0, 7)).collect(Collectors.toList()));
        r.put("m0", series.getOrDefault("M0", List.of()));
        r.put("m1", series.getOrDefault("M1", List.of()));
        r.put("m2", series.getOrDefault("M2", List.of()));
        r.put("m0Yoy", series.getOrDefault("M0_YOY", List.of()));
        r.put("m1Yoy", series.getOrDefault("M1_YOY", List.of()));
        r.put("m2Yoy", series.getOrDefault("M2_YOY", List.of()));
        return r;
    }

    /** M1-M2 剪刀差走势 */
    public Map<String, Object> getScissors(int months) {
        Date start = monthsAgo(months);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("M1_YOY", "M2_YOY"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        List<BigDecimal> m1Yoy = series.getOrDefault("M1_YOY", List.of());
        List<BigDecimal> m2Yoy = series.getOrDefault("M2_YOY", List.of());

        List<BigDecimal> scissors = new ArrayList<>();
        for (int i = 0; i < m1Yoy.size(); i++) {
            scissors.add(m1Yoy.get(i).subtract(m2Yoy.get(i)));
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates.stream().map(d -> d.substring(0, 7)).collect(Collectors.toList()));
        r.put("m1Yoy", m1Yoy);
        r.put("m2Yoy", m2Yoy);
        r.put("scissors", scissors);
        return r;
    }

    /** 货币结构占比（环形图：M0 / M1 / 准货币） */
    public Map<String, Object> getStructure() {
        Date start = monthsAgo(3);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("M0", "M1", "M2"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        if (dates.isEmpty()) return Collections.emptyMap();

        int last = dates.size() - 1;
        BigDecimal m0 = safeGet(series, "M0", last);
        BigDecimal m1 = safeGet(series, "M1", last);
        BigDecimal m2 = safeGet(series, "M2", last);
        BigDecimal quasiM2 = m2.subtract(m1);
        BigDecimal total = m2;

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", dates.get(last).substring(0, 7));
        r.put("m0", m0);
        r.put("m1", m1);
        r.put("m2", m2);
        r.put("quasiM2", quasiM2);
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            r.put("m0Pct", m0.multiply(new BigDecimal("100")).divide(total, 2, BigDecimal.ROUND_HALF_UP));
            r.put("m1Pct", m1.multiply(new BigDecimal("100")).divide(total, 2, BigDecimal.ROUND_HALF_UP));
            r.put("quasiPct", quasiM2.multiply(new BigDecimal("100")).divide(total, 2, BigDecimal.ROUND_HALF_UP));
        } else {
            r.put("m0Pct", BigDecimal.ZERO);
            r.put("m1Pct", BigDecimal.ZERO);
            r.put("quasiPct", BigDecimal.ZERO);
        }
        return r;
    }

    /** 剪刀差 vs 上证指数（跨市场关联） */
    public Map<String, Object> getCorrelationScissorsStock(int months) {
        Date start = monthsAgo(months);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("M1_YOY", "M2_YOY", "SH_INDEX"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        List<BigDecimal> m1Yoy = series.getOrDefault("M1_YOY", List.of());
        List<BigDecimal> m2Yoy = series.getOrDefault("M2_YOY", List.of());

        List<BigDecimal> scissors = new ArrayList<>();
        for (int i = 0; i < m1Yoy.size(); i++) {
            scissors.add(m1Yoy.get(i).subtract(m2Yoy.get(i)));
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates.stream().map(d -> d.substring(0, 7)).collect(Collectors.toList()));
        r.put("scissors", scissors);
        r.put("stockIndex", series.getOrDefault("SH_INDEX", Collections.emptyList()));
        return r;
    }

    /** M2 增速 vs CPI/PPI */
    public Map<String, Object> getCorrelationM2Cpi(int months) {
        Date start = monthsAgo(months);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("M2_YOY", "CPI_YOY", "PPI_YOY"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates.stream().map(d -> d.substring(0, 7)).collect(Collectors.toList()));
        r.put("m2Yoy", series.getOrDefault("M2_YOY", List.of()));
        r.put("cpiYoy", series.getOrDefault("CPI_YOY", List.of()));
        r.put("ppiYoy", series.getOrDefault("PPI_YOY", List.of()));
        return r;
    }

    /** 社融增量 vs M2 增速 */
    public Map<String, Object> getCorrelationSfM2(int months) {
        Date start = monthsAgo(months);
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("SF_YOY", "M2_YOY"),
                start, new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates.stream().map(d -> d.substring(0, 7)).collect(Collectors.toList()));
        r.put("sfYoy", series.getOrDefault("SF_YOY", List.of()));
        r.put("m2Yoy", series.getOrDefault("M2_YOY", List.of()));
        return r;
    }
}
