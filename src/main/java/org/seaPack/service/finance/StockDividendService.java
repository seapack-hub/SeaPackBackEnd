package org.seaPack.service.finance;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.finance.StockDividendMapper;
import org.seaPack.model.finance.StockDividend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = false)
public class StockDividendService {

    @Autowired
    private StockDividendMapper stockDividendMapper;

    public PageInfo<StockDividend> getList(int pageNum, int pageSize, String stockCode,
                                           Integer year, String dividendType,
                                           String status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(stockDividendMapper.selectList(stockCode, year, dividendType, status, keyword));
    }

    public StockDividend getById(Long id) {
        return stockDividendMapper.selectById(id);
    }

    public int insert(StockDividend record) {
        StockDividend existing = stockDividendMapper.selectByUnique(
                record.getStockCode(), record.getYear(), record.getDividendType());
        if (existing != null) {
            throw new RuntimeException("股票 " + record.getStockCode()
                    + " " + record.getYear() + " 年 " + record.getDividendType() + " 分红已存在！");
        }
        return stockDividendMapper.insert(record);
    }

    public int update(StockDividend record) {
        StockDividend existing = stockDividendMapper.selectById(record.getId());
        if (existing == null) {
            throw new RuntimeException("分红记录 " + record.getId() + " 不存在！");
        }
        return stockDividendMapper.update(record);
    }

    public int delete(Long id) {
        StockDividend existing = stockDividendMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分红记录 " + id + " 不存在！");
        }
        return stockDividendMapper.deleteById(id);
    }
}