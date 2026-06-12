package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.StockMarketQuoteDto;
import org.seaPack.service.market.StockMarketQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 股票行情展示控制器
 * <p>
 * 以 stock_basic 为基础表，关联 stock_realtime_quote（最新交易日）和 stock_dividend（最新年份），
 * 提供股票行情分页查询接口，含涨跌幅、股息率等计算指标。
 */
@Slf4j
@RestController
@RequestMapping("/stockMarketQuote")
public class StockMarketQuoteController {

    @Autowired
    private StockMarketQuoteService stockMarketQuoteService;

    /**
     * 分页查询股票行情数据
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param query    筛选条件（stockCode、stockName、exchange、industry、keywords）
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<StockMarketQuoteDto>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody(required = false) StockMarketQuoteDto query) {
        if (query == null) {
            query = new StockMarketQuoteDto();
        }
        PageInfo<StockMarketQuoteDto> pageInfo = stockMarketQuoteService.getQuotePage(pageNum, pageSize, query);
        return ResponseEntity.ok(pageInfo);
    }
}
