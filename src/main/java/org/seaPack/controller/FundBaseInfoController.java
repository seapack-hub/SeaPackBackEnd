package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.FundBaseInfo;
import org.seaPack.service.FundBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/fundBaseInfo")
public class FundBaseInfoController {

    @Autowired
    private FundBaseInfoService fundBaseInfoService;


    /**
     * 分页查询基金基本信息
     * @param pageNum 页数
     * @param pageSize 每页数
     * @param fundBaseInfo 参数信息
     * @return
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<FundBaseInfo>> getFundBaseInfo(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestBody FundBaseInfo fundBaseInfo) {
        PageInfo<FundBaseInfo> pageInfo = fundBaseInfoService.getFundBaseInfoList(pageNum, pageSize, fundBaseInfo);

        return ResponseEntity.ok(pageInfo);
    }

    /**
     * 插入基金信息
     * @param fundBaseInfo 基金实体
     * @return
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insertFundBaseInfo(@RequestBody FundBaseInfo fundBaseInfo) {
        return ResponseEntity.ok(fundBaseInfoService.insertFundBaseInfo(fundBaseInfo));
    }

    /**
     * 更新基金信息
     * @param fundBaseInfo
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> updateFundBaseInfo(@RequestBody FundBaseInfo fundBaseInfo) {
        return ResponseEntity.ok(fundBaseInfoService.updateFundBaseInfo(fundBaseInfo));
    }

    /**
     * 删除基金信息
     * @param fundCode
     * @return
     */
    @DeleteMapping("/delete/{fundCode}")
    public ResponseEntity<Integer> deleteFundBaseInfo(@PathVariable String fundCode) {
        return ResponseEntity.ok(fundBaseInfoService.deleteFundBaseInfo(fundCode));
    }

    /**
     * 根据Code查询基金详情
     * @param fundCode
     * @return
     */
    @GetMapping("/detail/{fundCode}")
    public ResponseEntity<FundBaseInfo> getFundBaseInfoDetail(@PathVariable String fundCode) {
        return ResponseEntity.ok(fundBaseInfoService.getFundBaseInfoByCode(fundCode));
    }
}
