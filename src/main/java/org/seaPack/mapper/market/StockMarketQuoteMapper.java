package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.seaPack.dto.market.StockMarketQuoteDto;

import java.util.List;

@Mapper
public interface StockMarketQuoteMapper {

    List<StockMarketQuoteDto> selectMarketQuoteList(StockMarketQuoteDto query);
}
