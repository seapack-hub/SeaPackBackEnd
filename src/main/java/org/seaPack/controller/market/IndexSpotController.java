package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.market.IndexSpot;
import org.seaPack.service.market.IndexSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大盘指数控制器
 * <p>提供大盘指数实时行情的查询接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/stock/index-spot")
public class IndexSpotController {

    @Autowired
    private IndexSpotService indexSpotService;

    /**
     * 查询所有大盘指数（按 sort_order 排序）
     */
    @GetMapping("/list")
    public ResponseEntity<List<IndexSpot>> list() {
        return ResponseEntity.ok(indexSpotService.listAll());
    }
}
