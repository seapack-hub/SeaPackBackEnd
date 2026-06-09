package org.seaPack.service.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.EastMoneyJsonParser;
import org.seaPack.dto.market.BillboardDto;
import org.seaPack.dto.market.RealtimeQuoteDto;
import org.seaPack.dto.market.StockHistoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
public class EastMoneyStockService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 根据股票代码生成东方财富 secid 格式：沪市前缀 1.，深市前缀 0.
     */
    private String buildSecId(String stockCode) {
        return stockCode.startsWith("6") ? "1." + stockCode : "0." + stockCode;
    }

    /**
     * 构建通用请求头（模拟浏览器访问）
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.add(HttpHeaders.REFERER, "http://quote.eastmoney.com/");
        return headers;
    }

    /**
     * 获取股票历史 K 线数据（带缓存）
     */
    @Cacheable(value = "stockHistory", key = "#stockCode + '_' + #startDate + '_' + #endDate")
    public List<StockHistoryDto> getStockHistory(String stockCode, String startDate, String endDate) {
        String secid = buildSecId(stockCode);

        String url = UriComponentsBuilder.fromHttpUrl("http://push2his.eastmoney.com/api/qt/stock/kline/get")
                .queryParam("secid", secid)
                .queryParam("fields1", "f1,f2,f3,f4,f5,f6")
                .queryParam("fields2", "f51,f52,f53,f54,f55,f56,f57,f58")
                .queryParam("klt", "101")
                .queryParam("fqt", "1")
                .queryParam("beg", startDate)
                .queryParam("end", endDate)
                .toUriString();

        log.info("请求东方财富历史K线数据：stockCode={}, secid={}, start={}, end={}",
                stockCode, secid, startDate, endDate);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(buildHeaders()),
                String.class);

        List<StockHistoryDto> result = EastMoneyJsonParser.parseHistoryKlines(response.getBody());
        log.info("东方财富历史K线数据解析 {} 条", result.size());
        return result;
    }

    /**
     * 获取股票实时行情
     */
    public RealtimeQuoteDto getRealtimeQuote(String stockCode) {
        String secid = buildSecId(stockCode);

        String url = UriComponentsBuilder.fromHttpUrl("http://push2.eastmoney.com/api/qt/stock/get")
                .queryParam("secid", secid)
                .queryParam("fields", "f43,f44,f45,f46,f47,f48,f51,f52,f57,f58,f60")
                .queryParam("ut", "fa5fd1943c7b386f172d6893dbfba10b")
                .queryParam("fltt", "2")
                .toUriString();

        log.info("请求东方财富实时行情：stockCode={}, secid={}", stockCode, secid);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(buildHeaders()),
                String.class);

        RealtimeQuoteDto result = EastMoneyJsonParser.parseRealtimeQuote(response.getBody());
        if (result != null) {
            log.info("东方财富实时行情数据：{}[{}] 最新价={}",
                    result.getStockName(), result.getStockCode(), result.getLatestPrice());
        } else {
            log.warn("东方财富实时行情解析失败：stockCode={}", stockCode);
        }
        return result;
    }

    /**
     * 获取龙虎榜数据（带缓存）
     */
    @Cacheable(value = "stockBillboard", key = "#stockCode + '_' + #date")
    public List<BillboardDto> getBillboardDetails(String stockCode, String date) {
        String url = UriComponentsBuilder.fromHttpUrl("https://datacenter-web.eastmoney.com/api/data/v1/get")
                .queryParam("reportName", "RPT_DAILYBILLBOARD_DETAILSNEW")
                .queryParam("columns", "ALL")
                .queryParam("filter", "(SECURITY_CODE=\"" + stockCode + "\")(TRADE_DATE='" + date + "')")
                .queryParam("pageNumber", "1")
                .queryParam("pageSize", "50")
                .queryParam("sortColumns", "TRADE_DATE")
                .queryParam("sortTypes", "-1")
                .toUriString();

        log.info("请求东方财富龙虎榜数据：stockCode={}, date={}", stockCode, date);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(buildBillboardHeaders()),
                String.class);

        List<BillboardDto> result = EastMoneyJsonParser.parseBillboard(response.getBody());
        log.info("东方财富龙虎榜数据解析 {} 条", result.size());
        return result;
    }

    /**
     * 构建龙虎榜接口专用的请求头
     */
    private HttpHeaders buildBillboardHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.add(HttpHeaders.REFERER, "https://data.eastmoney.com/");
        return headers;
    }
}