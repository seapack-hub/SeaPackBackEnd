package org.seaPack.service.finance;

import com.github.pagehelper.PageHelper; // MyBatis 分页插件
import com.github.pagehelper.PageInfo; // 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.mapper.finance.StockDividendMapper; // 股票分红 Mapper
import org.seaPack.model.finance.StockDividend; // 股票分红实体
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.stereotype.Service; // Spring 服务注解
import org.springframework.transaction.annotation.Transactional; // 事务管理

/**
 * 股票分红服务
 * 提供分红记录的分页查询、详情查询、新增、修改和删除功能。
 */
@Slf4j // Lombok 日志
@Service // 标识为 Spring 服务 Bean
@Transactional(readOnly = false) // 启用写入事务
public class StockDividendService {

    @Autowired // 注入股票分红 Mapper
    private StockDividendMapper stockDividendMapper;

    /**
     * 分页查询分红记录
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param stockCode 股票代码（可选）
     * @param year 年度（可选）
     * @param dividendType 分红类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageInfo<StockDividend> getList(int pageNum, int pageSize, String stockCode,
                                           Integer year, String dividendType,
                                           String status, String keyword) {
        PageHelper.startPage(pageNum, pageSize); // 启动分页
        return new PageInfo<>(stockDividendMapper.selectList(stockCode, year, dividendType, status, keyword)); // 查询并返回分页结果
    }

    /**
     * 根据 ID 查询分红详情
     * @param id 记录 ID
     * @return 分红详情
     */
    @Transactional(readOnly = true)
    public StockDividend getById(Long id) {
        return stockDividendMapper.selectById(id); // 调用 Mapper 查询
    }

    /**
     * 新增分红记录
     * @param record 分红实体
     * @return 影响行数
     */
    public int insert(StockDividend record) {
        StockDividend existing = stockDividendMapper.selectByUnique( // 校验唯一性（股票代码 + 年度 + 分红类型）
                record.getStockCode(), record.getYear(), record.getDividendType());
        if (existing != null) { // 已存在则抛出异常
            throw new RuntimeException("股票编号 " + record.getStockCode()
                    + " " + record.getYear() + " " + record.getDividendType() + "-");
        }
        return stockDividendMapper.insert(record); // 执行插入
    }

    /**
     * 修改分红记录
     * @param record 分红实体（需含 id）
     * @return 影响行数
     */
    public int update(StockDividend record) {
        StockDividend existing = stockDividendMapper.selectById(record.getId()); // 校验是否存在
        if (existing == null) { // 不存在则抛出异常
            throw new RuntimeException("分红记录 " + record.getId() + " 不存在！");
        }
        return stockDividendMapper.update(record); // 执行更新
    }

    /**
     * 删除分红记录
     * @param id 记录 ID
     * @return 影响行数
     */
    public int delete(Long id) {
        StockDividend existing = stockDividendMapper.selectById(id); // 校验是否存在
        if (existing == null) { // 不存在则抛出异常
            throw new RuntimeException("分红记录 " + id + " 不存在！");
        }
        return stockDividendMapper.deleteById(id); // 执行删除
    }
}