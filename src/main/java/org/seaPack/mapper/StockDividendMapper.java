package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.StockDividend;

import java.util.List;

/**
 * 股票分红明细 Mapper
 */
@Mapper
public interface StockDividendMapper {

    /**
     * 多条件分页查询分红记录
     *
     * @param stockCode    股票代码（精确匹配）
     * @param year         分红年份（精确匹配）
     * @param dividendType 分红类型（INTERIM/FINAL）
     * @param status       实施状态（PROPOSED/APPROVED/IMPLEMENTED）
     * @param keyword      关键字（模糊匹配股票代码或名称）
     * @return 分红记录列表
     */
    List<StockDividend> selectList(@Param("stockCode") String stockCode,
                                   @Param("year") Integer year,
                                   @Param("dividendType") String dividendType,
                                   @Param("status") String status,
                                   @Param("keyword") String keyword);

    /**
     * 按主键查询分红记录
     *
     * @param id 主键ID
     * @return 分红记录
     */
    StockDividend selectById(@Param("id") Long id);

    /**
     * 按唯一约束查询（stockCode + year + dividendType）
     * 用于新增时的重复校验
     */
    StockDividend selectByUnique(@Param("stockCode") String stockCode,
                                 @Param("year") Integer year,
                                 @Param("dividendType") String dividendType);

    /**
     * 新增分红记录
     *
     * @param record 分红数据
     * @return 影响行数
     */
    int insert(StockDividend record);

    /**
     * 更新分红记录
     *
     * @param record 待更新的分红数据
     * @return 影响行数
     */
    int update(StockDividend record);

    /**
     * 删除分红记录
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
