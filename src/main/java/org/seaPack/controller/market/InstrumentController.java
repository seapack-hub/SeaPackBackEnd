package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.market.InstrumentDto;
import org.seaPack.service.market.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A 股标的池基础信息控制器
 * <p>
 * 提供从本地 Parquet 文件中读取全市场 A 股标的池基本信息的接口，
 * 支持查询全部标的列表、按股票代码或 symbol 精确查询。
 * 数据由 Python 脚本通过 TickFlow 获取并每日更新。
 */
@Slf4j
@RestController
@RequestMapping("/instrument")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    /**
     * 查询全部标的池列表（按 code 升序排列）
     *
     * @return 全市场标的信息列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<InstrumentDto>> list() {
        log.info("查询全部标的池数据");
        List<InstrumentDto> dataList = instrumentService.getAll();
        return ResponseEntity.ok(dataList);
    }

    /**
     * 根据纯数字股票代码查询标的信息
     *
     * @param code 股票代码（6 位纯数字），如 600000
     * @return 匹配的标的信息
     */
    @GetMapping("/{code}")
    public ResponseEntity<List<InstrumentDto>> getByCode(@PathVariable String code) {
        log.info("按股票代码查询标的：code={}", code);
        List<InstrumentDto> dataList = instrumentService.getByCode(code);
        return ResponseEntity.ok(dataList);
    }

    /**
     * 根据带交易所后缀的 symbol 查询标的信息
     *
     * @param symbol 带交易所后缀的代码，如 600000.SH
     * @return 匹配的标的信息
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<InstrumentDto>> getBySymbol(@PathVariable String symbol) {
        log.info("按 symbol 查询标的：symbol={}", symbol);
        List<InstrumentDto> dataList = instrumentService.getBySymbol(symbol);
        return ResponseEntity.ok(dataList);
    }
}
