package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.StockDailyKlineDto;
import org.seaPack.service.market.StockDailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A 股日 K 线数据控制器
 * <p>
 * 提供从本地 Parquet 文件（由 TickFlow 下载）中读取日 K 线数据的接口，
 * 前端可通过股票代码和日期范围筛选数据。
 * 数据格式与 TickFlow 的 klines.get 接口返回的字段一致。
 */
@Slf4j
@RestController
@RequestMapping("/stockDaily")
public class StockDailyController {

    @Autowired
    private StockDailyService stockDailyService;

    /**
     * 查询股票日 K 线数据（支持日期范围筛选）
     * <p>
     * 从本地 Parquet 文件中读取指定股票的日 K 线数据，
     * 支持通过 startDate 和 endDate 参数筛选日期范围。
     * 若不传日期参数则返回该股票的全部历史数据。
     *
     * @param stockCode 股票代码（6 位纯数字），如 600036
     * @param startDate 起始日期，格式 yyyy-MM-dd，可选
     * @param endDate   截止日期，格式 yyyy-MM-dd，可选
     * @return 日 K 线数据列表（按日期升序排列）
     */
    @GetMapping("/klines")
    public ResponseEntity<List<StockDailyKlineDto>> klines(
            @RequestParam String stockCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("查询股票日K线：code={}, start={}, end={}", stockCode, startDate, endDate);

        // 调用服务层查询数据
        List<StockDailyKlineDto> dataList = stockDailyService.getKlines(stockCode, startDate, endDate);

        return ResponseEntity.ok(dataList);
    }
}
