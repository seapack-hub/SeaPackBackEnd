package org.seaPack.controller;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.service.AkToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AKTools 数据接口控制器
 * <p>
 * 提供基于 AKShare（通过 AKTools）的股票数据查询接口，包括 A 股历史行情、
 * 实时行情、个股信息、指数日线和分钟级数据。作为 EastMoneyController 的补充方案。
 * 注意：使用本控制器前需要确保本地的 AKTools 服务已启动。
 */
@Slf4j
@RestController
@RequestMapping("/aktools")
public class AkToolsController {

    @Autowired
    private AkToolsService akToolsService;

    /**
     * A股历史行情
     */
    @GetMapping("/stock/hist")
    public ResponseEntity<JSONArray> stockHist(
            @RequestParam String symbol,
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "qfq") String adjust) {
        return ResponseEntity.ok(
                akToolsService.stockZhAHist(symbol, period, startDate, endDate, adjust));
    }

    /**
     * A股实时行情（东方财富）
     */
    @GetMapping("/stock/spot")
    public ResponseEntity<JSONArray> stockSpot() {
        return ResponseEntity.ok(akToolsService.stockZhASpotEm());
    }

    /**
     * 个股详细信息
     */
    @GetMapping("/stock/info")
    public ResponseEntity<JSONArray> stockInfo(@RequestParam String symbol) {
        return ResponseEntity.ok(akToolsService.stockIndividualInfoEm(symbol));
    }

    /**
     * 指数日线行情
     */
    @GetMapping("/index/daily")
    public ResponseEntity<JSONArray> indexDaily(
            @RequestParam String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(akToolsService.stockZhIndexDaily(symbol, startDate, endDate));
    }

    /**
     * A股分钟级数据
     */
    @GetMapping("/stock/min")
    public ResponseEntity<JSONArray> stockMin(
            @RequestParam String symbol,
            @RequestParam(required = false, defaultValue = "1") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(
                akToolsService.stockZhAHistMinEm(symbol, period, startDate, endDate));
    }

    /**
     * 通用接口：直接调用 AKShare 任意函数
     */
    @GetMapping("/call/{function}")
    public ResponseEntity<JSONArray> call(
            @PathVariable String function,
            @RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(akToolsService.callApi(function, params));
    }

    /**
     * 通用 POST 接口
     */
    @PostMapping("/call/{function}")
    public ResponseEntity<JSONArray> callPost(
            @PathVariable String function,
            @RequestBody(required = false) Map<String, Object> params) {
        if (params == null) params = Map.of();
        return ResponseEntity.ok(akToolsService.callApiPost(function, params));
    }
}
