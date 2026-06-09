package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.StockDividend;
import org.seaPack.service.StockDividendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 股票分红明细控制器
 * <p>
 * 提供分红记录的分页查询、详情查询、新增、更新和删除接口。
 * 返回的数据中 dividendTypeName（分红类型名称）和 statusName（实施状态名称）
 * 已自动关联 sys_dict 字典表解析。
 */
@Slf4j
@RestController
@RequestMapping("/stockDividend")
public class StockDividendController {

    @Autowired
    private StockDividendService stockDividendService;

    /**
     * 多条件分页查询分红记录
     * <p>
     * 支持按股票代码、年份、分红类型、实施状态筛选，
     * keyword 参数可模糊匹配股票代码或股票名称。
     *
     * @param pageNum      页码，默认 1
     * @param pageSize     每页条数，默认 10
     * @param stockCode    股票代码（精确匹配）
     * @param year         分红年份（精确匹配）
     * @param dividendType 分红类型：INTERIM-中期分红, FINAL-末期分红
     * @param status       实施状态：PROPOSED-预案, APPROVED-已批准, IMPLEMENTED-已实施
     * @param keyword      关键字（模糊匹配股票代码或名称）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageInfo<StockDividend>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String stockCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String dividendType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                stockDividendService.getList(pageNum, pageSize, stockCode, year, dividendType, status, keyword));
    }

    /**
     * 查询分红详情
     *
     * @param id 主键ID
     * @return 分红记录（含字典名称和股票名称）
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockDividend> detail(@PathVariable Long id) {
        return ResponseEntity.ok(stockDividendService.getById(id));
    }

    /**
     * 新增分红记录
     * <p>
     * 同股票同一年份的同类型分红不允许重复新增。
     *
     * @param record 分红数据（stockCode / year / dividendType 必填）
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockDividend record) {
        return ResponseEntity.ok(stockDividendService.insert(record));
    }

    /**
     * 更新分红记录
     *
     * @param record 待更新的分红数据（必须含 id）
     * @return 影响行数
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody StockDividend record) {
        return ResponseEntity.ok(stockDividendService.update(record));
    }

    /**
     * 删除分红记录
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(stockDividendService.delete(id));
    }
}
