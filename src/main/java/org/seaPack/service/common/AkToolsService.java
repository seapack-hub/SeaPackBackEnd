package org.seaPack.service.common;

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

    @Value("")
    private String baseUrl;

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

    public JSONArray stockZhAHist(String symbol, String period, String startDate, String endDate, String adjust) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        if (period != null) params.put("period", period);
        if (startDate != null) params.put("start_date", startDate);
        if (endDate != null) params.put("end_date", endDate);
        if (adjust != null) params.put("adjust", adjust);
        return callApi("stock_zh_a_hist", params);
    }

    public JSONArray stockZhASpotEm() {
        return callApi("stock_zh_a_spot_baostock", new HashMap<>());
    }

    public JSONArray stockIndividualInfoEm(String symbol) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        return callApi("stock_individual_info_em", params);
    }

    public JSONArray stockZhIndexDaily(String symbol, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        if (startDate != null) params.put("start_date", startDate);
        if (endDate != null) params.put("end_date", endDate);
        return callApi("stock_zh_index_daily", params);
    }

    public JSONArray stockZhAHistMinEm(String symbol, String period, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        if (period != null) params.put("period", period);
        if (startDate != null) params.put("start_date", startDate);
        if (endDate != null) params.put("end_date", endDate);
        return callApi("stock_zh_a_hist_min_em", params);
    }

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