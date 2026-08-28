package org.seaPack.controller.macro;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.service.macro.SocialFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社会融资规模看板专用接口
 */
@Slf4j
@RestController
@RequestMapping("/macro/social-finance")
public class SocialFinanceController {

    @Autowired
    private SocialFinanceService socialFinanceService;

    /** 概览（KPI 卡片） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(socialFinanceService.getOverview());
    }

    /** 趋势数据（双轴图 + 信用脉冲） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(socialFinanceService.getTrend(months));
    }

    /** 结构拆解（8个分项增量） */
    @GetMapping("/structure")
    public Result<Map<String, Object>> getStructure(
            @RequestParam(defaultValue = "24") int months) {
        return Result.success(socialFinanceService.getStructure(months));
    }
}
