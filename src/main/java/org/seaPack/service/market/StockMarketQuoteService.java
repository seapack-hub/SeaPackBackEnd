package org.seaPack.service.market;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.StockMarketQuoteDto;
import org.seaPack.mapper.market.StockMarketQuoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StockMarketQuoteService {

    @Autowired
    private StockMarketQuoteMapper stockMarketQuoteMapper;

    public PageInfo<StockMarketQuoteDto> getQuotePage(int pageNum, int pageSize, StockMarketQuoteDto query) {
        PageHelper.startPage(pageNum, pageSize);
        List<StockMarketQuoteDto> list = stockMarketQuoteMapper.selectMarketQuoteList(query);
        return new PageInfo<>(list);
    }
}
