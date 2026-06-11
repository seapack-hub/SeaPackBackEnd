package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.InstrumentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StockBasic 表数据同步服务
 * <p>
 * 从本地 Parquet 标的池文件中读取全市场 A 股信息，
 * 将 exchange 映射为字典值（SH→SSE, SZ→SZSE, BJ→BSE），
 * 生成 INSERT IGNORE INTO stock_basic SQL 并保存到桌面。
 */
@Slf4j
@Service
public class StockBasicSyncService {

    @Autowired
    private InstrumentService instrumentService;

    /** 交易所代码映射：Parquet 中的原始值 → stock_basic 表存储的字典值 */
    private static final Map<String, String> EXCHANGE_MAP = new HashMap<>() {{
        put("SH", "SSE");   // 上海证券交易所
        put("SZ", "SZSE");  // 深圳证券交易所
        put("BJ", "BSE");   // 北京证券交易所
    }};

    /**
     * SQL 片段最大长度限制，防止单条 SQL 过长导致 MySQL 报错
     */
    private static final int MAX_SQL_LENGTH = 1_000_000;

    /**
     * 生成 INSERT IGNORE SQL 文件
     * <p>
     * 读取标的池全量数据 → 映射交易所 → 生成 SQL → 写入桌面文件。
     * SQL 文件命名为 stock_basic_init_yyyyMMdd_HHmmss.sql。
     *
     * @return 生成的文件路径
     */
    public String generateSqlFile() {
        // 第一步：读取全市场标的数据
        log.info("开始读取全市场标的池...");
        List<InstrumentDto> allInstruments = instrumentService.getAll();
        log.info("读取完成，共 {} 条标的", allInstruments.size());

        // 第二步：确定桌面路径并生成文件名
        String desktop = System.getProperty("user.home") + "/Desktop";
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fileName = "stock_basic_init_" + timestamp + ".sql";
        Path outputPath = Paths.get(desktop, fileName);

        // 第三步：逐条生成 INSERT IGNORE SQL
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {

            // 写文件头注释
            writer.println("-- ============================================================");
            writer.println("-- StockBasic 初始化数据 SQL");
            writer.println("-- 生成时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            writer.println("-- 数据来源: a_stock_instruments.parquet");
            writer.println("-- 标的总数: " + allInstruments.size());
            writer.println("-- ============================================================");
            writer.println();

            // 当前 SQL 语句的 StringBuffer，用于拆分大文件
            StringBuilder sqlBatch = new StringBuilder();
            sqlBatch.append("INSERT IGNORE INTO `stock_basic` (`stock_code`, `stock_name`, `exchange`, `industry`) VALUES\n");

            int recordCount = 0;
            for (int i = 0; i < allInstruments.size(); i++) {
                InstrumentDto inst = allInstruments.get(i);

                // 过滤无效数据：code 或 name 为空则跳过
                String code = inst.getCode();
                String name = inst.getName();
                if (code == null || code.isEmpty() || name == null || name.isEmpty()) {
                    log.warn("跳过无效标的：code={}, name={}", code, name);
                    continue;
                }

                // 映射交易所代码
                String exchangeRaw = inst.getExchange();
                String exchangeMapped = EXCHANGE_MAP.getOrDefault(exchangeRaw, exchangeRaw);
                if (exchangeMapped == null || exchangeMapped.isEmpty()) {
                    log.warn("未知交易所代码：{}，股票 {}", exchangeRaw, code);
                    exchangeMapped = exchangeRaw;
                }

                // 拼接 SQL VALUES 行（industry 默认为 30-化工，后续可修改）
                String valuesRow = String.format("('%s', '%s', '%s', '30')",
                        escapeSql(code),
                        escapeSql(name),
                        escapeSql(exchangeMapped));

                // 计算添加该行后 SQL 总长度，超过限制则先写出当前批次
                int estimateLen = sqlBatch.length() + valuesRow.length() + 10;
                if (recordCount > 0 && estimateLen > MAX_SQL_LENGTH) {
                    // 去掉末尾逗号，加分号结尾
                    sqlBatch.setLength(sqlBatch.length() - 1);
                    sqlBatch.append(";\n\n");
                    writer.print(sqlBatch.toString());

                    // 开启新批次
                    sqlBatch.setLength(0);
                    sqlBatch.append("INSERT IGNORE INTO `stock_basic` (`stock_code`, `stock_name`, `exchange`, `industry`) VALUES\n");
                }

                // 追加 VALUES 行
                if (recordCount > 0) {
                    sqlBatch.append(",\n");
                }
                sqlBatch.append(valuesRow);
                recordCount++;
            }

            // 写出最后一批 SQL
            if (sqlBatch.length() > 0 && recordCount > 0) {
                sqlBatch.append(";\n");
                writer.print(sqlBatch.toString());
            }

            // 写文件尾注释
            writer.println();
            writer.println("-- ============================================================");
            writer.println("-- 生成完成，共 " + recordCount + " 条 INSERT 语句");
            writer.println("-- ============================================================");

            log.info("SQL 文件生成完成：{}，共 {} 条记录", outputPath.toAbsolutePath(), recordCount);

        } catch (Exception e) {
            log.error("生成 SQL 文件失败", e);
            throw new RuntimeException("生成 SQL 文件失败", e);
        }

        return outputPath.toAbsolutePath().toString();
    }

    /**
     * SQL 字符串转义：将单引号替换为两个单引号，防止 SQL 注入或语法错误
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeSql(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }
}
