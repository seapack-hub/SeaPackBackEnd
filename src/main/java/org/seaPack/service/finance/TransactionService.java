package org.seaPack.service.finance;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.finance.TransactionMapper;
import org.seaPack.model.finance.Transaction;
import org.seaPack.model.finance.TransactionExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 基金交易记录服务
 * <p>提供交易记录的分页查询、新增、修改、删除等功能。</p>
 */
@Slf4j
@Service
@Transactional(readOnly = false)
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    /**
     * 分页查询交易记录列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param example 查询条件
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageInfo<Transaction> getTransactionList(int pageNum, int pageSize, TransactionExample example) {
        PageHelper.startPage(pageNum, pageSize);
        List<Transaction> list = transactionMapper.selectByExampleWithBLOBs(example);
        return new PageInfo<>(list);
    }

    /**
     * 根据基金代码查询交易记录
     * @param fundCode 基金代码
     * @return 交易记录列表
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionByFundCode(String fundCode) {
        TransactionExample example = new TransactionExample();
        example.createCriteria().andFundCodeEqualTo(fundCode);
        example.setOrderByClause("trade_date DESC");
        return transactionMapper.selectByExampleWithBLOBs(example);
    }

    /**
     * 根据用户ID查询交易记录
     * @param userId 用户ID
     * @return 交易记录列表
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionByUserId(Integer userId) {
        TransactionExample example = new TransactionExample();
        example.createCriteria().andUserIdEqualTo(userId);
        example.setOrderByClause("trade_date DESC");
        return transactionMapper.selectByExampleWithBLOBs(example);
    }

    /**
     * 根据主键查询交易详情
     * @param id 主键ID
     * @return 交易信息
     */
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Integer id) {
        return transactionMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增交易记录
     * @param transaction 交易数据
     * @return 影响行数
     */
    @Transactional
    public int insertTransaction(Transaction transaction) {
        Date now = new Date();
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(now);
        }
        if (transaction.getUpdatedAt() == null) {
            transaction.setUpdatedAt(now);
        }
        return transactionMapper.insertSelective(transaction);
    }

    /**
     * 更新交易记录
     * @param transaction 交易数据（需含 id）
     * @return 影响行数
     */
    @Transactional
    public int updateTransaction(Transaction transaction) {
        transaction.setUpdatedAt(new Date());
        return transactionMapper.updateByPrimaryKeySelective(transaction);
    }

    /**
     * 根据主键删除交易记录
     * @param id 主键ID
     * @return 影响行数
     */
    @Transactional
    public int deleteTransaction(Integer id) {
        return transactionMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据用户ID删除所有交易记录
     * @param userId 用户ID
     * @return 影响行数
     */
    @Transactional
    public int deleteTransactionByUserId(Integer userId) {
        TransactionExample example = new TransactionExample();
        example.createCriteria().andUserIdEqualTo(userId);
        return transactionMapper.deleteByExample(example);
    }
}
