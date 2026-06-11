package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.market.StockRealtimeQuote;

import java.util.List;

/**
 * 实时行情 Mapper
 * <p>提供行情数据的单条和批量 upsert（唯一键冲突时更新），以及最新行情查询。</p>
 */
@Mapper
public interface StockRealtimeQuoteMapper {

    /** 按股票代码查询最新行情 */
    StockRealtimeQuote selectLatestByCode(@Param("stockCode") String stockCode);

    /** 查询所有股票最新行情 */
    List<StockRealtimeQuote> selectAllLatest();

    /** 查询指定日期的行情 */
    StockRealtimeQuote selectByCodeAndDate(@Param("stockCode") String stockCode, @Param("tradeDate") String tradeDate);

    /** 插入或更新（唯一键冲突时更新） */
    int upsert(StockRealtimeQuote record);

    /** 批量插入或更新 */
    int batchUpsert(List<StockRealtimeQuote> list);
}
