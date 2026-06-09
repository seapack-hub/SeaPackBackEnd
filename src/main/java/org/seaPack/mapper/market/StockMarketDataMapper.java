package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.market.StockMarketData;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockMarketDataMapper {

    /**
     * 多条件查询行情数据列表
     * @param param 查询条件（stockId/stockCode）
     * @return 行情数据列表
     */
    List<StockMarketData> selectStockMarketList(StockMarketData param);

    /**
     * 查询股票最新行情（按时间降序取第一条）
     * @param stockId 股票ID
     * @return 最新行情
     */
    StockMarketData selectLatestByStockId(@Param("stockId") Long stockId);

    /**
     * 查询股票历史行情（按时间升序，用于趋势图）
     * @param stockId 股票ID
     * @return 历史行情列表
     */
    List<StockMarketData> selectHistoryByStockId(@Param("stockId") Long stockId);

    /**
     * 新增行情记录
     * @param data 行情数据
     * @return 影响行数
     */
    int insertStockMarketData(StockMarketData data);

    /**
     * 统计全市场每只股票的平均/最大/最小股息率
     * @return 每只股票的股息率汇总
     */
    List<Map<String, Object>> selectAverageDividendYield();
}