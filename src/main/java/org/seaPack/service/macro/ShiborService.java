package org.seaPack.service.macro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SHIBOR 资金面监控看板专用 Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ShiborService {

    /** SHIBOR 全部8个期限品种（按期限从短到长） */
    private static final List<String> ALL_TENORS = List.of(
            "SHIBOR_ON", "SHIBOR_1W", "SHIBOR_2W", "SHIBOR_1M",
            "SHIBOR_3M", "SHIBOR_6M", "SHIBOR_9M", "SHIBOR_1Y");

    /** 期限中文名映射 */
    private static final Map<String, String> TENOR_LABELS = Map.of(
            "SHIBOR_ON", "隔夜", "SHIBOR_1W", "1周", "SHIBOR_2W", "2周",
            "SHIBOR_1M", "1月", "SHIBOR_3M", "3月", "SHIBOR_6M", "6月",
            "SHIBOR_9M", "9月", "SHIBOR_1Y", "1年");

    @Autowired
    private MacroDataService macroDataService;

    private Date daysAgo(int n) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -n);
        return cal.getTime();
    }

    private Date monthsAgo(int n) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -n);
        return cal.getTime();
    }

    private BigDecimal safeGet(Map<String, List<BigDecimal>> series, String key, int idx) {
        List<BigDecimal> list = series.getOrDefault(key, Collections.emptyList());
        return idx < list.size() ? list.get(idx) : BigDecimal.ZERO;
    }

    /**
     * 概览（KPI 卡片）
     * 返回最新一天的隔夜/1年利率 + 日环比(BP) + 期限利差
     */
    public Map<String, Object> getOverview() {
        // 查最近7天数据确保取到最新交易日
        Map<String, Object> pivot = macroDataService.queryPivot("daily",
                ALL_TENORS, daysAgo(7), new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        if (dates.isEmpty()) return Collections.emptyMap();

        int last = dates.size() - 1;
        int prev = Math.max(0, last - 1);

        BigDecimal onNow = safeGet(series, "SHIBOR_ON", last);
        BigDecimal onPrev = safeGet(series, "SHIBOR_ON", prev);
        BigDecimal y1Now = safeGet(series, "SHIBOR_1Y", last);
        BigDecimal y1Prev = safeGet(series, "SHIBOR_1Y", prev);
        // 期限利差 = 1年 - 隔夜
        BigDecimal spread = y1Now.subtract(onNow);
        BigDecimal spreadPrev = y1Prev.subtract(onPrev);

        // BP 变化 = (当前 - 前值) * 100，保留整数
        BigDecimal onBp = onNow.subtract(onPrev).multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal y1Bp = y1Now.subtract(y1Prev).multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal spreadBp = spread.subtract(spreadPrev).multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", dates.get(last));
        r.put("on", onNow);
        r.put("onBp", onBp.intValue());
        r.put("y1", y1Now);
        r.put("y1Bp", y1Bp.intValue());
        r.put("spread", spread);
        r.put("spreadBp", spreadBp.intValue());
        return r;
    }

    /**
     * 多期限趋势（多折线图）
     * 返回按日期分组的全部8条线数据
     */
    public Map<String, Object> getTrend(int months) {
        Map<String, Object> pivot = macroDataService.queryPivot("daily",
                ALL_TENORS, monthsAgo(months), new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates);
        for (String code : ALL_TENORS) {
            r.put(code, series.getOrDefault(code, List.of()));
        }
        return r;
    }

    /**
     * 今日期限结构曲线（柱状图）
     * 返回最新一天的8个期限利率
     */
    public Map<String, Object> getCurve() {
        Map<String, Object> pivot = macroDataService.queryPivot("daily",
                ALL_TENORS, daysAgo(7), new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        if (dates.isEmpty()) return Collections.emptyMap();

        int last = dates.size() - 1;
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (String code : ALL_TENORS) {
            labels.add(TENOR_LABELS.getOrDefault(code, code));
            values.add(safeGet(series, code, last));
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", dates.get(last));
        r.put("labels", labels);
        r.put("values", values);
        return r;
    }
}
