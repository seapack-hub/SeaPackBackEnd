package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.StockMarketDataMapper;
import org.seaPack.model.market.StockMarketData;
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

    public List<StockMarketData> getList(StockMarketData param) {
        return stockMarketDataMapper.selectStockMarketList(param);
    }

    public StockMarketData getLatestByStockId(Long stockId) {
        return stockMarketDataMapper.selectLatestByStockId(stockId);
    }

    public List<StockMarketData> getHistoryByStockId(Long stockId) {
        return stockMarketDataMapper.selectHistoryByStockId(stockId);
    }

    public int insert(StockMarketData data) {
        return stockMarketDataMapper.insertStockMarketData(data);
    }

    public List<Map<String, Object>> getAverageDividendYield() {
        return stockMarketDataMapper.selectAverageDividendYield();
    }
}