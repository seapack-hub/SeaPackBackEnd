package org.seaPack.controller.macro;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.model.macro.*;
import org.seaPack.service.macro.MacroDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/macro")
public class MacroDataController {

    @Autowired
    private MacroDataService macroDataService;

    // ==================== 统一查询 ====================

    /** 统一 pivot 查询（前端核心接口） */
    @GetMapping("/query")
    public Result<Map<String, Object>> queryPivot(
            @RequestParam(defaultValue = "monthly") String frequency,
            @RequestParam List<String> indicators,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) Integer dataVersion) {
        if (startDate == null) { startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); }
        if (endDate == null) { endDate = new Date(); }
        return Result.success(macroDataService.queryPivot(frequency, indicators, startDate, endDate, dataVersion));
    }

    /** 查询指标元数据（前端动态渲染用） */
    @GetMapping("/meta")
    public Result<List<SysMacroIndicatorMeta>> getMeta(
            @RequestParam(required = false) String frequency) {
        if (frequency != null && !frequency.isEmpty()) {
            return Result.success(macroDataService.getMetaByFrequency(frequency));
        }
        return Result.success(macroDataService.getAllMeta());
    }

    /** 查询已使用的指标编码 */
    @GetMapping("/indicators")
    public Result<List<String>> getUsedIndicators(
            @RequestParam(defaultValue = "monthly") String frequency) {
        return Result.success(macroDataService.getUsedIndicatorCodes(frequency));
    }

    // ==================== 分页查询（管理页面用） ====================

    @GetMapping("/monthly/page")
    public Result<PageInfo<MacroMonthly>> queryMonthlyPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam String indicatorCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) Integer dataVersion) {
        if (startDate == null) { startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); }
        if (endDate == null) { endDate = new Date(); }
        return Result.success(macroDataService.queryMonthly(pageNum, pageSize, indicatorCode, startDate, endDate, dataVersion));
    }

    @GetMapping("/daily/page")
    public Result<PageInfo<MacroDaily>> queryDailyPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam String indicatorCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        if (startDate == null) { startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); }
        if (endDate == null) { endDate = new Date(); }
        return Result.success(macroDataService.queryDaily(pageNum, pageSize, indicatorCode, startDate, endDate));
    }

    @GetMapping("/weekly/page")
    public Result<PageInfo<MacroWeekly>> queryWeeklyPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam String indicatorCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        if (startDate == null) { startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); }
        if (endDate == null) { endDate = new Date(); }
        return Result.success(macroDataService.queryWeekly(pageNum, pageSize, indicatorCode, startDate, endDate));
    }

    // ==================== 写入接口 ====================

    /** 保存单条记录（upsert：存在则更新，不存在则插入） */
    @PostMapping("/save")
    public Result<Integer> saveRecord(
            @RequestParam(defaultValue = "monthly") String frequency,
            @RequestBody MacroDataItem item) {
        return Result.success(macroDataService.saveRecord(
                frequency, item.getStatDate(), item.getIndicatorCode(),
                item.getMetricValue(), item.getMetricValue2(), item.getMomChange(),
                item.getDataVersion(), item.getSource(), item.getExtra()));
    }

    /** 统一导入接口 */
    @PostMapping("/import")
    public Result<Map<String, Integer>> importData(
            @RequestParam(defaultValue = "monthly") String frequency,
            @RequestBody List<MacroDataItem> items) {
        Map<String, Integer> result = new HashMap<>();
        switch (frequency) {
            case "daily": {
                List<MacroDaily> list = items.stream().map(item -> {
                    MacroDaily d = new MacroDaily();
                    d.setStatDate(item.getStatDate());
                    d.setIndicatorCode(item.getIndicatorCode());
                    d.setMetricValue(item.getMetricValue());
                    d.setMetricValue2(item.getMetricValue2());
                    d.setMomChange(item.getMomChange());
                    d.setSource(item.getSource() != null ? item.getSource() : "");
                    d.setExtra(item.getExtra());
                    return d;
                }).toList();
                result.put("imported", macroDataService.importDaily(list));
                break;
            }
            case "weekly": {
                List<MacroWeekly> list = items.stream().map(item -> {
                    MacroWeekly w = new MacroWeekly();
                    w.setStatDate(item.getStatDate());
                    w.setIndicatorCode(item.getIndicatorCode());
                    w.setMetricValue(item.getMetricValue());
                    w.setMomChange(item.getMomChange());
                    w.setSource(item.getSource() != null ? item.getSource() : "");
                    w.setExtra(item.getExtra());
                    return w;
                }).toList();
                result.put("imported", macroDataService.importWeekly(list));
                break;
            }
            default: {
                List<MacroMonthly> list = items.stream().map(item -> {
                    MacroMonthly m = new MacroMonthly();
                    m.setStatDate(item.getStatDate());
                    m.setIndicatorCode(item.getIndicatorCode());
                    m.setMetricValue(item.getMetricValue());
                    m.setMetricValue2(item.getMetricValue2());
                    m.setMomChange(item.getMomChange());
                    m.setDataVersion(item.getDataVersion() != null ? item.getDataVersion() : 1);
                    m.setSource(item.getSource() != null ? item.getSource() : "");
                    m.setExtra(item.getExtra());
                    return m;
                }).toList();
                result.put("imported", macroDataService.importMonthly(list));
                break;
            }
        }
        return Result.success(result);
    }

    /** 删除单条记录 */
    @PostMapping("/delete")
    public Result<Integer> deleteRecord(
            @RequestParam(defaultValue = "monthly") String frequency,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date statDate,
            @RequestParam String indicatorCode) {
        return Result.success(macroDataService.deleteRecord(frequency, statDate, indicatorCode));
    }

    // ==================== DTO ====================

    @lombok.Data
    public static class MacroDataItem {
        private Date statDate;
        private String indicatorCode;
        private BigDecimal metricValue;
        private BigDecimal metricValue2;
        private BigDecimal momChange;
        private Integer dataVersion;
        private String source;
        private String extra;
    }
}
