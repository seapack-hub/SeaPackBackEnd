package org.seaPack.mapper.finance;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.finance.Transaction;
import org.seaPack.model.finance.TransactionExample;

@Mapper
public interface TransactionMapper {

    /**
     * 按条件统计交易记录数
     * @param example 查询条件
     * @return 记录数
     */
    long countByExample(TransactionExample example);

    /**
     * 按条件删除交易记录
     * @param example 条件
     * @return 影响行数
     */
    int deleteByExample(TransactionExample example);

    /**
     * 根据主键删除交易记录
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 新增交易记录（全字段插入）
     * @param row 交易数据
     * @return 影响行数
     */
    int insert(Transaction row);

    /**
     * 新增交易记录（仅非空字段）
     * @param row 交易数据
     * @return 影响行数
     */
    int insertSelective(Transaction row);

    /**
     * 按条件查询交易列表（含BLOB字段）
     * @param example 查询条件
     * @return 交易列表
     */
    List<Transaction> selectByExampleWithBLOBs(TransactionExample example);

    /**
     * 按条件查询交易列表（不含BLOB字段）
     * @param example 查询条件
     * @return 交易列表
     */
    List<Transaction> selectByExample(TransactionExample example);

    /**
     * 根据主键查询交易
     * @param id 主键ID
     * @return 交易信息
     */
    Transaction selectByPrimaryKey(Integer id);

    /**
     * 按条件更新（非空字段）
     * @param row 新数据
     * @param example 条件
     * @return 影响行数
     */
    int updateByExampleSelective(@Param("row") Transaction row, @Param("example") TransactionExample example);

    /**
     * 按条件更新（含BLOB字段）
     * @param row 新数据
     * @param example 条件
     * @return 影响行数
     */
    int updateByExampleWithBLOBs(@Param("row") Transaction row, @Param("example") TransactionExample example);

    /**
     * 按条件更新（全字段，不含BLOB）
     * @param row 新数据
     * @param example 条件
     * @return 影响行数
     */
    int updateByExample(@Param("row") Transaction row, @Param("example") TransactionExample example);

    /**
     * 根据主键更新（非空字段）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKeySelective(Transaction row);

    /**
     * 根据主键更新（含BLOB字段）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKeyWithBLOBs(Transaction row);

    /**
     * 根据主键更新（全字段，不含BLOB）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKey(Transaction row);
}