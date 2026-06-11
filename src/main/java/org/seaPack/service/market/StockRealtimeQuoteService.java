package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.StockRealtimeQuoteMapper;
import org.seaPack.model.market.StockRealtimeQuote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 实时行情服务
 * <p>接收 iTick WebSocket 推送的行情数据，写入 stock_realtime_quote 表，
 * 同时计算动态股息率（预期每股分红 / 当前价）。</p>
 */
@Slf4j
@Service
public class StockRealtimeQuoteService {

    @Autowired
    private StockRealtimeQuoteMapper quoteMapper;

    /** 按股票代码查询最新行情 */
    public StockRealtimeQuote getLatestByCode(String stockCode) {
        return quoteMapper.selectLatestByCode(stockCode);
    }

    /** 查询所有股票最新行情 */
    public List<StockRealtimeQuote> getAllLatest() {
        return quoteMapper.selectAllLatest();
    }

    /**
     * 保存行情数据
     * <p>自动计算动态股息率并写入。</p>
     */
    @Transactional
    public StockRealtimeQuote saveQuote(String stockCode, BigDecimal price,
                                         BigDecimal open, BigDecimal high, BigDecimal low,
                                         BigDecimal expectedDividend) {
        // 获取当前日期作为交易日期
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 创建行情实体并逐字段赋值
        StockRealtimeQuote quote = new StockRealtimeQuote();
        quote.setStockCode(stockCode);
        quote.setCurrentPrice(price);
        quote.setOpenPrice(open);
        quote.setHighPrice(high);
        quote.setLowPrice(low);
        quote.setTradeDate(today);
        // 若有预期分红数据则计算动态股息率 = 预期每股分红 / 当前价 × 100%
        if (expectedDividend != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal yield = expectedDividend.divide(price, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            quote.setDynamicYield(yield);
        }
        // 写入数据库（唯一键冲突时自动更新）
        quoteMapper.upsert(quote);
        // 回查刚写入的记录以返回完整数据
        return quoteMapper.selectByCodeAndDate(stockCode, today);
    }

    /** 批量保存行情 */
    @Transactional
    public void batchSave(List<StockRealtimeQuote> quotes) {
        for (StockRealtimeQuote q : quotes) {
            if (q.getDynamicYield() == null && q.getCurrentPrice() != null
                    && q.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
                q.setDynamicYield(BigDecimal.ZERO);
            }
            q.setTradeDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
        quoteMapper.batchUpsert(quotes);
    }
}
