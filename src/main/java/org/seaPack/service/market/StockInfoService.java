package org.seaPack.service.market;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.IndustrySectorMapper;
import org.seaPack.mapper.market.StockInfoMapper;
import org.seaPack.mapper.market.StockMarketDataMapper;
import org.seaPack.model.market.IndustrySector;
import org.seaPack.model.market.StockInfo;
import org.seaPack.model.market.StockMarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = false)
public class StockInfoService {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private StockMarketDataMapper stockMarketDataMapper;

    @Autowired
    private IndustrySectorMapper industrySectorMapper;

    public PageInfo<StockInfo> getStockList(int pageNum, int pageSize, StockInfo param) {
        expandIndustryIds(param);
        PageHelper.startPage(pageNum, pageSize);
        List<StockInfo> list = stockInfoMapper.selectStockList(param);
        return new PageInfo<>(list);
    }

    private void expandIndustryIds(StockInfo param) {
        String industry = param.getIndustry();
        if (industry == null || industry.isEmpty()) {
            return;
        }
        List<String> ids = new ArrayList<>();
        collectDescendantIds(Long.valueOf(industry), ids);
        param.setIndustryIds(ids);
    }

    private void collectDescendantIds(Long parentId, List<String> ids) {
        ids.add(String.valueOf(parentId));
        List<IndustrySector> children = industrySectorMapper.selectByParentId(parentId);
        for (IndustrySector child : children) {
            collectDescendantIds(child.getId(), ids);
        }
    }

    public List<StockInfo> getStockListAll(StockInfo param) {
        expandIndustryIds(param);
        return stockInfoMapper.selectStockList(param);
    }

    public StockInfo getStockById(Long stockId) {
        return stockInfoMapper.selectStockById(stockId);
    }

    public StockInfo getStockByCode(String stockCode) {
        return stockInfoMapper.selectStockByCode(stockCode);
    }

    public int insertStock(StockInfo stockInfo) {
        int count = stockInfoMapper.checkStockCodeExists(stockInfo.getStockCode());
        if (count > 0) {
            throw new RuntimeException("股票代码 " + stockInfo.getStockCode() + " 已存在！");
        }
        return stockInfoMapper.insertStock(stockInfo);
    }

    public int updateStock(StockInfo stockInfo) {
        StockInfo existing = stockInfoMapper.selectStockById(stockInfo.getStockId());
        if (existing == null) {
            throw new RuntimeException("股票不存在");
        }
        return stockInfoMapper.updateStock(stockInfo);
    }

    public int deleteStock(Long stockId) {
        StockInfo existing = stockInfoMapper.selectStockById(stockId);
        if (existing == null) {
            throw new RuntimeException("股票不存在");
        }
        return stockInfoMapper.deleteStock(stockId);
    }

    public StockMarketData getLatestMarketData(Long stockId) {
        return stockMarketDataMapper.selectLatestByStockId(stockId);
    }

    public List<StockMarketData> getDividendHistory(Long stockId) {
        return stockMarketDataMapper.selectHistoryByStockId(stockId);
    }

    public List<StockMarketData> getMarketDataList(StockMarketData param) {
        return stockMarketDataMapper.selectStockMarketList(param);
    }

    public List<Map<String, Object>> getAverageDividendYield() {
        return stockMarketDataMapper.selectAverageDividendYield();
    }
}