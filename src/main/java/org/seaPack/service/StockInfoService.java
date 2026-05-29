package org.seaPack.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.StockInfoMapper;
import org.seaPack.mapper.StockMarketDataMapper;
import org.seaPack.model.StockInfo;
import org.seaPack.model.StockMarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 分页查询股票列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param param 查询条件（stockCode/stockName/exchange/keywords等）
     * @return 分页结果
     */
    public PageInfo<StockInfo> getStockList(int pageNum, int pageSize, StockInfo param) {
        PageHelper.startPage(pageNum, pageSize);
        List<StockInfo> list = stockInfoMapper.selectStockList(param);
        return new PageInfo<>(list);
    }

    /**
     * 查询全部股票列表（不分页）
     * @param param 查询条件
     * @return 股票列表
     */
    public List<StockInfo> getStockListAll(StockInfo param) {
        return stockInfoMapper.selectStockList(param);
    }

    /**
     * 根据ID查询股票详情
     * @param id 股票ID
     * @return 股票信息
     */
    public StockInfo getStockById(Long id) {
        return stockInfoMapper.selectStockById(id);
    }

    /**
     * 根据股票代码查询
     * @param stockCode 股票代码（如 600519）
     * @return 股票信息
     */
    public StockInfo getStockByCode(String stockCode) {
        return stockInfoMapper.selectStockByCode(stockCode);
    }

    /**
     * 新增股票（含唯一性校验，重复code抛异常）
     * @param stockInfo 股票信息
     * @return 影响行数
     */
    public int insertStock(StockInfo stockInfo) {
        int count = stockInfoMapper.checkStockCodeExists(stockInfo.getStockCode());
        if (count > 0) {
            throw new RuntimeException("股票代码 " + stockInfo.getStockCode() + " 已存在！");
        }
        return stockInfoMapper.insertStock(stockInfo);
    }

    /**
     * 更新股票信息（仅允许修改名称和交易所）
     * @param stockInfo 待更新的股票信息（必须含id）
     * @return 影响行数
     */
    public int updateStock(StockInfo stockInfo) {
        StockInfo existing = stockInfoMapper.selectStockById(stockInfo.getId());
        if (existing == null || existing.getIsDel() == 1) {
            throw new RuntimeException("股票不存在或已被删除");
        }
        return stockInfoMapper.updateStock(stockInfo);
    }

    /**
     * 软删除股票（标记is_del=1）
     * @param id 股票ID
     * @return 影响行数
     */
    public int softDeleteStock(Long id) {
        StockInfo existing = stockInfoMapper.selectStockById(id);
        if (existing == null || existing.getIsDel() == 1) {
            throw new RuntimeException("股票不存在或已被删除");
        }
        return stockInfoMapper.softDeleteStock(id);
    }

    /**
     * 物理删除股票（直接从表中移除）
     * @param id 股票ID
     * @return 影响行数
     */
    public int hardDeleteStock(Long id) {
        return stockInfoMapper.hardDeleteStock(id);
    }

    /**
     * 查询股票最新行情数据
     * @param stockId 股票ID
     * @return 最新行情
     */
    public StockMarketData getLatestMarketData(Long stockId) {
        return stockMarketDataMapper.selectLatestByStockId(stockId);
    }

    /**
     * 查询股票历史分红趋势（按时间升序）
     * @param stockId 股票ID
     * @return 历史行情列表
     */
    public List<StockMarketData> getDividendHistory(Long stockId) {
        return stockMarketDataMapper.selectHistoryByStockId(stockId);
    }

    /**
     * 多条件查询行情数据列表
     * @param param 查询条件（stockId/stockCode）
     * @return 行情数据列表
     */
    public List<StockMarketData> getMarketDataList(StockMarketData param) {
        return stockMarketDataMapper.selectStockMarketList(param);
    }

    /**
     * 统计全市场所有股票的平均股息率
     * @return 每只股票的code/name/avg_yield/max_yield/min_yield/data_count
     */
    public List<Map<String, Object>> getAverageDividendYield() {
        return stockMarketDataMapper.selectAverageDividendYield();
    }
}
