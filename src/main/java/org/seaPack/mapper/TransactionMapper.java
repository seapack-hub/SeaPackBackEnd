package org.seaPack.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Transaction;
import org.seaPack.model.TransactionExample;

public interface TransactionMapper {
    long countByExample(TransactionExample example);

    int deleteByExample(TransactionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Transaction row);

    int insertSelective(Transaction row);

    List<Transaction> selectByExampleWithBLOBs(TransactionExample example);

    List<Transaction> selectByExample(TransactionExample example);

    Transaction selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("row") Transaction row, @Param("example") TransactionExample example);

    int updateByExampleWithBLOBs(@Param("row") Transaction row, @Param("example") TransactionExample example);

    int updateByExample(@Param("row") Transaction row, @Param("example") TransactionExample example);

    int updateByPrimaryKeySelective(Transaction row);

    int updateByPrimaryKeyWithBLOBs(Transaction row);

    int updateByPrimaryKey(Transaction row);
}