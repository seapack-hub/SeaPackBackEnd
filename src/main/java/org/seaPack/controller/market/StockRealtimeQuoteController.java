package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.market.StockRealtimeQuote;
import org.seaPack.service.market.StockRealtimeQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实时行情控制器
 * <p>提供行情查询接口，数据由 iTick WebSocket 实时推送写入。</p>
 */
@Slf4j
@RestController
@RequestMapping("/stockRealtimeQuote")
public class StockRealtimeQuoteController {

    @Autowired
    private StockRealtimeQuoteService quoteService;

    /** 查询指定股票最新行情 */
    @GetMapping("/latest/{stockCode}")
    public ResponseEntity<StockRealtimeQuote> latest(@PathVariable String stockCode) {
        return ResponseEntity.ok(quoteService.getLatestByCode(stockCode));
    }

    /** 查询全部股票最新行情 */
    @GetMapping("/latest/all")
    public ResponseEntity<List<StockRealtimeQuote>> latestAll() {
        return ResponseEntity.ok(quoteService.getAllLatest());
    }
}
