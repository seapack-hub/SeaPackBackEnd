package org.seaPack.service.parse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 分红数据导出服务
 * <p>读取本地 a_stock_dividend_akshare.parquet 文件，生成 TXT 报告或 SQL 脚本保存到桌面。</p>
 */
@Slf4j
@Service
public class DividendExportService {

    private static final String DB_URL = "jdbc:duckdb:";
    private static final String FILE_NAME = "a_stock_dividend_akshare.parquet";

    @Value("${stock.dividend.parquet.dir}")
    private String parquetDir;

    /**
     * 导出分红记录 TXT 文件到桌面
     */
    public String exportTxt() {
        String parquetPath = getParquetPath();
        String desktop = System.getProperty("user.home") + "/Desktop";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "stock_dividend_export_" + timestamp + ".txt";
        Path outputPath = Paths.get(desktop, fileName);

        String sql = "SELECT stock_code, stock_name, year, dividend_type, "
                + "cash_per_share, bonus_shares_per_10, transfer_shares_per_10, "
                + "plan_text, ex_dividend_date, status "
                + "FROM read_parquet('" + parquetPath + "') "
                + "ORDER BY stock_code ASC, year DESC, dividend_type ASC";

        log.info("DuckDB SQL: {}", sql);

        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PrintWriter writer = new PrintWriter(outputPath.toFile(), StandardCharsets.UTF_8)) {

            writer.println("A股历史分红数据导出");
            writer.println("=".repeat(60));
            writer.println("导出时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println("数据来源: " + getParquetPath());
            writer.println("=".repeat(60));
            writer.println();

            while (rs.next()) {
                writer.println("股票代码    : " + nullToEmpty(rs.getString("stock_code")));
                writer.println("股票名称    : " + nullToEmpty(rs.getString("stock_name")));
                writer.println("分红年份    : " + rs.getInt("year"));
                writer.println("分红类型    : " + nullToEmpty(rs.getString("dividend_type")));
                writer.println("每股派息    : " + rs.getBigDecimal("cash_per_share").stripTrailingZeros().toPlainString());
                writer.println("每10股送股  : " + rs.getBigDecimal("bonus_shares_per_10").stripTrailingZeros().toPlainString());
                writer.println("每10股转增  : " + rs.getBigDecimal("transfer_shares_per_10").stripTrailingZeros().toPlainString());
                writer.println("方案原文    : " + nullToEmpty(rs.getString("plan_text")));
                writer.println("除权除息日  : " + nullToEmpty(rs.getString("ex_dividend_date")));
                writer.println("状态        : " + nullToEmpty(rs.getString("status")));
                writer.println("-".repeat(60));
                count++;
            }

            writer.println();
            writer.println("=".repeat(60));
            writer.println("共 " + count + " 条分红记录");
            writer.println("=".repeat(60));

            log.info("分红 TXT 导出完成：{}，共 {} 条", outputPath.toAbsolutePath(), count);

        } catch (Exception e) {
            log.error("导出分红 TXT 失败", e);
            throw new RuntimeException("导出分红 TXT 失败", e);
        }

        return outputPath.toAbsolutePath().toString();
    }

    /**
     * 根据本地 Parquet 生成 stock_dividend INSERT IGNORE SQL 文件
     * <p>新版本 akshare 数据已预处理好字段，直接映射入库。</p>
     */
    public String generateSql() {
        String parquetPath = getParquetPath();
        String desktop = System.getProperty("user.home") + "/Desktop";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "stock_dividend_import_" + timestamp + ".sql";
        Path outputPath = Paths.get(desktop, fileName);

        String sql = "SELECT stock_code, year, dividend_type, "
                + "cash_per_share, bonus_shares_per_10, transfer_shares_per_10, "
                + "plan_text, announcement_date, ex_dividend_date, status "
                + "FROM read_parquet('" + parquetPath + "') "
                + "ORDER BY stock_code ASC, year DESC, dividend_type ASC";

        log.info("DuckDB SQL: {}", sql);

        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PrintWriter writer = new PrintWriter(outputPath.toFile(), StandardCharsets.UTF_8)) {

            writer.println("-- ============================================================");
            writer.println("-- stock_dividend 导入 SQL");
            writer.println("-- 生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println("-- 数据来源: " + getParquetPath());
            writer.println("-- ============================================================");
            writer.println();

            writer.println("TRUNCATE TABLE sea_pack.`stock_dividend`;");
            writer.println();

            while (rs.next()) {
                String stockCode = rs.getString("stock_code");
                if (stockCode == null || stockCode.isEmpty()) {
                    continue;
                }

                int year = rs.getInt("year");
                String dividendType = rs.getString("dividend_type");
                if (dividendType == null || dividendType.isEmpty()) {
                    dividendType = "FINAL";
                }

                String cash = rs.getObject("cash_per_share") != null
                        ? rs.getBigDecimal("cash_per_share").toPlainString() : "0";
                String bonus = rs.getObject("bonus_shares_per_10") != null
                        ? rs.getBigDecimal("bonus_shares_per_10").toPlainString() : "0";
                String transfer = rs.getObject("transfer_shares_per_10") != null
                        ? rs.getBigDecimal("transfer_shares_per_10").toPlainString() : "0";

                String planText = rs.getString("plan_text");
                String annDate = rs.getString("announcement_date");
                String exDate = rs.getString("ex_dividend_date");
                String status = rs.getString("status");
                if (status == null || status.isEmpty()) {
                    status = "IMPLEMENTED";
                }

                String values = String.format(
                        "('%s', %d, '%s', %s, %s, %s, %s, %s, %s, '%s')",
                        escapeSql(stockCode),
                        year,
                        escapeSql(dividendType),
                        cash,
                        bonus,
                        transfer,
                        planText != null ? "'" + escapeSql(planText) + "'" : "NULL",
                        annDate != null ? "'" + annDate + "'" : "NULL",
                        exDate != null ? "'" + exDate + "'" : "NULL",
                        escapeSql(status)
                );

                writer.println("INSERT IGNORE INTO sea_pack.`stock_dividend` "
                        + "(`stock_code`, `year`, `dividend_type`, `cash_per_share`, "
                        + "`bonus_shares_per_10`, `transfer_shares_per_10`, "
                        + "`plan_text`, `announcement_date`, `ex_dividend_date`, `status`) "
                        + "VALUES " + values + ";");
                count++;
            }

            writer.println();
            writer.println("-- ============================================================");
            writer.println("-- 生成完成，共 " + count + " 条 INSERT 语句");
            writer.println("-- ============================================================");

            log.info("SQL 文件生成完成：{}，共 {} 条", outputPath.toAbsolutePath(), count);

        } catch (Exception e) {
            log.error("生成 SQL 文件失败", e);
            throw new RuntimeException("生成 SQL 文件失败", e);
        }

        return outputPath.toAbsolutePath().toString();
    }

    private String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("'", "''");
    }

    private String getParquetPath() {
        return parquetDir.replace("\\", "/") + "/" + FILE_NAME;
    }

    private static String nullToEmpty(String val) {
        return val == null ? "" : val;
    }
}
