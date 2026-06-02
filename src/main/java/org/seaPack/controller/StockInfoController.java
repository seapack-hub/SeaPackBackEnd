package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.StockInfo;
import org.seaPack.model.StockMarketData;
import org.seaPack.service.StockInfoService;
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

    /**
     * 分页查询股票列表
     * @param pageNum 页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param param 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<StockInfo>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody(required = false) StockInfo param) {
        if (param == null) param = new StockInfo();
        PageInfo<StockInfo> pageInfo = stockInfoService.getStockList(pageNum, pageSize, param);
        return ResponseEntity.ok(pageInfo);
    }

    /**
     * 查询全部股票列表（不分页）
     * @param param 查询条件
     * @return 股票列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<StockInfo>> list(StockInfo param) {
        return ResponseEntity.ok(stockInfoService.getStockListAll(param));
    }

    /**
     * 新增股票
     * @param stockInfo 股票信息（stockCode/stockName必填）
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockInfo stockInfo) {
        return ResponseEntity.ok(stockInfoService.insertStock(stockInfo));
    }

    /**
     * 更新股票信息
     * @param stockInfo 待更新数据（必须含stockId）
     * @return 影响行数
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody StockInfo stockInfo) {
        return ResponseEntity.ok(stockInfoService.updateStock(stockInfo));
    }

    /**
     * 删除股票
     * @param stockId 股票ID
     * @return 影响行数
     */
    @DeleteMapping("/delete/{stockId}")
    public ResponseEntity<Integer> delete(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.deleteStock(stockId));
    }

    /**
     * 根据ID查询股票详情
     * @param stockId 股票ID
     * @return 股票信息
     */
    @GetMapping("/detail/{stockId}")
    public ResponseEntity<StockInfo> detail(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getStockById(stockId));
    }

    /**
     * 根据股票代码查询
     * @param stockCode 股票代码
     * @return 股票信息
     */
    @GetMapping("/code/{stockCode}")
    public ResponseEntity<StockInfo> byCode(@PathVariable String stockCode) {
        return ResponseEntity.ok(stockInfoService.getStockByCode(stockCode));
    }

    /**
     * 查询股票最新行情
     * @param stockId 股票ID
     * @return 最新行情数据
     */
    @GetMapping("/market/latest/{stockId}")
    public ResponseEntity<StockMarketData> latestMarketData(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getLatestMarketData(stockId));
    }

    /**
     * 查询股票历史分红趋势
     * @param stockId 股票ID
     * @return 历史行情列表
     */
    @GetMapping("/market/history/{stockId}")
    public ResponseEntity<List<StockMarketData>> marketHistory(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getDividendHistory(stockId));
    }

    /**
     * 多条件查询行情数据
     * @param param 查询条件
     * @return 行情数据列表
     */
    @GetMapping("/market/list")
    public ResponseEntity<List<StockMarketData>> marketList(StockMarketData param) {
        return ResponseEntity.ok(stockInfoService.getMarketDataList(param));
    }

    /**
     * 统计全市场平均股息率
     * @return 每只股票的股息率统计
     */
    @GetMapping("/dividend/average")
    public ResponseEntity<List<Map<String, Object>>> averageDividendYield() {
        return ResponseEntity.ok(stockInfoService.getAverageDividendYield());
    }
}
