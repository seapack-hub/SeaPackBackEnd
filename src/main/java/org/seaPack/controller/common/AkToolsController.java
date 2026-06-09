package org.seaPack.controller.common;

import com.alibaba.fastjson.JSONArray; // 阿里 fastjson JSON 数组
import lombok.extern.slf4j.Slf4j; // Lombok 日志注解
import org.seaPack.service.common.AkToolsService; // AKTools 服务层
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.Map; // Map 集合

/**
 * AKTools 数据接口控制器
 * 提供股票历史 K 线、实时行情、个股信息、指数日线等数据查询接口，
 * 底层通过 HTTP 调用 AKTools 服务获取金融数据。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/aktools") // 请求基础路径
public class AkToolsController {

    @Autowired // 注入 AKTools 服务
    private AkToolsService akToolsService;

    /**
     * 获取 A 股历史 K 线数据
     * @param symbol   股票代码
     * @param period   K 线周期（daily/weekly/monthly）
     * @param startDate 起始日期
     * @param endDate   截止日期
     * @param adjust    复权方式（qfq-前复权 hfq-后复权）
     */
    @GetMapping("/stock/hist")
    public ResponseEntity<JSONArray> stockHist(
            @RequestParam String symbol,
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "qfq") String adjust) {
        return ResponseEntity.ok(akToolsService.stockZhAHist(symbol, period, startDate, endDate, adjust));
    }

    /**
     * 获取 A 股全市场实时行情
     */
    @GetMapping("/stock/spot")
    public ResponseEntity<JSONArray> stockSpot() {
        return ResponseEntity.ok(akToolsService.stockZhASpotEm());
    }

    /**
     * 获取个股基本信息
     * @param symbol 股票代码
     */
    @GetMapping("/stock/info")
    public ResponseEntity<JSONArray> stockInfo(@RequestParam String symbol) {
        return ResponseEntity.ok(akToolsService.stockIndividualInfoEm(symbol));
    }

    /**
     * 获取指数日线数据
     * @param symbol    指数代码
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    @GetMapping("/index/daily")
    public ResponseEntity<JSONArray> indexDaily(
            @RequestParam String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(akToolsService.stockZhIndexDaily(symbol, startDate, endDate));
    }

    /**
     * 获取股票分钟级 K 线
     * @param symbol    股票代码
     * @param period    分钟周期（1/5/15/30/60）
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    @GetMapping("/stock/min")
    public ResponseEntity<JSONArray> stockMin(
            @RequestParam String symbol,
            @RequestParam(required = false, defaultValue = "1") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(akToolsService.stockZhAHistMinEm(symbol, period, startDate, endDate));
    }

    /**
     * GET 方式通用 AKTools 函数调用
     * @param function 函数名称
     * @param params   查询参数
     */
    @GetMapping("/call/{function}")
    public ResponseEntity<JSONArray> call(
            @PathVariable String function,
            @RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(akToolsService.callApi(function, params));
    }

    /**
     * POST 方式通用 AKTools 函数调用
     * @param function 函数名称
     * @param params   请求体参数
     */
    @PostMapping("/call/{function}")
    public ResponseEntity<JSONArray> callPost(
            @PathVariable String function,
            @RequestBody(required = false) Map<String, Object> params) {
        if (params == null) params = Map.of();
        return ResponseEntity.ok(akToolsService.callApiPost(function, params));
    }
}