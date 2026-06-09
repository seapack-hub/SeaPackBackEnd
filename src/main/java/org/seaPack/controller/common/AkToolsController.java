package org.seaPack.controller.common;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.service.common.AkToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/aktools")
public class AkToolsController {

    @Autowired
    private AkToolsService akToolsService;

    @GetMapping("/stock/hist")
    public ResponseEntity<JSONArray> stockHist(
            @RequestParam String symbol,
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "qfq") String adjust) {
        return ResponseEntity.ok(
                akToolsService.stockZhAHist(symbol, period, startDate, endDate, adjust));
    }

    @GetMapping("/stock/spot")
    public ResponseEntity<JSONArray> stockSpot() {
        return ResponseEntity.ok(akToolsService.stockZhASpotEm());
    }

    @GetMapping("/stock/info")
    public ResponseEntity<JSONArray> stockInfo(@RequestParam String symbol) {
        return ResponseEntity.ok(akToolsService.stockIndividualInfoEm(symbol));
    }

    @GetMapping("/index/daily")
    public ResponseEntity<JSONArray> indexDaily(
            @RequestParam String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(akToolsService.stockZhIndexDaily(symbol, startDate, endDate));
    }

    @GetMapping("/stock/min")
    public ResponseEntity<JSONArray> stockMin(
            @RequestParam String symbol,
            @RequestParam(required = false, defaultValue = "1") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(
                akToolsService.stockZhAHistMinEm(symbol, period, startDate, endDate));
    }

    @GetMapping("/call/{function}")
    public ResponseEntity<JSONArray> call(
            @PathVariable String function,
            @RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(akToolsService.callApi(function, params));
    }

    @PostMapping("/call/{function}")
    public ResponseEntity<JSONArray> callPost(
            @PathVariable String function,
            @RequestBody(required = false) Map<String, Object> params) {
        if (params == null) params = Map.of();
        return ResponseEntity.ok(akToolsService.callApiPost(function, params));
    }
}