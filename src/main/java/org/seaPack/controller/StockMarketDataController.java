package org.seaPack.controller;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.StockMarketData;
import org.seaPack.service.StockMarketDataService;
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

    /**
     * 多条件查询行情数据
     * @param param 查询条件
     * @return 行情数据列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<StockMarketData>> list(StockMarketData param) {
        return ResponseEntity.ok(stockMarketDataService.getList(param));
    }

    /**
     * 查询股票最新行情
     * @param stockId 股票ID
     * @return 最新行情
     */
    @GetMapping("/latest/{stockId}")
    public ResponseEntity<StockMarketData> latest(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockMarketDataService.getLatestByStockId(stockId));
    }

    /**
     * 查询股票历史行情（趋势图用）
     * @param stockId 股票ID
     * @return 历史行情列表
     */
    @GetMapping("/history/{stockId}")
    public ResponseEntity<List<StockMarketData>> history(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockMarketDataService.getHistoryByStockId(stockId));
    }

    /**
     * 新增行情记录
     * @param data 行情数据
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockMarketData data) {
        return ResponseEntity.ok(stockMarketDataService.insert(data));
    }

    /**
     * 统计全市场平均股息率
     * @return 每只股票的股息率统计
     */
    @GetMapping("/dividend/average")
    public ResponseEntity<List<Map<String, Object>>> averageDividendYield() {
        return ResponseEntity.ok(stockMarketDataService.getAverageDividendYield());
    }
}
