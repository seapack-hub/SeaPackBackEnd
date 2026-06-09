package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.market.StockMarketData;
import org.seaPack.service.market.StockMarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/stockMarketData")
public class StockMarketDataController {

    @Autowired
    private StockMarketDataService stockMarketDataService;

    @GetMapping("/list")
    public ResponseEntity<List<StockMarketData>> list(StockMarketData param) {
        return ResponseEntity.ok(stockMarketDataService.getList(param));
    }

    @GetMapping("/latest/{stockId}")
    public ResponseEntity<StockMarketData> latest(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockMarketDataService.getLatestByStockId(stockId));
    }

    @GetMapping("/history/{stockId}")
    public ResponseEntity<List<StockMarketData>> history(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockMarketDataService.getHistoryByStockId(stockId));
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockMarketData data) {
        return ResponseEntity.ok(stockMarketDataService.insert(data));
    }

    @GetMapping("/dividend/average")
    public ResponseEntity<List<Map<String, Object>>> averageDividendYield() {
        return ResponseEntity.ok(stockMarketDataService.getAverageDividendYield());
    }
}