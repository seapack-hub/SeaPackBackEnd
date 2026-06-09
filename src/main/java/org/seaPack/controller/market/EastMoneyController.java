package org.seaPack.controller.market;

import org.seaPack.config.Result; // 统一响应体
import org.seaPack.dto.market.BillboardDto; // 龙虎榜 DTO
import org.seaPack.dto.market.RealtimeQuoteDto; // 实时行情 DTO
import org.seaPack.dto.market.StockHistoryDto; // 历史 K 线 DTO
import org.seaPack.service.market.EastMoneyStockService; // 东方财富股票数据服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 东方财富数据控制器
 * 通过东方财富 HTTP API 获取 A 股历史 K 线、实时行情和龙虎榜数据。
 */
@RestController // 标识为 RESTful 控制器
@RequestMapping("/eastmoney") // 请求基础路径
public class EastMoneyController {

    @Autowired // 注入东方财富服务
    private EastMoneyStockService eastMoneyStockService;

    /**
     * 获取股票历史 K 线
     * @param code  股票代码
     * @param start 起始日期 yyyyMMdd
     * @param end   截止日期 yyyyMMdd
     */
    @GetMapping("/history")
    public Result<List<StockHistoryDto>> history(
            @RequestParam(defaultValue = "600036") String code,
            @RequestParam(defaultValue = "20260101") String start,
            @RequestParam(defaultValue = "20500101") String end) {
        List<StockHistoryDto> data = eastMoneyStockService.getStockHistory(code, start, end);
        return Result.success(data);
    }

    /**
     * 获取股票实时行情
     * @param code 股票代码
     */
    @GetMapping("/realtime")
    public Result<RealtimeQuoteDto> realtime(
            @RequestParam(defaultValue = "600036") String code) {
        RealtimeQuoteDto data = eastMoneyStockService.getRealtimeQuote(code);
        return Result.success(data);
    }

    /**
     * 获取龙虎榜详情
     * @param code 股票代码
     * @param date 交易日期 yyyy-MM-dd
     */
    @GetMapping("/billboard")
    public Result<List<BillboardDto>> billboard(
            @RequestParam(defaultValue = "600036") String code,
            @RequestParam(defaultValue = "2026-06-05") String date) {
        List<BillboardDto> data = eastMoneyStockService.getBillboardDetails(code, date);
        return Result.success(data);
    }
}