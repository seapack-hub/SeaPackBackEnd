package org.seaPack.controller.macro;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.service.macro.LprService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * LPR 利率看板专用接口
 */
@Slf4j
@RestController
@RequestMapping("/macro/lpr")
public class LprController {

    @Autowired
    private LprService lprService;

    /** 最新 LPR 概览（KPI 卡片） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(lprService.getOverview());
    }

    /** LPR 走势（阶梯折线图） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "120") int months) {
        return Result.success(lprService.getTrend(months));
    }
}
