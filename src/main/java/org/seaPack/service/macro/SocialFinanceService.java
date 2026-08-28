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
 * 社会融资规模看板专用 Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SocialFinanceService {

    /** ========== 存量指标（metric_value=存量额, metric_value2=同比增速%）========== */
    private static final String SF_STOCK_CODE            = "SF_STOCK";            // 社会融资规模存量
    private static final String SF_RMB_LOAN_STOCK_CODE   = "SF_RMB_LOAN_STOCK";   // 人民币贷款存量
    private static final String SF_FOREIGN_LOAN_STOCK    = "SF_FOREIGN_LOAN_STOCK";  // 外币贷款存量
    private static final String SF_ENTRUDED_LOAN_STOCK   = "SF_ENTRUDED_LOAN_STOCK"; // 委托贷款存量
    private static final String SF_TRUST_LOAN_STOCK      = "SF_TRUST_LOAN_STOCK";    // 信托贷款存量
    private static final String SF_ACCEPTANCE_STOCK      = "SF_ACCEPTANCE_STOCK";    // 未贴现承兑汇票存量
    private static final String SF_CORP_BOND_STOCK       = "SF_CORP_BOND_STOCK";     // 企业债券存量
    private static final String SF_GOVT_BOND_STOCK       = "SF_GOVT_BOND_STOCK";     // 政府债券存量
    private static final String SF_EQUITY_STOCK          = "SF_EQUITY_STOCK";        // 股票融资存量
    private static final String SF_ABS_STOCK             = "SF_ABS_STOCK";           // ABS存量
    private static final String SF_LOAN_WRITEOFF_STOCK   = "SF_LOAN_WRITEOFF_STOCK"; // 贷款核销存量

    /** ========== 增量指标（metric_value=当月增量）========== */
    private static final String SF_NEW_CODE             = "SF_NEW";             // 社融增量
    private static final String SF_RMB_LOAN_CODE        = "SF_RMB_LOAN";        // 人民币贷款增量
    private static final String SF_GOVT_BOND_CODE       = "SF_GOVT_BOND";       // 政府债券增量
    private static final String SF_CORP_BOND_CODE       = "SF_CORP_BOND";       // 企业债券增量
    private static final String SF_EQUITY_CODE          = "SF_EQUITY";          // 股票融资增量
    private static final String SF_ACCEPTANCE_CODE      = "SF_ACCEPTANCE";      // 未贴现承兑汇票增量
    private static final String SF_TRUST_LOAN_CODE      = "SF_TRUST_LOAN";      // 信托贷款增量
    private static final String SF_ENTRUDED_LOAN_CODE   = "SF_ENTRUDED_LOAN";   // 委托贷款增量
    private static final String SF_FOREIGN_LOAN_CODE    = "SF_FOREIGN_LOAN";    // 外币贷款增量
    private static final String SF_ABS_CODE             = "SF_ABS";             // ABS增量
    private static final String SF_LOAN_WRITEOFF_CODE   = "SF_LOAN_WRITEOFF";   // 贷款核销增量

    /** M2同比 */
    private static final String M2_YOY_CODE = "M2_YOY";

    /** 所有需要查询的指标码 */
    private static final List<String> ALL_SF_CODES = List.of(
            SF_STOCK_CODE, SF_RMB_LOAN_STOCK_CODE, SF_FOREIGN_LOAN_STOCK,
            SF_ENTRUDED_LOAN_STOCK, SF_TRUST_LOAN_STOCK, SF_ACCEPTANCE_STOCK,
            SF_CORP_BOND_STOCK, SF_GOVT_BOND_STOCK, SF_EQUITY_STOCK,
            SF_ABS_STOCK, SF_LOAN_WRITEOFF_STOCK,
            SF_NEW_CODE, SF_RMB_LOAN_CODE, SF_GOVT_BOND_CODE, SF_CORP_BOND_CODE,
            SF_EQUITY_CODE, SF_ACCEPTANCE_CODE, SF_TRUST_LOAN_CODE, SF_ENTRUDED_LOAN_CODE,
            SF_FOREIGN_LOAN_CODE, SF_ABS_CODE, SF_LOAN_WRITEOFF_CODE,
            M2_YOY_CODE
    );

    private static final int SCALE = 4;

    @Autowired
    private MacroMonthlyMapper monthlyMapper;

    private Date monthsAgo(int n) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -n);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /** 安全取值，null→0 */
    private BigDecimal val(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * 概览（KPI 卡片）
     * 社融存量同比、M2-社融剪刀差、贷款同比、社融当月新增
     */
    public Map<String, Object> getOverview() {
        List<MacroMonthly> rows = monthlyMapper.selectByIndicatorsAndDateRange(
                ALL_SF_CODES, monthsAgo(3), new Date(), null);
        if (rows.isEmpty()) return Collections.emptyMap();

        // 按日期倒序，取最新两个月的数据
        Map<String, List<MacroMonthly>> byCode = rows.stream()
                .sorted(Comparator.comparing(MacroMonthly::getStatDate).reversed())
                .collect(Collectors.groupingBy(
                        MacroMonthly::getIndicatorCode,
                        LinkedHashMap::new,
                        Collectors.toList()));

        MacroMonthly latestSF    = first(byCode.get(SF_STOCK_CODE));
        MacroMonthly prevSF      = second(byCode.get(SF_STOCK_CODE));
        MacroMonthly latestM2    = first(byCode.get(M2_YOY_CODE));
        MacroMonthly latestLoan  = first(byCode.get(SF_RMB_LOAN_STOCK_CODE));
        MacroMonthly prevLoan    = second(byCode.get(SF_RMB_LOAN_STOCK_CODE));
        MacroMonthly latestGovt  = first(byCode.get(SF_GOVT_BOND_STOCK));
        MacroMonthly prevGovt    = second(byCode.get(SF_GOVT_BOND_STOCK));
        MacroMonthly latestNew   = first(byCode.get(SF_NEW_CODE));

        BigDecimal sfStock   = latestSF != null ? latestSF.getMetricValue() : BigDecimal.ZERO;
        BigDecimal sfYoy     = latestSF != null ? val(latestSF.getMetricValue2()) : BigDecimal.ZERO;
        BigDecimal sfYoyPrev = prevSF != null ? val(prevSF.getMetricValue2()) : BigDecimal.ZERO;
        BigDecimal m2Yoy     = latestM2 != null ? latestM2.getMetricValue() : BigDecimal.ZERO;
        BigDecimal loanYoy   = latestLoan != null ? val(latestLoan.getMetricValue2()) : BigDecimal.ZERO;
        BigDecimal loanYoyPrev  = prevLoan != null ? val(prevLoan.getMetricValue2()) : BigDecimal.ZERO;
        BigDecimal govtBondYoy  = latestGovt != null ? val(latestGovt.getMetricValue2()) : BigDecimal.ZERO;
        BigDecimal govtBondYoyPrev = prevGovt != null ? val(prevGovt.getMetricValue2()) : BigDecimal.ZERO;
        BigDecimal sfNew        = latestNew != null ? latestNew.getMetricValue() : BigDecimal.ZERO;

        String date = latestSF != null
                ? new java.text.SimpleDateFormat("yyyy-MM").format(latestSF.getStatDate())
                : "--";

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("date", date);
        r.put("sfStock", sfStock);          // 社融存量（万亿元）
        r.put("sfYoy", sfYoy);              // 社融存量同比（%）
        r.put("sfYoyChange", sfYoy.subtract(sfYoyPrev));
        r.put("m2Yoy", m2Yoy);              // M2同比（%）
        r.put("scissors", m2Yoy.subtract(sfYoy));   // M2-社融剪刀差
        r.put("loanYoy", loanYoy);           // 贷款存量同比（%）
        r.put("loanYoyChange", loanYoy.subtract(loanYoyPrev));
        r.put("govtBondYoy", govtBondYoy);         // 政府债券存量同比（%）
        r.put("govtBondYoyChange", govtBondYoy.subtract(govtBondYoyPrev));
        r.put("sfNew", sfNew);               // 当月新增（万亿元）
        return r;
    }

    /**
     * 趋势数据（双轴图 + 信用脉冲）
     * 返回：社融存量、社融同比、社融增量、贷款增量、M2同比、信用脉冲、政府债券增量、企业债券增量
     */
    public Map<String, Object> getTrend(int months) {
        List<MacroMonthly> rows = monthlyMapper.selectByIndicatorsAndDateRange(
                ALL_SF_CODES, monthsAgo(months), new Date(), null);

        List<String> dates = rows.stream()
                .map(r -> new java.text.SimpleDateFormat("yyyy-MM").format(r.getStatDate()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 构建 code → date → MacroMonthly 索引
        Map<String, Map<String, MacroMonthly>> idx = new HashMap<>();
        for (MacroMonthly r : rows) {
            String d = new java.text.SimpleDateFormat("yyyy-MM").format(r.getStatDate());
            idx.computeIfAbsent(r.getIndicatorCode(), k -> new LinkedHashMap<>()).put(d, r);
        }

        // 信用脉冲：社融同比增速的12期变动
        List<BigDecimal> sfYoySeries = dates.stream()
                .map(d -> idx.getOrDefault(SF_STOCK_CODE, Collections.emptyMap())
                        .getOrDefault(d, null))
                .map(r -> r != null ? val(r.getMetricValue2()) : BigDecimal.ZERO)
                .collect(Collectors.toList());

        List<BigDecimal> creditImpulse = new ArrayList<>();
        for (int i = 0; i < sfYoySeries.size(); i++) {
            if (i >= 12) creditImpulse.add(sfYoySeries.get(i).subtract(sfYoySeries.get(i - 12)));
            else creditImpulse.add(BigDecimal.ZERO);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates);
        r.put("sfStock",  takeMetric(idx, SF_STOCK_CODE, dates));      // 社融存量
        r.put("sfYoy",    sfYoySeries);                                  // 社融同比
        r.put("m2Yoy",    takeMetric(idx, M2_YOY_CODE, dates));        // M2同比
        r.put("sfNew",    takeMetric(idx, SF_NEW_CODE, dates));        // 社融增量
        r.put("loanNew",  takeMetric(idx, SF_RMB_LOAN_CODE, dates));   // 贷款增量
        r.put("govtBondNew", takeMetric(idx, SF_GOVT_BOND_CODE, dates)); // 政府债券增量
        r.put("corpBondNew", takeMetric(idx, SF_CORP_BOND_CODE, dates)); // 企业债券增量
        r.put("creditImpulse", creditImpulse);

        // 8个分项存量序列（供堆叠面积图使用）
        r.put("sfStockRmbLoan",    takeMetric(idx, SF_RMB_LOAN_STOCK_CODE, dates));
        r.put("sfStockGovtBond",   takeMetric(idx, SF_GOVT_BOND_STOCK, dates));
        r.put("sfStockCorpBond",   takeMetric(idx, SF_CORP_BOND_STOCK, dates));
        r.put("sfStockEquity",     takeMetric(idx, SF_EQUITY_STOCK, dates));
        r.put("sfStockTrustLoan",  takeMetric(idx, SF_TRUST_LOAN_STOCK, dates));
        r.put("sfStockEntrustedLoan", takeMetric(idx, SF_ENTRUDED_LOAN_STOCK, dates));
        r.put("sfStockForeignLoan",   takeMetric(idx, SF_FOREIGN_LOAN_STOCK, dates));
        r.put("sfStockOther",      takeMetric(idx, SF_ABS_STOCK, dates));
        return r;
    }

    /**
     * 结构贡献图（社融增量拆解为8个分项）
     */
    public Map<String, Object> getStructure(int months) {
        List<MacroMonthly> rows = monthlyMapper.selectByIndicatorsAndDateRange(
                ALL_SF_CODES, monthsAgo(months), new Date(), null);

        List<String> dates = rows.stream()
                .map(r -> new java.text.SimpleDateFormat("yyyy-MM").format(r.getStatDate()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Map<String, Map<String, MacroMonthly>> idx = new HashMap<>();
        for (MacroMonthly r : rows) {
            String d = new java.text.SimpleDateFormat("yyyy-MM").format(r.getStatDate());
            idx.computeIfAbsent(r.getIndicatorCode(), k -> new LinkedHashMap<>()).put(d, r);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dates", dates);
        r.put("rmblOan",     takeMetric(idx, SF_RMB_LOAN_CODE, dates));     // 人民币贷款增量
        r.put("govtBond",    takeMetric(idx, SF_GOVT_BOND_CODE, dates));    // 政府债券增量
        r.put("corpBond",    takeMetric(idx, SF_CORP_BOND_CODE, dates));    // 企业债券增量
        r.put("equity",      takeMetric(idx, SF_EQUITY_CODE, dates));       // 股票融资增量
        r.put("trustLoan",   takeMetric(idx, SF_TRUST_LOAN_CODE, dates));   // 信托贷款增量
        r.put("entrustedLoan", takeMetric(idx, SF_ENTRUDED_LOAN_CODE, dates)); // 委托贷款增量
        r.put("foreignLoan", takeMetric(idx, SF_FOREIGN_LOAN_CODE, dates)); // 外币贷款增量
        r.put("other",       takeMetric(idx, SF_ABS_CODE, dates));          // ABS+核销+承兑等
        return r;
    }

    // ---- private helpers ----

    /** 取列表第一个非null元素 */
    private MacroMonthly first(List<MacroMonthly> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** 取列表第二个非null元素 */
    private MacroMonthly second(List<MacroMonthly> list) {
        return (list != null && list.size() > 1) ? list.get(1) : null;
    }

    /** 按日期顺序取某指标的 metric_value */
    private List<BigDecimal> takeMetric(Map<String, Map<String, MacroMonthly>> idx,
                                         String code, List<String> dates) {
        Map<String, MacroMonthly> m = idx.getOrDefault(code, Collections.emptyMap());
        return dates.stream()
                .map(d -> m.containsKey(d) ? val(m.get(d).getMetricValue()) : BigDecimal.ZERO)
                .collect(Collectors.toList());
    }
}
