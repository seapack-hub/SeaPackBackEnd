package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.finance.FundBaseInfo;
import org.seaPack.service.finance.FundBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/fundBaseInfo")
public class FundBaseInfoController {

    @Autowired
    private FundBaseInfoService fundBaseInfoService;

    @PostMapping("/page")
    public ResponseEntity<PageInfo<FundBaseInfo>> getFundBaseInfo(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestBody FundBaseInfo fundBaseInfo) {
        PageInfo<FundBaseInfo> pageInfo = fundBaseInfoService.getFundBaseInfoList(pageNum, pageSize, fundBaseInfo);
        return ResponseEntity.ok(pageInfo);
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insertFundBaseInfo(@RequestBody FundBaseInfo fundBaseInfo) {
        return ResponseEntity.ok(fundBaseInfoService.insertFundBaseInfo(fundBaseInfo));
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> updateFundBaseInfo(@RequestBody FundBaseInfo fundBaseInfo) {
        return ResponseEntity.ok(fundBaseInfoService.updateFundBaseInfo(fundBaseInfo));
    }

    @DeleteMapping("/delete/{fundCode}")
    public ResponseEntity<Integer> deleteFundBaseInfo(@PathVariable String fundCode) {
        return ResponseEntity.ok(fundBaseInfoService.deleteFundBaseInfo(fundCode));
    }

    @GetMapping("/detail/{fundCode}")
    public ResponseEntity<FundBaseInfo> getFundBaseInfoDetail(@PathVariable String fundCode) {
        return ResponseEntity.ok(fundBaseInfoService.getFundBaseInfoByCode(fundCode));
    }
}