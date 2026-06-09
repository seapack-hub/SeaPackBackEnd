package org.seaPack.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.StockDividendMapper;
import org.seaPack.model.StockDividend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 股票分红明细服务
 * <p>
 * 提供分红记录的增删改查及分页功能。
 * 新增时通过 stock_code + year + dividend_type 复合唯一键校验是否重复。
 */
@Slf4j
@Service
@Transactional(readOnly = false)
public class StockDividendService {

    @Autowired
    private StockDividendMapper stockDividendMapper;

    /**
     * 多条件分页查询
     *
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @param stockCode    股票代码
     * @param year         分红年份
     * @param dividendType 分红类型
     * @param status       实施状态
     * @param keyword      关键字
     * @return 分页结果
     */
    public PageInfo<StockDividend> getList(int pageNum, int pageSize, String stockCode,
                                           Integer year, String dividendType,
                                           String status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(stockDividendMapper.selectList(stockCode, year, dividendType, status, keyword));
    }

    /**
     * 按 ID 查询分红详情
     *
     * @param id 主键ID
     * @return 分红记录
     */
    public StockDividend getById(Long id) {
        return stockDividendMapper.selectById(id);
    }

    /**
     * 新增分红记录（含唯一性校验）
     *
     * @param record 分红数据
     * @return 影响行数
     * @throws RuntimeException 同一股票同一年的同类型分红已存在时抛出
     */
    public int insert(StockDividend record) {
        StockDividend existing = stockDividendMapper.selectByUnique(
                record.getStockCode(), record.getYear(), record.getDividendType());
        if (existing != null) {
            throw new RuntimeException("股票 " + record.getStockCode()
                    + " " + record.getYear() + " 年 " + record.getDividendType() + " 分红已存在！");
        }
        return stockDividendMapper.insert(record);
    }

    /**
     * 更新分红记录（含存在性校验）
     *
     * @param record 待更新的数据
     * @return 影响行数
     * @throws RuntimeException 记录不存在时抛出
     */
    public int update(StockDividend record) {
        StockDividend existing = stockDividendMapper.selectById(record.getId());
        if (existing == null) {
            throw new RuntimeException("分红记录 " + record.getId() + " 不存在！");
        }
        return stockDividendMapper.update(record);
    }

    /**
     * 删除分红记录（含存在性校验）
     *
     * @param id 主键ID
     * @return 影响行数
     * @throws RuntimeException 记录不存在时抛出
     */
    public int delete(Long id) {
        StockDividend existing = stockDividendMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分红记录 " + id + " 不存在！");
        }
        return stockDividendMapper.deleteById(id);
    }
}
