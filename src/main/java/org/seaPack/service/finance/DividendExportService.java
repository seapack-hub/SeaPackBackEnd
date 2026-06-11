package org.seaPack.service.finance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 分红数据导出服务
 * <p>读取本地 a_stock_dividend_history.parquet 文件，生成可读的 TXT 报告保存到桌面。</p>
 */
@Slf4j
@Service
public class DividendExportService {

    private static final String DB_URL = "jdbc:duckdb:";
    private static final String FILE_NAME = "a_stock_dividend_history.parquet";

    @Value("${stock.dividend.parquet.dir}")
    private String parquetDir;

    /** 进度中文 → status 字典值映射 */
    private static final Map<String, String> STATUS_MAP = new HashMap<>() {{
        put("预案", "PROPOSED");
        put("预披露", "PROPOSED");
        put("股东大会通过", "APPROVED");
        put("实施", "IMPLEMENTED");
        put("已实施", "IMPLEMENTED");
    }};

    /**
     * 导出分红记录 TXT 文件到桌面
     *
     * @return 生成的文件路径
     */
    public String exportTxt() {
        String parquetPath = getParquetPath();
        String desktop = System.getProperty("user.home") + "/Desktop";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "a_stock_dividend_history_" + timestamp + ".txt";
        Path outputPath = Paths.get(desktop, fileName);

        String sql = "SELECT \"股票代码\", \"公告日期\", \"送股\", \"转增\", \"派息\", "
                + "\"进度\", \"除权除息日\", \"股权登记日\", \"红股上市日\" "
                + "FROM read_parquet('" + parquetPath + "') "
                + "ORDER BY \"公告日期\" DESC";

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
                String stockCode = rs.getString("股票代码");
                String announceDate = rs.getString("公告日期");
                String songGu = rs.getString("送股");
                String zhuanZeng = rs.getString("转增");
                String paiXi = rs.getString("派息");
                String progress = rs.getString("进度");
                String exDate = rs.getString("除权除息日");
                String regDate = rs.getString("股权登记日");
                String listDate = rs.getString("红股上市日");

                writer.println("股票代码  : " + nullToEmpty(stockCode));
                writer.println("公告日期  : " + nullToEmpty(announceDate));
                writer.println("送股      : " + nullToEmpty(songGu));
                writer.println("转增      : " + nullToEmpty(zhuanZeng));
                writer.println("派息      : " + nullToEmpty(paiXi));
                writer.println("进度      : " + nullToEmpty(progress));
                writer.println("除权除息日: " + nullToEmpty(exDate));
                writer.println("股权登记日: " + nullToEmpty(regDate));
                writer.println("红股上市日: " + nullToEmpty(listDate));
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
     * 根据本地 Parquet 生成 stock_dividend INSERT SQL 文件
     * <p>先 TRUNCATE 旧数据，再逐条 INSERT，覆盖全量分红记录。</p>
     *
     * @return 生成的文件路径
     */
    public String generateSql() {
        String parquetPath = getParquetPath();
        String desktop = System.getProperty("user.home") + "/Desktop";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "stock_dividend_init_" + timestamp + ".sql";
        Path outputPath = Paths.get(desktop, fileName);

        String sql = "SELECT \"股票代码\", \"公告日期\", \"送股\", \"转增\", \"派息\", "
                + "\"进度\", \"除权除息日\", \"股权登记日\", \"红股上市日\" "
                + "FROM read_parquet('" + parquetPath + "') "
                + "ORDER BY \"公告日期\" DESC";

        log.info("DuckDB SQL: {}", sql);

        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PrintWriter writer = new PrintWriter(outputPath.toFile(), StandardCharsets.UTF_8)) {

            writer.println("-- ============================================================");
            writer.println("-- stock_dividend 初始化数据 SQL");
            writer.println("-- 生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println("-- 数据来源: " + getParquetPath());
            writer.println("-- ============================================================");
            writer.println();

            writer.println("TRUNCATE TABLE sea_pack.`stock_dividend`;");
            writer.println();

            while (rs.next()) {
                String stockCode = rs.getString("股票代码");
                String announceDate = rs.getString("公告日期");
                String songGu = rs.getString("送股");
                String zhuanZeng = rs.getString("转增");
                String paiXi = rs.getString("派息");
                String progress = rs.getString("进度");
                String exDate = rs.getString("除权除息日");

                if (stockCode == null || stockCode.isEmpty() || announceDate == null || announceDate.isEmpty()) {
                    continue;
                }

                String year = announceDate.length() >= 4 ? announceDate.substring(0, 4) : "0";
                String dividendType = inferDividendType(announceDate);

                BigDecimal cash = parseDecimal(paiXi);
                BigDecimal bonus = parseDecimal(songGu);
                BigDecimal transfer = parseDecimal(zhuanZeng);

                String planText = buildPlanText(cash, bonus, transfer);
                String status = mapStatus(progress);
                String annDate = announceDate;
                String exDivDate = nullToEmpty(exDate);

                String values = String.format(
                        "('%s', %s, '%s', %s, %s, %s, '%s', %s, %s, '%s')",
                        escapeSql(stockCode),
                        year,
                        escapeSql(dividendType),
                        cash,
                        bonus,
                        transfer,
                        escapeSql(planText),
                        annDate.isEmpty() ? "NULL" : "'" + annDate + "'",
                        exDivDate.isEmpty() ? "NULL" : "'" + exDivDate + "'",
                        escapeSql(status)
                );

                writer.println("INSERT INTO sea_pack.`stock_dividend` "
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

    /** 根据公告日期月份推断分红类型：≥7月为中期分红，否则为末期分红 */
    private String inferDividendType(String announceDate) {
        if (announceDate == null || announceDate.length() < 7) return "FINAL";
        try {
            int month = Integer.parseInt(announceDate.substring(5, 7));
            return month >= 7 ? "INTERIM" : "FINAL";
        } catch (NumberFormatException e) {
            return "FINAL";
        }
    }

    /** 中文进度映射为英文 status */
    private String mapStatus(String progress) {
        if (progress == null || progress.isEmpty()) return "PROPOSED";
        return STATUS_MAP.getOrDefault(progress.trim(), "PROPOSED");
    }

    /** 构建分红方案原文 */
    private String buildPlanText(BigDecimal cash, BigDecimal bonus, BigDecimal transfer) {
        StringBuilder sb = new StringBuilder("10");
        if (cash.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("派").append(cash.multiply(BigDecimal.TEN).stripTrailingZeros().toPlainString()).append("元");
        }
        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("送").append(bonus.stripTrailingZeros().toPlainString()).append("股");
        }
        if (transfer.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("转").append(transfer.stripTrailingZeros().toPlainString()).append("股");
        }
        return sb.length() > 2 ? sb.toString() : "";
    }

    /** 安全解析数字字符串为 BigDecimal */
    private BigDecimal parseDecimal(String val) {
        if (val == null || val.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** SQL 字符串转义 */
    private String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("'", "''");
    }

    /** 拼接 Parquet 文件完整路径 */
    private String getParquetPath() {
        return parquetDir.replace("\\", "/") + "/" + FILE_NAME;
    }

    private String nullToEmpty(String val) {
        return val == null ? "" : val;
    }
}
