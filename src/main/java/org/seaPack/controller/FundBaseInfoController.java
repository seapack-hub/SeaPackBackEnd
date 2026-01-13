package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
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
     * @param pageNum
     * @param pageSize
     * @param fundBaseInfo
     * @return
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<FundBaseInfo>> getFundBaseInfo(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestBody FundBaseInfo fundBaseInfo) {
        PageInfo<FundBaseInfo> pageInfo = fundBaseInfoService.getFundBaseInfoList(pageNum, pageSize, fundBaseInfo);

        return ResponseEntity.ok(pageInfo);
    }
}
