package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.Holding;
import org.seaPack.service.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/holding")
public class HoldingController {

    @Autowired
    private HoldingService holdingService;


    /**
     *  分页查询持仓表
     * @param pageNum
     * @param pageSize
     * @param holding
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageInfo<Holding>> selectHoldingList(@RequestParam(defaultValue = "1") int pageNum,
                                                               @RequestParam(defaultValue = "10") int pageSize,
                                                               Holding holding){
        PageInfo<Holding> holdingPageInfo = holdingService.getHoldingList(pageNum, pageSize, holding);
        return ResponseEntity.ok(holdingPageInfo);
    }
}
