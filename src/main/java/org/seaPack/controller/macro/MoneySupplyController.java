package org.seaPack.controller.macro;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.service.macro.MoneySupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 货币供应量看板专用接口
 */
@Slf4j
@RestController
@RequestMapping("/macro/money-supply")
public class MoneySupplyController {

    @Autowired
    private MoneySupplyService moneySupplyService;

    /** 总览（KPI 卡片） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(moneySupplyService.getOverview(months));
    }

    /** 趋势（双Y轴：余额 + 同比增速） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(moneySupplyService.getTrend(months));
    }

    /** M1-M2 剪刀差走势 */
    @GetMapping("/scissors")
    public Result<Map<String, Object>> getScissors(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(moneySupplyService.getScissors(months));
    }

    /** 货币结构占比（环形图） */
    @GetMapping("/structure")
    public Result<Map<String, Object>> getStructure() {
        return Result.success(moneySupplyService.getStructure());
    }

    /** 剪刀差 vs 上证指数（跨市场关联） */
    @GetMapping("/correlation/scissors-stock")
    public Result<Map<String, Object>> getCorrelationScissorsStock(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(moneySupplyService.getCorrelationScissorsStock(months));
    }

    /** M2 增速 vs CPI/PPI */
    @GetMapping("/correlation/m2-cpi")
    public Result<Map<String, Object>> getCorrelationM2Cpi(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(moneySupplyService.getCorrelationM2Cpi(months));
    }

    /** 社融增量 vs M2 增速 */
    @GetMapping("/correlation/sf-m2")
    public Result<Map<String, Object>> getCorrelationSfM2(
            @RequestParam(defaultValue = "36") int months) {
        return Result.success(moneySupplyService.getCorrelationSfM2(months));
    }
}
