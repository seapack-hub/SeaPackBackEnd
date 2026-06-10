package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.InstrumentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A 股标的池 Parquet 文件读取服务
 * <p>
 * 使用 DuckDB JDBC 直接以 SQL 方式查询磁盘上全市场标的池 Parquet 文件，
 * 支持查询全部标的或按股票代码／symbol 精确查询。
 * Parquet 文件由 Python 脚本通过 TickFlow 下载并存放在指定目录。
 */
@Slf4j
@Service
public class InstrumentService {

    /** Parquet 文件存放的目录路径 */
    @Value("${stock.instrument.parquet.dir}")
    private String parquetDir;

    /** Parquet 文件名 */
    private static final String FILE_NAME = "a_stock_instruments.parquet";

    /**
     * 查询全市场标的池列表
     *
     * @return 全部标的信息列表
     */
    public List<InstrumentDto> getAll() {
        return queryInstruments(null, null);
    }

    /**
     * 根据纯数字股票代码查询标的信息
     *
     * @param code 股票代码（6 位纯数字），如 600000
     * @return 匹配的标的信息，未找到时返回空列表
     */
    public List<InstrumentDto> getByCode(String code) {
        return queryInstruments("code", code);
    }

    /**
     * 根据带交易所后缀的 symbol 查询标的信息
     *
     * @param symbol 带交易所后缀的代码，如 600000.SH
     * @return 匹配的标的信息，未找到时返回空列表
     */
    public List<InstrumentDto> getBySymbol(String symbol) {
        return queryInstruments("symbol", symbol);
    }

    /**
     * DuckDB 通用查询方法
     * <p>
     * 读取 Parquet 文件并通过 DuckDB 的 read_parquet 函数执行 SQL 查询。
     * Parquet 中嵌套的 ext 字段经 pandas.json_normalize 展平为 ext.xxx 列名，
     * 故 SQL 中使用双引号引用这些带点的列名。
     *
     * @param field 筛选字段名（可为 null 表示查询全部）
     * @param value 筛选字段值（可为 null）
     * @return 查询结果列表
     */
    private List<InstrumentDto> queryInstruments(String field, String value) {
        // 拼接 Parquet 文件的完整路径
        String filePath = parquetDir.replace("\\", "/") + "/" + FILE_NAME;

        // DuckDB 内存模式 JDBC 连接 URL
        String dbUrl = "jdbc:duckdb:";

        // 构建 SQL：DuckDB 中带点的列名需要用双引号引用
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT symbol, exchange, code, name, region, type, ");
        sql.append("\"ext.type\", \"ext.listing_date\", \"ext.total_shares\", ");
        sql.append("\"ext.float_shares\", \"ext.tick_size\", \"ext.limit_up\", \"ext.limit_down\" ");
        sql.append("FROM read_parquet('").append(filePath).append("')");

        // 动态拼接筛选条件
        if (field != null && value != null && !value.isEmpty()) {
            sql.append(" WHERE ").append(field).append(" = '").append(value.replace("'", "''")).append("'");
        }

        sql.append(" ORDER BY code ASC");

        String sqlStr = sql.toString();
        log.info("DuckDB SQL: {}", sqlStr);

        List<InstrumentDto> resultList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlStr)) {

            while (rs.next()) {
                InstrumentDto dto = new InstrumentDto();
                dto.setSymbol(rs.getString("symbol"));
                dto.setExchange(rs.getString("exchange"));
                dto.setCode(rs.getString("code"));
                dto.setName(rs.getString("name"));
                dto.setRegion(rs.getString("region"));
                dto.setType(rs.getString("type"));
                dto.setExtType(rs.getString("ext.type"));
                dto.setListingDate(rs.getString("ext.listing_date"));
                dto.setTotalShares(rs.getDouble("ext.total_shares"));
                dto.setFloatShares(rs.getDouble("ext.float_shares"));
                dto.setTickSize(rs.getDouble("ext.tick_size"));
                dto.setLimitUp(rs.getDouble("ext.limit_up"));
                dto.setLimitDown(rs.getDouble("ext.limit_down"));
                resultList.add(dto);
            }

            log.info("查询标的池数据：条件 field={}, value={}，共 {} 条", field, value, resultList.size());

        } catch (SQLException e) {
            throw new RuntimeException("读取标的池 Parquet 文件失败：" + filePath, e);
        }

        return resultList;
    }
}
