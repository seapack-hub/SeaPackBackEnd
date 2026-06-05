package org.seaPack.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AkToolsService {

    @Value("${aktools.base-url:http://127.0.0.1:8888}")
    private String baseUrl;

    /**
     * 通用调用：请求 AKTools 的任意接口
     *
     * @param function AKShare 函数名，如 stock_zh_a_hist
     * @param params   查询参数 Map
     * @return JSONArray（AKShare 绝大部分接口返回数组）
     */
    public JSONArray callApi(String function, Map<String, Object> params) {
        String url = baseUrl + "/api/public/" + function;
        try (HttpResponse response = HttpRequest.get(url)
                .form(params)
                .timeout(30000)
                .execute()) {
            String body = response.body();
            log.info("AKTools [{}] response: {} bytes", function, body.length());
            Object obj = JSON.parse(body);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            } else if (obj instanceof JSONObject) {
                JSONArray arr = new JSONArray();
                arr.add(obj);
                return arr;
            }
            return new JSONArray();
        } catch (Exception e) {
            log.error("AKTools call failed: {} - {}", function, e.getMessage());
            throw new RuntimeException("AKTools 接口调用失败: " + function, e);
        }
    }

    /**
     * A股历史行情
     */
    public JSONArray stockZhAHist(String symbol, String period, String startDate, String endDate, String adjust) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        if (period != null) params.put("period", period);
        if (startDate != null) params.put("start_date", startDate);
        if (endDate != null) params.put("end_date", endDate);
        if (adjust != null) params.put("adjust", adjust);
        return callApi("stock_zh_a_hist", params);
    }

    /**
     * 实时行情（新浪网）
     */
    public JSONArray stockZhASpotEm() {
        return callApi("stock_zh_a_spot_baostock", new HashMap<>());
    }

    /**
     * 个股信息
     */
    public JSONArray stockIndividualInfoEm(String symbol) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        return callApi("stock_individual_info_em", params);
    }

    /**
     * 指数日线行情
     */
    public JSONArray stockZhIndexDaily(String symbol, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        if (startDate != null) params.put("start_date", startDate);
        if (endDate != null) params.put("end_date", endDate);
        return callApi("stock_zh_index_daily", params);
    }

    /**
     * A股分钟级数据
     */
    public JSONArray stockZhAHistMinEm(String symbol, String period, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        if (period != null) params.put("period", period);
        if (startDate != null) params.put("start_date", startDate);
        if (endDate != null) params.put("end_date", endDate);
        return callApi("stock_zh_a_hist_min_em", params);
    }

    /**
     * 通用 POST 调用（部分 AKShare 接口需要 POST）
     */
    public JSONArray callApiPost(String function, Map<String, Object> params) {
        String url = baseUrl + "/api/public/" + function;
        try (HttpResponse response = HttpRequest.post(url)
                .form(params)
                .timeout(30000)
                .execute()) {
            String body = response.body();
            Object obj = JSON.parse(body);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            } else if (obj instanceof JSONObject) {
                JSONArray arr = new JSONArray();
                arr.add(obj);
                return arr;
            }
            return new JSONArray();
        } catch (Exception e) {
            log.error("AKTools POST call failed: {} - {}", function, e.getMessage());
            throw new RuntimeException("AKTools 接口调用失败: " + function, e);
        }
    }
}
