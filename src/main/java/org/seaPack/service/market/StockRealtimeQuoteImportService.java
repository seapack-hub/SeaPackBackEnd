package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 实时行情 Parquet → SQL 导入服务
 * <p>
 * 读取 D:\\stockInfo\\realtimeQuote 目录下最新的 stock_realtime_quote_*.parquet 文件，
 * 生成 INSERT ... ON DUPLICATE KEY UPDATE 语句，输出到桌面。
 */
@Slf4j
@Service
public class StockRealtimeQuoteImportService {

    /** Parquet 文件存放目录 */
    @Value("${stock.realtime.parquet.dir}")
    private String parquetDir;

    /** 动态股息率默认值 */
    private static final double DEFAULT_DYNAMIC_YIELD = 0.0;

    /**
     * 读取 Parquet 并生成 SQL 文件到桌面
     * @return 生成的 SQL 文件路径
     */
    public String generateSql() {
        // 1. 查找最新的 Parquet 文件
        Path parquetFile = findLatestParquet();
        if (parquetFile == null) {
            throw new RuntimeException("未找到 Parquet 文件，请先执行 Python 脚本生成数据");
        }
        log.info("找到 Parquet 文件：{}", parquetFile);

        // 2. 通过 DuckDB 读取数据
        List<String[]> rows = readParquet(parquetFile.toString());
        if (rows.isEmpty()) {
            throw new RuntimeException("Parquet 文件中无有效数据");
        }
        log.info("共读取 {} 条记录", rows.size());

        // 3. 生成 SQL
        String sql = buildInsertSql(rows);
        log.info("SQL 语句生成完成，共 {} 行", sql.lines().count());

        // 4. 写入桌面文件
        String desktopPath = System.getProperty("user.home") + "\\Desktop";
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String outputPath = desktopPath + "\\stock_realtime_quote_import_" + today + ".sql";

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.println("-- ============================================================");
            writer.println("-- stock_realtime_quote 导入 SQL");
            writer.println("-- 生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("-- 数据来源: " + parquetFile.getFileName());
            writer.println("-- 记录数: " + rows.size());
            writer.println("-- ============================================================");
            writer.println();
            writer.print(sql);
            writer.println();
            writer.println("-- 导入完成");
            log.info("SQL 文件已保存至：{}", outputPath);
        } catch (IOException e) {
            throw new RuntimeException("写入 SQL 文件失败：" + outputPath, e);
        }

        return outputPath;
    }

    /**
     * 查找目录中最新的 stock_realtime_quote_*.parquet 文件
     */
    private Path findLatestParquet() {
        Path dir = Paths.get(parquetDir);
        if (!Files.exists(dir)) {
            return null;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> files = stream
                    .filter(p -> p.getFileName().toString().startsWith("stock_realtime_quote_")
                            && p.getFileName().toString().endsWith(".parquet"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .collect(Collectors.toList());
            return files.isEmpty() ? null : files.get(0);
        } catch (IOException e) {
            log.warn("扫描 Parquet 目录失败", e);
            return null;
        }
    }

    /**
     * 通过 DuckDB 读取 Parquet 文件，返回行数据列表
     * 每行是一个字符串数组：[stock_code, current_price, open_price, high_price, low_price, trade_date, update_time]
     */
    private List<String[]> readParquet(String filePath) {
        String dbUrl = "jdbc:duckdb:";
        String sql = "SELECT stock_code, current_price, open_price, high_price, low_price, "
                + "trade_date, update_time "
                + "FROM read_parquet('" + filePath.replace("\\", "/") + "') "
                + "ORDER BY stock_code ASC";

        log.info("DuckDB SQL: {}", sql);

        List<String[]> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] row = new String[7];
                row[0] = rs.getString("stock_code");
                row[1] = rs.getObject("current_price") != null ? rs.getBigDecimal("current_price").toPlainString() : "0";
                row[2] = rs.getObject("open_price") != null ? rs.getBigDecimal("open_price").toPlainString() : "NULL";
                row[3] = rs.getObject("high_price") != null ? rs.getBigDecimal("high_price").toPlainString() : "NULL";
                row[4] = rs.getObject("low_price") != null ? rs.getBigDecimal("low_price").toPlainString() : "NULL";
                row[5] = rs.getString("trade_date");
                row[6] = rs.getString("update_time");
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("读取 Parquet 文件失败：" + filePath, e);
        }
        return rows;
    }

    /**
     * 构建 INSERT ... ON DUPLICATE KEY UPDATE 语句
     */
    private String buildInsertSql(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `stock_realtime_quote` (\n")
          .append("  `stock_code`, `current_price`, `open_price`, `high_price`, `low_price`,\n")
          .append("  `dynamic_yield`, `trade_date`, `update_time`\n")
          .append(") VALUES\n");

        int batchSize = 500;
        for (int i = 0; i < rows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rows.size());
            List<String[]> batch = rows.subList(i, end);

            if (i > 0) {
                sb.append(",\n");
            }
            sb.append(batch.stream().map(row -> {
                String stockCode = escapeSql(row[0]);
                String currentPrice = row[1];
                String openPrice = row[2];
                String highPrice = row[3];
                String lowPrice = row[4];
                String tradeDate = row[5];
                String updateTime = row[6];

                return "  ('" + stockCode + "', " + currentPrice + ", " + openPrice + ", "
                     + highPrice + ", " + lowPrice + ", "
                     + DEFAULT_DYNAMIC_YIELD + ", '" + tradeDate + "', '" + updateTime + "')";
            }).collect(Collectors.joining(",\n")));
        }

        sb.append("\nON DUPLICATE KEY UPDATE\n");
        sb.append("  `current_price` = VALUES(`current_price`),\n");
        sb.append("  `open_price` = VALUES(`open_price`),\n");
        sb.append("  `high_price` = VALUES(`high_price`),\n");
        sb.append("  `low_price` = VALUES(`low_price`),\n");
        sb.append("  `dynamic_yield` = VALUES(`dynamic_yield`),\n");
        sb.append("  `update_time` = VALUES(`update_time`)");

        sb.append(";\n");
        return sb.toString();
    }

    private String escapeSql(String value) {
        return value == null ? "" : value.replace("'", "''");
    }
}
