package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.finance.StockDividend; // 股票分红实体
import org.seaPack.service.finance.DividendExportService; // 分红 TXT 导出服务
import org.seaPack.service.finance.StockDividendService; // 股票分红服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

/**
 * 股票分红控制器
 * 提供股票分红记录的分页查询、详情、新增、修改和删除接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/stockDividend") // 请求基础路径
public class StockDividendController {

    @Autowired // 注入股票分红服务
    private StockDividendService stockDividendService;

    @Autowired // 注入分红 TXT 导出服务
    private DividendExportService dividendExportService;

    /**
     * 分页查询分红记录
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param stockCode 股票代码（可选）
     * @param year 年度（可选）
     * @param dividendType 分红类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     */
    @GetMapping("/list")
    public ResponseEntity<PageInfo<StockDividend>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String stockCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String dividendType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                stockDividendService.getList(pageNum, pageSize, stockCode, year, dividendType, status, keyword));
    }

    /**
     * 查询分红详情
     * @param id 记录 ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockDividend> detail(@PathVariable Long id) {
        return ResponseEntity.ok(stockDividendService.getById(id));
    }

    /**
     * 新增分红记录
     * @param record 分红实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockDividend record) {
        return ResponseEntity.ok(stockDividendService.insert(record));
    }

    /**
     * 修改分红记录
     * @param record 分红实体（需含 id）
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody StockDividend record) {
        return ResponseEntity.ok(stockDividendService.update(record));
    }

    /**
     * 删除分红记录
     * @param id 记录 ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(stockDividendService.delete(id));
    }

    /**
     * 导出分红 TXT 文件到桌面
     * <p>读取 a_stock_dividend_history.parquet 并生成可读 TXT 报告。</p>
     */
    @GetMapping("/export-txt")
    public ResponseEntity<String> exportTxt() {
        String filePath = dividendExportService.exportTxt();
        log.info("分红 TXT 已导出至：{}", filePath);
        return ResponseEntity.ok("分红 TXT 已导出至：" + filePath);
    }

    /**
     * 根据本地 Parquet 生成 stock_dividend INSERT SQL 文件
     * <p>先 TRUNCATE 旧数据，再逐条 INSERT 覆盖全量分红记录。</p>
     */
    @GetMapping("/generate-sql")
    public ResponseEntity<String> generateSql() {
        String filePath = dividendExportService.generateSql();
        log.info("分红 SQL 已生成至：{}", filePath);
        return ResponseEntity.ok("分红 SQL 已生成至：" + filePath);
    }
}