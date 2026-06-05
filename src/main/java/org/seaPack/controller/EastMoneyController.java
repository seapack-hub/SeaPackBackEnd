package org.seaPack.controller;

import org.seaPack.config.Result;
import org.seaPack.dto.BillboardDto;
import org.seaPack.dto.RealtimeQuoteDto;
import org.seaPack.dto.StockHistoryDto;
import org.seaPack.service.EastMoneyStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 东方财富股票数据控制器
 * <p>
 * 提供 A 股历史 K 线行情、实时行情和龙虎榜数据的查询接口，
 * 数据源为东方财富 HTTP API。
 */
@RestController
@RequestMapping("/eastmoney")
public class EastMoneyController {

    @Autowired
    private EastMoneyStockService eastMoneyStockService;

    /**
     * 获取股票历史 K 线数据
     * <p>
     * 默认查询招商银行（600036）从 2026-01-01 至今的日 K 线数据，
     * 数据经过前复权处理。
     *
     * @param code  股票代码，默认 600036
     * @param start 起始日期，格式 yyyyMMdd，默认 20260101
     * @param end   截止日期，格式 yyyyMMdd，默认 20500101
     * @return 统一响应体，data 为 K 线数据列表
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
     * <p>
     * 默认查询招商银行（600036）的实时报价，
     * 包含最新价、今开、昨收、最高、最低、成交量等信息。
     *
     * @param code 股票代码，默认 600036
     * @return 统一响应体，data 为实时行情对象
     */
    @GetMapping("/realtime")
    public Result<RealtimeQuoteDto> realtime(
            @RequestParam(defaultValue = "600036") String code) {
        RealtimeQuoteDto data = eastMoneyStockService.getRealtimeQuote(code);
        return Result.success(data);
    }

    /**
     * 获取龙虎榜详情
     * <p>
     * 查询指定股票在指定日期的龙虎榜上榜数据。
     * 注意：日期格式为 yyyy-MM-dd（与历史K线的 yyyyMMdd 格式不同）。
     * 默认查询招商银行（600036）在 2026-06-05 的龙虎榜数据。
     *
     * @param code 股票代码，默认 600036
     * @param date 交易日期，格式 yyyy-MM-dd，默认 2026-06-05
     * @return 统一响应体，data 为龙虎榜数据列表
     */
    @GetMapping("/billboard")
    public Result<List<BillboardDto>> billboard(
            @RequestParam(defaultValue = "600036") String code,
            @RequestParam(defaultValue = "2026-06-05") String date) {
        List<BillboardDto> data = eastMoneyStockService.getBillboardDetails(code, date);
        return Result.success(data);
    }
}
