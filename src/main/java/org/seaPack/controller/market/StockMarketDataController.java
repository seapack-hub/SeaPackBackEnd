package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.market.StockMarketData; // 行情数据实体
import org.seaPack.service.market.StockMarketDataService; // 行情数据服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合
import java.util.Map; // Map 集合

/**
 * 股票行情数据控制器
 * 提供行情数据的列表查询、最新行情、历史行情、新增和股息率统计接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/stockMarketData") // 请求基础路径
public class StockMarketDataController {

    @Autowired // 注入行情数据服务
    private StockMarketDataService stockMarketDataService;

    /**
     * 多条件查询行情数据列表
     * @param param 查询条件
     */
    @GetMapping("/list")
    public ResponseEntity<List<StockMarketData>> list(StockMarketData param) {
        return ResponseEntity.ok(stockMarketDataService.getList(param));
    }

    /**
     * 查询股票最新行情
     * @param stockId 股票 ID
     */
    @GetMapping("/latest/{stockId}")
    public ResponseEntity<StockMarketData> latest(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockMarketDataService.getLatestByStockId(stockId));
    }

    /**
     * 查询股票历史行情
     * @param stockId 股票 ID
     */
    @GetMapping("/history/{stockId}")
    public ResponseEntity<List<StockMarketData>> history(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockMarketDataService.getHistoryByStockId(stockId));
    }

    /**
     * 新增行情数据
     * @param data 行情数据实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockMarketData data) {
        return ResponseEntity.ok(stockMarketDataService.insert(data));
    }

    /**
     * 统计全市场平均股息率
     */
    @GetMapping("/dividend/average")
    public ResponseEntity<List<Map<String, Object>>> averageDividendYield() {
        return ResponseEntity.ok(stockMarketDataService.getAverageDividendYield());
    }
}