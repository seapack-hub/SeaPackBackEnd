package org.seaPack.controller.macro;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.service.macro.ShiborService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SHIBOR 资金面监控看板专用接口
 */
@Slf4j
@RestController
@RequestMapping("/macro/shibor")
public class ShiborController {

    @Autowired
    private ShiborService shiborService;

    /** 概览（KPI 卡片） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(shiborService.getOverview());
    }

    /** 多期限趋势（多折线图） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "12") int months) {
        return Result.success(shiborService.getTrend(months));
    }

    /** 今日期限结构曲线 */
    @GetMapping("/curve")
    public Result<Map<String, Object>> getCurve() {
        return Result.success(shiborService.getCurve());
    }
}
