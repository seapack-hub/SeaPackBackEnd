package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.StockInfo;

import java.util.List;

@Mapper
public interface StockInfoMapper {

    /**
     * 多条件查询股票列表
     * @param stockInfo 查询条件（stockId/stockCode/stockName/exchange/industry/keywords）
     * @return 股票列表
     */
    List<StockInfo> selectStockList(StockInfo stockInfo);

    /**
     * 根据ID查询股票
     * @param stockId 股票ID
     * @return 股票信息
     */
    StockInfo selectStockById(@Param("stockId") Long stockId);

    /**
     * 根据股票代码查询
     * @param stockCode 股票代码
     * @return 股票信息
     */
    StockInfo selectStockByCode(@Param("stockCode") String stockCode);

    /**
     * 检查股票代码是否已存在（用于唯一性校验）
     * @param stockCode 股票代码
     * @return 记录数
     */
    int checkStockCodeExists(@Param("stockCode") String stockCode);

    /**
     * 新增股票
     * @param stockInfo 股票信息
     * @return 影响行数
     */
    int insertStock(StockInfo stockInfo);

    /**
     * 更新股票信息
     * @param stockInfo 待更新数据
     * @return 影响行数
     */
    int updateStock(StockInfo stockInfo);

    /**
     * 删除股票
     * @param stockId 股票ID
     * @return 影响行数
     */
    int deleteStock(@Param("stockId") Long stockId);
}
