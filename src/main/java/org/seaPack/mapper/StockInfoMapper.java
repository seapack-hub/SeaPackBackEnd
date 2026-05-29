package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.StockInfo;

import java.util.List;

@Mapper
public interface StockInfoMapper {

    /**
     * 多条件查询股票列表
     * @param stockInfo 查询条件（支持id/stockCode/stockName/exchange/isDel/keywords）
     * @return 股票列表
     */
    List<StockInfo> selectStockList(StockInfo stockInfo);

    /**
     * 根据ID查询股票
     * @param id 股票ID
     * @return 股票信息
     */
    StockInfo selectStockById(@Param("id") Long id);

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
     * 软删除股票（标记is_del=1）
     * @param id 股票ID
     * @return 影响行数
     */
    int softDeleteStock(@Param("id") Long id);

    /**
     * 物理删除股票
     * @param id 股票ID
     * @return 影响行数
     */
    int hardDeleteStock(@Param("id") Long id);
}
