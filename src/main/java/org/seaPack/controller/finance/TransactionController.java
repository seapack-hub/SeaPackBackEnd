package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.finance.Transaction;
import org.seaPack.model.finance.TransactionExample;
import org.seaPack.service.finance.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基金交易记录控制器
 * <p>提供交易记录的分页查询、详情、新增、修改、删除等接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * 分页查询交易记录列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param fundCode 基金代码（可选）
     * @param userId 用户ID（可选）
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<Transaction>> getTransactionList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String fundCode,
            @RequestParam(required = false) Integer userId) {
        TransactionExample example = new TransactionExample();
        TransactionExample.Criteria criteria = example.createCriteria();
        if (fundCode != null && !fundCode.isEmpty()) {
            criteria.andFundCodeEqualTo(fundCode);
        }
        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        example.setOrderByClause("trade_date DESC");
        PageInfo<Transaction> pageInfo = transactionService.getTransactionList(pageNum, pageSize, example);
        return ResponseEntity.ok(pageInfo);
    }

    /**
     * 根据基金代码查询交易记录
     * @param fundCode 基金代码
     */
    @GetMapping("/list/fund/{fundCode}")
    public ResponseEntity<List<Transaction>> getTransactionByFundCode(@PathVariable String fundCode) {
        return ResponseEntity.ok(transactionService.getTransactionByFundCode(fundCode));
    }

    /**
     * 根据用户ID查询交易记录
     * @param userId 用户ID
     */
    @GetMapping("/list/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(transactionService.getTransactionByUserId(userId));
    }

    /**
     * 查询交易详情
     * @param id 主键ID
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Transaction> getTransactionDetail(@PathVariable Integer id) {
        Transaction transaction = transactionService.getTransactionById(id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    /**
     * 新增交易记录
     * @param transaction 交易数据
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insertTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.insertTransaction(transaction));
    }

    /**
     * 更新交易记录
     * @param transaction 交易数据
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> updateTransaction(@RequestBody Transaction transaction) {
        if (transaction.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(transactionService.updateTransaction(transaction));
    }

    /**
     * 删除交易记录
     * @param id 主键ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> deleteTransaction(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.deleteTransaction(id));
    }
}
