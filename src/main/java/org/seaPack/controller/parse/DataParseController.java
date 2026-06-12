package org.seaPack.controller.parse;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.dto.market.StockDailyKlineDto;
import org.seaPack.service.parse.DividendExportService;
import org.seaPack.service.parse.StockBasicSyncService;
import org.seaPack.service.parse.StockDailyService;
import org.seaPack.service.parse.StockDividendImportService;
import org.seaPack.service.parse.StockRealtimeQuoteImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据解析控制器
 * <p>
 * 统一托管所有从 Parquet 文件读取数据并生成 SQL/K 线/TXT 数据的接口。
 * 涉及股票日 K 线查询、StockBasic 初始化 SQL 生成、分红导出、银行股分红 SQL 生成、实时行情 SQL 生成。
 */
@Slf4j
@RestController
public class DataParseController {

    @Autowired
    private StockDailyService stockDailyService;

    @Autowired
    private StockBasicSyncService stockBasicSyncService;

    @Autowired
    private StockDividendImportService stockDividendImportService;

    @Autowired
    private StockRealtimeQuoteImportService stockRealtimeQuoteImportService;

    @Autowired
    private DividendExportService dividendExportService;

    /**
     * 查询股票日 K 线数据（支持日期范围筛选）
     * <p>
     * 从本地 Parquet 文件中读取指定股票的日 K 线数据，
     * 支持通过 startDate 和 endDate 参数筛选日期范围。
     * 若不传日期参数则返回该股票的全部历史数据。
     *
     * @param stockCode 股票代码（6 位纯数字），如 600036
     * @param startDate 起始日期，格式 yyyy-MM-dd，可选
     * @param endDate   截止日期，格式 yyyy-MM-dd，可选
     * @return 日 K 线数据列表（按日期升序排列）
     */
    @GetMapping("/stockDaily/klines")
    public ResponseEntity<List<StockDailyKlineDto>> klines(
            @RequestParam String stockCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("查询股票日K线：code={}, start={}, end={}", stockCode, startDate, endDate);

        // 调用服务层查询数据
        List<StockDailyKlineDto> dataList = stockDailyService.getKlines(stockCode, startDate, endDate);

        return ResponseEntity.ok(dataList);
    }

    /**
     * 从 Parquet 标的池生成 StockBasic 初始化 SQL
     * <p>
     * 读取本地 Parquet 文件中全市场 A 股数据，
     * 将交易所代码映射为字典值（SH→SSE, SZ→SZSE, BJ→BSE），
     * 生成 INSERT IGNORE INTO stock_basic SQL 文件并保存到桌面。
     *
     * @return SQL 文件路径
     */
    @GetMapping("/stockInfo/sync/generate-sql")
    public ResponseEntity<String> generateStockBasicSql() {
        String filePath = stockBasicSyncService.generateSqlFile();
        log.info("StockBasic SQL 文件已生成：{}", filePath);
        return ResponseEntity.ok("SQL 文件已生成：" + filePath);
    }

    /**
     * 读取 bank_dividend_cninfo.parquet 生成银行股分红 INSERT SQL 到桌面
     */
    @GetMapping("/stockDividend/generate-bank-sql")
    public ResponseEntity<String> generateBankSql() {
        String filePath = stockDividendImportService.generateSql();
        log.info("银行股分红 SQL 已生成至：{}", filePath);
        return ResponseEntity.ok("银行股分红 SQL 已生成至：" + filePath);
    }

    /**
     * 读取最新 Parquet 文件并生成 SQL 导入脚本到桌面
     *
     * @return SQL 文件路径
     */
    @GetMapping("/stockRealtimeQuote/generateSql")
    public Result<String> generateRealtimeQuoteSql() {
        log.info("触发实时行情 SQL 生成");
        String filePath = stockRealtimeQuoteImportService.generateSql();
        return Result.success(filePath);
    }

    /**
     * 导出分红 TXT 文件到桌面
     * <p>读取 a_stock_dividend_history.parquet 并生成可读 TXT 报告。</p>
     */
    @GetMapping("/stockDividend/export-txt")
    public ResponseEntity<String> exportDividendTxt() {
        String filePath = dividendExportService.exportTxt();
        log.info("分红 TXT 已导出至：{}", filePath);
        return ResponseEntity.ok("分红 TXT 已导出至：" + filePath);
    }

    /**
     * 根据本地 Parquet 生成 stock_dividend INSERT SQL 文件
     * <p>先 TRUNCATE 旧数据，再逐条 INSERT 覆盖全量分红记录。</p>
     */
    @GetMapping("/stockDividend/generate-sql")
    public ResponseEntity<String> generateDividendSql() {
        String filePath = dividendExportService.generateSql();
        log.info("分红 SQL 已生成至：{}", filePath);
        return ResponseEntity.ok("分红 SQL 已生成至：" + filePath);
    }
}
