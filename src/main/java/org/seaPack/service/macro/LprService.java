package org.seaPack.service.macro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LPR 利率看板专用 Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class LprService {

    @Autowired
    private MacroDataService macroDataService;

    private Date monthsAgo(int n) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -n);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /** 最新 LPR 概览（KPI 卡片） */
    public Map<String, Object> getOverview() {
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("LPR_1Y", "LPR_5Y"),
                monthsAgo(3), new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");
        if (dates.isEmpty()) return Collections.emptyMap();

        int last = dates.size() - 1;
        int prev = Math.max(0, last - 1);

        List<BigDecimal> y1 = series.getOrDefault("LPR_1Y", List.of());
        List<BigDecimal> y5 = series.getOrDefault("LPR_5Y", List.of());

        BigDecimal lpr1y = last < y1.size() ? y1.get(last) : BigDecimal.ZERO;
        BigDecimal lpr5y = last < y5.size() ? y5.get(last) : BigDecimal.ZERO;
        BigDecimal prev1y = prev < y1.size() ? y1.get(prev) : BigDecimal.ZERO;
        BigDecimal prev5y = prev < y5.size() ? y5.get(prev) : BigDecimal.ZERO;

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", dates.get(last).substring(0, 7));
        r.put("lpr1y", lpr1y);
        r.put("lpr5y", lpr5y);
        r.put("spread", lpr5y.subtract(lpr1y));
        r.put("prevLpr1y", prev1y);
        r.put("prevLpr5y", prev5y);
        r.put("change1y", lpr1y.subtract(prev1y));
        r.put("change5y", lpr5y.subtract(prev5y));
        return r;
    }

    /** LPR 走势（阶梯折线图） */
    public Map<String, Object> getTrend(int months) {
        Map<String, Object> pivot = macroDataService.queryPivot("monthly",
                List.of("LPR_1Y", "LPR_5Y"),
                monthsAgo(months), new Date(), null);
        List<String> dates = (List<String>) pivot.get("dates");
        Map<String, List<BigDecimal>> series = (Map<String, List<BigDecimal>>) pivot.get("series");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates.stream().map(d -> d.substring(0, 7)).collect(Collectors.toList()));
        r.put("lpr1y", series.getOrDefault("LPR_1Y", List.of()));
        r.put("lpr5y", series.getOrDefault("LPR_5Y", List.of()));
        return r;
    }
}
