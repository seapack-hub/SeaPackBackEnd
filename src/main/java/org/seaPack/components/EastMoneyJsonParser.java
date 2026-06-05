package org.seaPack.components;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.seaPack.dto.BillboardDto;
import org.seaPack.dto.RealtimeQuoteDto;
import org.seaPack.dto.StockHistoryDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 东方财富接口 JSON 解析工具类
 * <p>
 * 提供静态方法，将东方财富 API 返回的 JSON 字符串解析为对应的 DTO 对象。
 * 各解析方法均内置了空值/格式校验，解析失败时返回空列表或 null。
 */
public class EastMoneyJsonParser {

    /** 工具类禁止实例化 */
    private EastMoneyJsonParser() {
    }

    /**
     * 解析历史 K 线数据
     * <p>
     * 东方财富 push2his 接口返回的 klines 是以逗号分隔的字符串数组，
     * 每个字符串包含：日期、开盘价、收盘价、最高价、最低价、成交量、成交额、振幅。
     *
     * @param jsonStr 接口返回的原始 JSON 字符串
     * @return 解析后的 K 线列表，解析失败返回空列表
     */
    public static List<StockHistoryDto> parseHistoryKlines(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) { // 空字符串直接返回空列表
            return Collections.emptyList();
        }
        JSONObject root = JSON.parseObject(jsonStr); // 将 JSON 字符串解析为 JSONObject
        JSONObject data = root.getJSONObject("data"); // 获取嵌套的 data 对象
        if (data == null || !data.containsKey("klines")) { // data 为空或不含 klines 字段
            return Collections.emptyList();
        }
        JSONArray klinesArray = data.getJSONArray("klines"); // 获取 klines 数组
        List<StockHistoryDto> result = new ArrayList<>(klinesArray.size()); // 初始化结果列表
        for (int i = 0; i < klinesArray.size(); i++) { // 遍历每条 K 线
            String[] fields = klinesArray.getString(i).split(","); // 按逗号分割成字段数组
            if (fields.length < 8) { // 字段数不足 8 说明数据不完整
                continue; // 跳过该条数据
            }
            StockHistoryDto dto = new StockHistoryDto(); // 创建 DTO 对象
            dto.setTradeDate(fields[0]);   // 索引 0：交易日期 yyyy-MM-dd
            dto.setOpenPrice(new BigDecimal(fields[1]));   // 索引 1：开盘价
            dto.setClosePrice(new BigDecimal(fields[2]));  // 索引 2：收盘价
            dto.setHighPrice(new BigDecimal(fields[3]));   // 索引 3：最高价
            dto.setLowPrice(new BigDecimal(fields[4]));    // 索引 4：最低价
            dto.setVolume(Long.parseLong(fields[5]));      // 索引 5：成交量（股）
            dto.setTurnover(new BigDecimal(fields[6]));    // 索引 6：成交额（元）
            dto.setAmplitude(new BigDecimal(fields[7]));   // 索引 7：振幅（%）
            result.add(dto); // 将 DTO 加入结果列表
        }
        return result; // 返回解析后的 K 线列表
    }

    /**
     * 解析实时行情数据
     * <p>
     * 东方财富 push2 实时行情接口返回的 data 对象使用 f 前缀的数字字段名，
     * 解析时映射为 RealtimeQuoteDto 的中文含义字段。
     *
     * @param jsonStr 接口返回的原始 JSON 字符串
     * @return 解析后的实时行情对象，解析失败返回 null
     */
    public static RealtimeQuoteDto parseRealtimeQuote(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) { // 空字符串直接返回 null
            return null;
        }
        JSONObject root = JSON.parseObject(jsonStr); // 将 JSON 字符串解析为 JSONObject
        JSONObject data = root.getJSONObject("data"); // 获取嵌套的 data 对象
        if (data == null) { // data 为空说明没有行情数据
            return null;
        }
        RealtimeQuoteDto dto = new RealtimeQuoteDto(); // 创建 DTO 对象
        dto.setStockCode(data.getString("f57"));         // f57：股票代码
        dto.setStockName(data.getString("f58"));         // f58：股票名称
        dto.setLatestPrice(data.getBigDecimal("f43"));   // f43：最新价
        dto.setOpenPrice(data.getBigDecimal("f46"));     // f46：今开
        dto.setPreClose(data.getBigDecimal("f60"));      // f60：昨收
        dto.setHighPrice(data.getBigDecimal("f44"));     // f44：最高
        dto.setLowPrice(data.getBigDecimal("f45"));      // f45：最低
        dto.setVolume(data.getLong("f47"));              // f47：成交量（股）
        dto.setAmount(data.getBigDecimal("f48"));        // f48：成交额（元）
        dto.setLimitUp(data.getBigDecimal("f51"));       // f51：涨停价
        dto.setLimitDown(data.getBigDecimal("f52"));     // f52：跌停价
        return dto; // 返回解析后的实时行情对象
    }

    /**
     * 解析龙虎榜明细数据
     * <p>
     * 东方财富数据中心龙虎榜接口返回的 data 数组中，每条记录包含
     * 交易日期、股票代码、股票名称、上榜原因、买入额、卖出额、净买额等字段。
     *
     * @param jsonStr 接口返回的原始 JSON 字符串
     * @return 解析后的龙虎榜列表，该日未上榜返回空列表
     */
    public static List<BillboardDto> parseBillboard(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) { // 空字符串直接返回空列表
            return Collections.emptyList();
        }
        JSONObject root = JSON.parseObject(jsonStr); // 将 JSON 字符串解析为 JSONObject
        JSONObject resultObj = root.getJSONObject("result"); // 获取嵌套的 result 对象
        if (resultObj == null || !resultObj.containsKey("data")) { // result 为空或不含 data
            return Collections.emptyList();
        }
        JSONArray dataArray = resultObj.getJSONArray("data"); // 获取龙虎榜明细数据数组
        List<BillboardDto> list = new ArrayList<>(dataArray.size()); // 初始化结果列表
        for (int i = 0; i < dataArray.size(); i++) { // 遍历每条龙虎榜记录
            JSONObject item = dataArray.getJSONObject(i); // 获取当前记录
            BillboardDto dto = new BillboardDto(); // 创建 DTO 对象
            dto.setTradeDate(item.getString("TRADE_DATE"));           // TRADE_DATE：交易日期
            dto.setStockCode(item.getString("SECURITY_CODE"));        // SECURITY_CODE：股票代码
            dto.setStockName(item.getString("SECURITY_NAME_ABBR"));   // SECURITY_NAME_ABBR：股票简称
            dto.setExplain(item.getString("EXPLAIN"));                // EXPLAIN：上榜原因
            dto.setBuyAmt(item.getBigDecimal("BUY_AMT"));            // BUY_AMT：买入额（元）
            dto.setSellAmt(item.getBigDecimal("SELL_AMT"));          // SELL_AMT：卖出额（元）
            dto.setNetAmt(item.getBigDecimal("NET_BUY_AMT"));        // NET_BUY_AMT：净买额（元）
            list.add(dto); // 将 DTO 加入结果列表
        }
        return list; // 返回解析后的龙虎榜列表
    }
}
