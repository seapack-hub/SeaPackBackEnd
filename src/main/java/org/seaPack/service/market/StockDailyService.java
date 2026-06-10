package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.StockDailyKlineDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A 股日 K 线 Parquet 文件读取服务
 * <p>
 * 使用 DuckDB JDBC 直接以 SQL 方式查询磁盘上的 Parquet 文件，
 * 支持按股票代码筛选和按日期范围过滤。
 * Parquet 文件由 Python 脚本通过 TickFlow 下载并存放在指定目录。
 */
@Slf4j
@Service
public class StockDailyService {

    /** Parquet 文件存放的基础目录 */
    @Value("${stock.parquet.dir:D:\\a_stock_daily_parquet}")
    private String parquetDir;

    /**
     * 查询指定股票在日期范围内的日 K 线数据
     * <p>
     * 通过 DuckDB 的 read_parquet 函数直接读取 Parquet 文件，
     * 使用 SQL WHERE 子句在 DuckDB 层完成日期过滤，避免全量加载。
     *
     * @param stockCode 股票代码（纯数字 6 位，如 600036）
     * @param startDate 起始日期，格式 yyyy-MM-dd，可为 null
     * @param endDate   截止日期，格式 yyyy-MM-dd，可为 null
     * @return 符合条件的 K 线列表（按 trade_date 升序）
     */
    public List<StockDailyKlineDto> getKlines(String stockCode, String startDate, String endDate) {
        // 拼接 Parquet 文件的完整路径
        String filePath = parquetDir.replace("\\", "/") + "/" + stockCode + ".parquet";

        // DuckDB 的 JDBC 连接 URL（使用内存模式）
        String dbUrl = "jdbc:duckdb:";

        // 构建 SQL：使用 read_parquet 读取 Parquet 文件
        // DuckDB 的 read_parquet 返回表结构，可直接用 WHERE 过滤
        String sql = "SELECT symbol, name, timestamp, trade_date, trade_time, "
                + "open, high, low, close, volume, amount "
                + "FROM read_parquet('" + filePath + "') WHERE 1=1";

        // 动态拼接日期过滤条件（trade_date 是字符串，可直接比较）
        if (startDate != null && !startDate.isEmpty()) {
            sql += " AND trade_date >= '" + startDate + "'";
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql += " AND trade_date <= '" + endDate + "'";
        }

        // 按交易日期升序排列
        sql += " ORDER BY trade_date ASC";

        log.info("DuckDB SQL: {}", sql);

        List<StockDailyKlineDto> resultList = new ArrayList<>();

        // 使用 try-with-resources 自动关闭 Connection、Statement、ResultSet
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // 遍历查询结果，逐行映射为 DTO
            while (rs.next()) {
                StockDailyKlineDto dto = new StockDailyKlineDto();
                dto.setSymbol(rs.getString("symbol"));           // 股票代码（带后缀）
                dto.setName(rs.getString("name"));               // 股票名称
                dto.setTimestamp(rs.getLong("timestamp"));       // Unix 毫秒时间戳
                dto.setTradeDate(rs.getString("trade_date"));    // 交易日期
                dto.setTradeTime(rs.getString("trade_time"));    // 交易时间
                dto.setOpen(rs.getDouble("open"));               // 开盘价
                dto.setHigh(rs.getDouble("high"));               // 最高价
                dto.setLow(rs.getDouble("low"));                 // 最低价
                dto.setClose(rs.getDouble("close"));             // 收盘价
                dto.setVolume(rs.getLong("volume"));             // 成交量
                dto.setAmount(rs.getDouble("amount"));           // 成交额
                resultList.add(dto);
            }

            log.info("查询股票 {} 日K线数据：共 {} 条", stockCode, resultList.size());

        } catch (SQLException e) {
            // 捕获 DuckDB 查询异常，转为运行时异常抛出
            throw new RuntimeException("读取 Parquet 文件失败：" + filePath, e);
        }

        return resultList;
    }
}
