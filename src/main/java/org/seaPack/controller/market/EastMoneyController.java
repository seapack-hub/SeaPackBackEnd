package org.seaPack.controller.market;

import org.seaPack.config.Result;
import org.seaPack.dto.market.BillboardDto;
import org.seaPack.dto.market.RealtimeQuoteDto;
import org.seaPack.dto.market.StockHistoryDto;
import org.seaPack.service.market.EastMoneyStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eastmoney")
public class EastMoneyController {

    @Autowired
    private EastMoneyStockService eastMoneyStockService;

    @GetMapping("/history")
    public Result<List<StockHistoryDto>> history(
            @RequestParam(defaultValue = "600036") String code,
            @RequestParam(defaultValue = "20260101") String start,
            @RequestParam(defaultValue = "20500101") String end) {
        List<StockHistoryDto> data = eastMoneyStockService.getStockHistory(code, start, end);
        return Result.success(data);
    }

    @GetMapping("/realtime")
    public Result<RealtimeQuoteDto> realtime(
            @RequestParam(defaultValue = "600036") String code) {
        RealtimeQuoteDto data = eastMoneyStockService.getRealtimeQuote(code);
        return Result.success(data);
    }

    @GetMapping("/billboard")
    public Result<List<BillboardDto>> billboard(
            @RequestParam(defaultValue = "600036") String code,
            @RequestParam(defaultValue = "2026-06-05") String date) {
        List<BillboardDto> data = eastMoneyStockService.getBillboardDetails(code, date);
        return Result.success(data);
    }
}