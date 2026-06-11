package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.service.market.StockRealtimeQuoteImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实时行情导入控制器
 * <p>
 * 触发从 Parquet 文件生成 SQL 并导出到桌面的操作。
 */
@Slf4j
@RestController
@RequestMapping("/stockRealtimeQuote")
public class StockRealtimeQuoteImportController {

    @Autowired
    private StockRealtimeQuoteImportService importService;

    /**
     * 读取最新 Parquet 文件并生成 SQL 导入脚本到桌面
     *
     * @return SQL 文件路径
     */
    @GetMapping("/generateSql")
    public Result<String> generateSql() {
        log.info("触发实时行情 SQL 生成");
        String filePath = importService.generateSql();
        return Result.success(filePath);
    }
}
