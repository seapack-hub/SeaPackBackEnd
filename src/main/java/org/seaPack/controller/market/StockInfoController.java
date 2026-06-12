package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.market.StockInfo; // 股票信息实体
import org.seaPack.service.market.StockInfoService; // 股票信息服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 股票信息控制器
 * 提供股票信息的分页查询、列表查询、新增、修改、删除以及行情数据查询接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/stockInfo") // 请求基础路径
public class StockInfoController {

    @Autowired // 注入股票信息服务
    private StockInfoService stockInfoService;

    /**
     * 分页查询股票列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param param 查询条件
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
     */
    @GetMapping("/list")
    public ResponseEntity<List<StockInfo>> list(StockInfo param) {
        return ResponseEntity.ok(stockInfoService.getStockListAll(param));
    }

    /**
     * 新增股票
     * @param stockInfo 股票实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockInfo stockInfo) {
        return ResponseEntity.ok(stockInfoService.insertStock(stockInfo));
    }

    /**
     * 修改股票信息
     * @param stockInfo 股票实体（需含 stockId）
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody StockInfo stockInfo) {
        return ResponseEntity.ok(stockInfoService.updateStock(stockInfo));
    }

    /**
     * 删除股票
     * @param stockId 股票 ID
     */
    @DeleteMapping("/delete/{stockId}")
    public ResponseEntity<Integer> delete(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.deleteStock(stockId));
    }

    /**
     * 根据 ID 查询股票详情
     * @param stockId 股票 ID
     */
    @GetMapping("/detail/{stockId}")
    public ResponseEntity<StockInfo> detail(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockInfoService.getStockById(stockId));
    }

    /**
     * 根据股票代码查询
     * @param stockCode 股票代码
     */
    @GetMapping("/code/{stockCode}")
    public ResponseEntity<StockInfo> byCode(@PathVariable String stockCode) {
        return ResponseEntity.ok(stockInfoService.getStockByCode(stockCode));
    }

}