package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.finance.FundBaseInfo; // 基金基本信息实体
import org.seaPack.service.finance.FundBaseInfoService; // 基金基本信息服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

/**
 * 基金基本信息控制器
 * 提供基金的分页查询、新增、修改、删除和详情接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/fundBaseInfo") // 请求基础路径
public class FundBaseInfoController {

    @Autowired // 注入基金基本信息服务
    private FundBaseInfoService fundBaseInfoService;

    /**
     * 分页查询基金列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param fundBaseInfo 查询条件
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<FundBaseInfo>> getFundBaseInfo(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestBody FundBaseInfo fundBaseInfo) {
        PageInfo<FundBaseInfo> pageInfo = fundBaseInfoService.getFundBaseInfoList(pageNum, pageSize, fundBaseInfo);
        return ResponseEntity.ok(pageInfo);
    }

    /**
     * 新增基金
     * @param fundBaseInfo 基金实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insertFundBaseInfo(@RequestBody FundBaseInfo fundBaseInfo) {
        return ResponseEntity.ok(fundBaseInfoService.insertFundBaseInfo(fundBaseInfo));
    }

    /**
     * 修改基金信息
     * @param fundBaseInfo 基金实体（需含 fundCode）
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> updateFundBaseInfo(@RequestBody FundBaseInfo fundBaseInfo) {
        return ResponseEntity.ok(fundBaseInfoService.updateFundBaseInfo(fundBaseInfo));
    }

    /**
     * 按基金代码删除基金
     * @param fundCode 基金代码
     */
    @DeleteMapping("/delete/{fundCode}")
    public ResponseEntity<Integer> deleteFundBaseInfo(@PathVariable String fundCode) {
        return ResponseEntity.ok(fundBaseInfoService.deleteFundBaseInfo(fundCode));
    }

    /**
     * 查询基金详情
     * @param fundCode 基金代码
     */
    @GetMapping("/detail/{fundCode}")
    public ResponseEntity<FundBaseInfo> getFundBaseInfoDetail(@PathVariable String fundCode) {
        return ResponseEntity.ok(fundBaseInfoService.getFundBaseInfoByCode(fundCode));
    }
}