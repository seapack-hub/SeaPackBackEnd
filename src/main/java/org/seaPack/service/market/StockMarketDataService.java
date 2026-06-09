package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.mapper.market.StockMarketDataMapper; // 行情数据 Mapper
import org.seaPack.model.market.StockMarketData; // 行情数据实体
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.stereotype.Service; // Spring 服务注解
import org.springframework.transaction.annotation.Transactional; // 事务管理

import java.util.List; // List 集合
import java.util.Map; // Map 集合

/**
 * 股票行情数据服务
 * 提供行情数据的列表查询、最新行情、历史行情、新增和股息率统计功能。
 */
@Slf4j // Lombok 日志
@Service // 标识为 Spring 服务 Bean
@Transactional(readOnly = false) // 启用写入事务
public class StockMarketDataService {

    @Autowired // 注入行情数据 Mapper
    private StockMarketDataMapper stockMarketDataMapper;

    /**
     * 多条件查询行情数据列表
     * @param param 查询条件
     * @return 行情数据列表
     */
    public List<StockMarketData> getList(StockMarketData param) {
        return stockMarketDataMapper.selectStockMarketList(param); // 调用 Mapper 查询
    }

    /**
     * 查询股票最新行情
     * @param stockId 股票 ID
     * @return 最新行情数据
     */
    public StockMarketData getLatestByStockId(Long stockId) {
        return stockMarketDataMapper.selectLatestByStockId(stockId); // 调用 Mapper 查询最新
    }

    /**
     * 查询股票历史行情（按时间升序）
     * @param stockId 股票 ID
     * @return 历史行情列表
     */
    public List<StockMarketData> getHistoryByStockId(Long stockId) {
        return stockMarketDataMapper.selectHistoryByStockId(stockId); // 调用 Mapper 查询历史
    }

    /**
     * 新增行情数据
     * @param data 行情数据实体
     * @return 影响行数
     */
    public int insert(StockMarketData data) {
        return stockMarketDataMapper.insertStockMarketData(data); // 调用 Mapper 插入
    }

    /**
     * 统计全市场所有股票的平均股息率
     * @return 每只股票的 code/name/avg_yield/max_yield/min_yield/data_count
     */
    public List<Map<String, Object>> getAverageDividendYield() {
        return stockMarketDataMapper.selectAverageDividendYield(); // 调用 Mapper 统计
    }
}