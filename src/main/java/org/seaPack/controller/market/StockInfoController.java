package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.market.StockInfo;
import org.seaPack.model.market.StockMarketData;
import org.seaPack.service.market.StockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/stockInfo")
public class StockInfoController {

    @Autowired
    private StockInfoService stockInfoService;

    @PostMapping("/page")
    public ResponseEntity<PageInfo<StockInfo>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody(required = false) StockInfo param) {
        if (param == null) param = new StockInfo();
        PageInfo<StockInfo> pageInfo = stockInfoService.getStockList(pageNum, pageSize, param);
        return ResponseEntity.ok(pageInfo);
    }

    @GetMapping("/list")
    public ResponseEntity<List<StockInfo>> list(StockInfo param) {
        return ResponseEntity.ok(stockInfoService.getStockListAll(param));
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockInfo stockInfo) {
        return ResponseEntity.ok(stockInfoService.insertStock(stockInfo));
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody StockInfo stockInfo) {
        return ResponseEntity.ok(stockInfoService.updateStock(stockInfo));
    }

    @DeleteMapping("/delete/{stockId}")
    public ResponseEntity<Integer> delete(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.deleteStock(stockId));
    }

    @GetMapping("/detail/{stockId}")
    public ResponseEntity<StockInfo> detail(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getStockById(stockId));
    }

    @GetMapping("/code/{stockCode}")
    public ResponseEntity<StockInfo> byCode(@PathVariable String stockCode) {
        return ResponseEntity.ok(stockInfoService.getStockByCode(stockCode));
    }

    @GetMapping("/market/latest/{stockId}")
    public ResponseEntity<StockMarketData> latestMarketData(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getLatestMarketData(stockId));
    }

    @GetMapping("/market/history/{stockId}")
    public ResponseEntity<List<StockMarketData>> marketHistory(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getDividendHistory(stockId));
    }

    @GetMapping("/market/list")
    public ResponseEntity<List<StockMarketData>> marketList(StockMarketData param) {
        return ResponseEntity.ok(stockInfoService.getMarketDataList(param));
    }

    @GetMapping("/dividend/average")
    public ResponseEntity<List<Map<String, Object>>> averageDividendYield() {
        return ResponseEntity.ok(stockInfoService.getAverageDividendYield());
    }
}