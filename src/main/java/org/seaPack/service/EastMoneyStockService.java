package org.seaPack.service;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.EastMoneyJsonParser;
import org.seaPack.dto.BillboardDto;
import org.seaPack.dto.RealtimeQuoteDto;
import org.seaPack.dto.StockHistoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * 东方财富股票数据服务
 * <p>
 * 通过 RestTemplate 直接调用东方财富 HTTP API，获取 A 股历史 K 线、实时行情和龙虎榜数据。
 * 使用 @Cacheable 注解对历史 K 线和龙虎榜数据进行本地缓存，避免重复请求。
 */
@Slf4j
@Service
public class EastMoneyStockService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 根据股票代码判断所属市场，生成东方财富 secid 参数
     * <p>
     * 规则：沪市（6开头）使用 1.前缀，深市（0/3开头）使用 0.前缀
     *
     * @param stockCode 股票代码，如 600036
     * @return secid 参数值，如 1.600036
     */
    private String buildSecId(String stockCode) {
        return stockCode.startsWith("6") ? "1." + stockCode : "0." + stockCode;
    }

    /**
     * 构造通用的 HTTP 请求头，模拟浏览器访问
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
     * 获取股票历史 K 线数据（带本地缓存）
     * <p>
     * 调用东方财富 push2his 接口获取日K线数据，使用 @Cacheable 进行结果缓存，
     * 相同参数在缓存有效期内直接返回缓存结果，避免重复请求。
     *
     * @param stockCode 股票代码，如 600036
     * @param startDate 起始日期，格式 yyyyMMdd，如 20260101
     * @param endDate   截止日期，格式 yyyyMMdd，如 20500101
     * @return 历史 K 线数据列表
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
        log.info("东方财富历史K线数据返回 {} 条", result.size());
        return result;
    }

    /**
     * 获取股票实时行情
     * <p>
     * 调用东方财富 push2 实时行情接口，获取股票的最新报价、涨跌幅等信息。
     * 实时行情不缓存，每次调用都请求最新数据。
     *
     * @param stockCode 股票代码，如 600036
     * @return 实时行情对象
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
     * 获取个股龙虎榜详情（带缓存）
     * <p>
     * 调用东方财富数据中心接口获取龙虎榜明细数据。
     * 注意：日期格式为 yyyy-MM-dd（与历史K线的 yyyyMMdd 格式不同）。
     * 使用 @Cacheable 缓存结果，有效期内相同参数直接返回缓存。
     *
     * @param stockCode 股票代码，如 600036
     * @param date      交易日期，格式 yyyy-MM-dd，如 2026-06-05
     * @return 龙虎榜数据列表，该日未上榜时返回空列表
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
        log.info("东方财富龙虎榜数据返回 {} 条", result.size());
        return result;
    }

    /**
     * 构造龙虎榜接口专用的 HTTP 请求头
     * <p>
     * 龙虎榜属于数据中心板块，Referer 需要设置为 data.eastmoney.com
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
