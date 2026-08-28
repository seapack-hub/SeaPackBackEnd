package org.seaPack.controller.macro;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.service.macro.ReserveAssetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 储备资产看板专用接口
 */
@Slf4j
@RestController
@RequestMapping("/macro/reserve-assets")
public class ReserveAssetsController {

    @Autowired
    private ReserveAssetsService reserveAssetsService;

    /** 概览（KPI 卡片 + 预警信号） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(reserveAssetsService.getOverview());
    }

    /** 趋势数据（各指标时间序列） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "120") int months) {
        return Result.success(reserveAssetsService.getTrend(months));
    }

    /** 明细表数据 */
    @GetMapping("/detail")
    public Result<Map<String, Object>> getDetail(
            @RequestParam(defaultValue = "120") int months) {
        return Result.success(reserveAssetsService.getDetail(months));
    }
}
