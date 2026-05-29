package org.seaPack.service;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.StockMarketDataMapper;
import org.seaPack.model.StockMarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = false)
public class StockMarketDataService {

    @Autowired
    private StockMarketDataMapper stockMarketDataMapper;

    /**
     * 多条件查询行情数据列表
     * @param param 查询条件（stockId/stockCode）
     * @return 行情数据列表
     */
    public List<StockMarketData> getList(StockMarketData param) {
        return stockMarketDataMapper.selectStockMarketList(param);
    }

    /**
     * 查询股票最新行情
     * @param stockId 股票ID
     * @return 最新行情记录
     */
    public StockMarketData getLatestByStockId(Long stockId) {
        return stockMarketDataMapper.selectLatestByStockId(stockId);
    }

    /**
     * 查询股票历史行情（按时间升序，用于趋势图）
     * @param stockId 股票ID
     * @return 历史行情列表
     */
    public List<StockMarketData> getHistoryByStockId(Long stockId) {
        return stockMarketDataMapper.selectHistoryByStockId(stockId);
    }

    /**
     * 新增行情记录
     * @param data 行情数据
     * @return 影响行数
     */
    public int insert(StockMarketData data) {
        return stockMarketDataMapper.insertStockMarketData(data);
    }

    /**
     * 统计全市场所有股票的平均股息率
     * @return 每只股票的股息率汇总
     */
    public List<Map<String, Object>> getAverageDividendYield() {
        return stockMarketDataMapper.selectAverageDividendYield();
    }
}
