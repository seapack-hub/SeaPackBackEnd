package org.seaPack.service.finance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
 * 银行股分红 Parquet → SQL 导入服务
 * <p>
 * 读取 D:\\stockInfo\\dividendData 目录下 bank_dividend_cninfo.parquet 文件，
 * 生成 INSERT 语句输出到桌面，映射至 stock_dividend 表。
 */
@Slf4j
@Service
public class StockDividendImportService {

    @Value("${stock.dividend.parquet.dir}")
    private String parquetDir;

    private static final String FILE_NAME = "bank_dividend_cninfo.parquet";

    /**
     * 生成分红导入 SQL 到桌面
     */
    public String generateSql() {
        String filePath = parquetDir.replace("\\", "/") + "/" + FILE_NAME;
        Path fullPath = Paths.get(parquetDir, FILE_NAME);
        if (!Files.exists(fullPath)) {
            throw new RuntimeException("未找到分红 Parquet 文件：" + fullPath);
        }

        List<String[]> rows = readParquet(filePath);
        if (rows.isEmpty()) {
            throw new RuntimeException("分红 Parquet 文件中无有效数据");
        }
        log.info("共读取 {} 条分红记录", rows.size());

        String sql = buildInsertSql(rows);
        log.info("SQL 语句生成完成，共 {} 行", sql.lines().count());

        String desktopPath = System.getProperty("user.home") + "\\Desktop";
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String outputPath = desktopPath + "\\stock_dividend_import_" + today + ".sql";

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            writer.println("-- ============================================================");
            writer.println("-- stock_dividend 导入 SQL");
            writer.println("-- 生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("-- 数据来源: " + FILE_NAME);
            writer.println("-- 记录数: " + rows.size());
            writer.println("-- ============================================================");
            writer.println();
            writer.print(sql);
            writer.println();
            writer.println("-- 导入完成");
        } catch (IOException e) {
            throw new RuntimeException("写入 SQL 文件失败：" + outputPath, e);
        }

        log.info("SQL 文件已保存至：{}", outputPath);
        return outputPath;
    }

    private List<String[]> readParquet(String filePath) {
        String dbUrl = "jdbc:duckdb:";
        String sql = "SELECT stock_code, ex_dividend_date, year, dividend_type, "
                + "cash_per_share, bonus_shares_per_10, transfer_shares_per_10, "
                + "plan_text, announcement_date, status "
                + "FROM read_parquet('" + filePath + "') "
                + "ORDER BY stock_code ASC, year DESC, dividend_type ASC";

        log.info("DuckDB SQL: {}", sql);

        List<String[]> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] row = new String[10];
                row[0] = rs.getString("stock_code");
                row[1] = rs.getString("ex_dividend_date");
                row[2] = rs.getObject("year") != null ? String.valueOf(rs.getInt("year")) : "";
                row[3] = rs.getString("dividend_type");
                row[4] = rs.getObject("cash_per_share") != null ? rs.getBigDecimal("cash_per_share").toPlainString() : "0";
                row[5] = rs.getObject("bonus_shares_per_10") != null ? rs.getBigDecimal("bonus_shares_per_10").toPlainString() : "0";
                row[6] = rs.getObject("transfer_shares_per_10") != null ? rs.getBigDecimal("transfer_shares_per_10").toPlainString() : "0";
                row[7] = rs.getString("plan_text");
                row[8] = rs.getString("announcement_date");
                row[9] = rs.getString("status");
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("读取分红 Parquet 文件失败：" + filePath, e);
        }
        return rows;
    }

    private String buildInsertSql(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `stock_dividend` (\n")
          .append("  `stock_code`, `year`, `dividend_type`,\n")
          .append("  `cash_per_share`, `bonus_shares_per_10`, `transfer_shares_per_10`,\n")
          .append("  `plan_text`, `announcement_date`, `ex_dividend_date`, `status`\n")
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
                String exDate = row[1] != null ? "'" + escapeSql(row[1]) + "'" : "NULL";
                String year = row[2];
                String dividendType = "'" + escapeSql(row[3]) + "'";
                String cash = row[4];
                String bonus = row[5];
                String transfer = row[6];
                String planText = row[7] != null ? "'" + escapeSql(row[7]) + "'" : "NULL";
                String annDate = row[8] != null ? "'" + escapeSql(row[8]) + "'" : "NULL";
                String status = row[9] != null ? "'" + escapeSql(row[9]) + "'" : "'PROPOSED'";

                return "  ('" + stockCode + "', " + year + ", " + dividendType + ", "
                     + cash + ", " + bonus + ", " + transfer + ", "
                     + planText + ", " + annDate + ", " + exDate + ", " + status + ")";
            }).collect(Collectors.joining(",\n")));
        }

        sb.append(";\n");
        return sb.toString();
    }

    private String escapeSql(String value) {
        return value == null ? "" : value.replace("'", "''");
    }
}
