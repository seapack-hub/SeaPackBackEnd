package org.seaPack.service.common;

import cn.hutool.http.HttpRequest; // Hutool HTTP 请求
import cn.hutool.http.HttpResponse; // Hutool HTTP 响应
import com.alibaba.fastjson.JSON; // 阿里 fastjson JSON 工具
import com.alibaba.fastjson.JSONArray; // 阿里 fastjson JSON 数组
import com.alibaba.fastjson.JSONObject; // 阿里 fastjson JSON 对象
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.springframework.beans.factory.annotation.Value; // Spring 配置值注入
import org.springframework.stereotype.Service; // Spring 服务注解

import java.util.HashMap; // HashMap
import java.util.Map; // Map 集合

/**
 * AKTools 数据服务
 * 通过 HTTP 调用本地 AKTools 服务（Python 封装的 AKShare 库），
 * 获取 A 股历史 K 线、实时行情、个股信息、指数日线等金融数据。
 */
@Slf4j // Lombok 日志
@Service // 标识为 Spring 服务 Bean
public class AkToolsService {

    @Value("") // AKTools 服务基础地址（从配置文件注入）
    private String baseUrl;

    /**
     * 通用 GET 方法调用 AKTools API
     * @param function 函数名称
     * @param params   查询参数
     * @return JSON 数组（兼容单对象返回，自动包装为数组）
     */
    public JSONArray callApi(String function, Map<String, Object> params) {
        String url = baseUrl + "/api/public/" + function; // 拼接完整请求 URL
        try (HttpResponse response = HttpRequest.get(url) // 发起 GET 请求
                .form(params) // 设置表单参数
                .timeout(30000) // 设置 30 秒超时
                .execute()) { // 执行请求
            String body = response.body(); // 获取响应体字符串
            log.info("AKTools [{}] response: {} bytes", function, body.length()); // 日志记录响应大小
            Object obj = JSON.parse(body); // 解析 JSON
            if (obj instanceof JSONArray) { // 如果本身就是数组，直接返回
                return (JSONArray) obj;
            } else if (obj instanceof JSONObject) { // 如果是对象，包装为单元素数组
                JSONArray arr = new JSONArray();
                arr.add(obj);
                return arr;
            }
            return new JSONArray(); // 其他情况返回空数组
        } catch (Exception e) {
            log.error("AKTools call failed: {} - {}", function, e.getMessage()); // 异常日志
            throw new RuntimeException("AKTools 接口调用失败: " + function, e); // 抛出运行时异常
        }
    }

    /**
     * 获取 A 股历史 K 线数据
     * @param symbol   股票代码
     * @param period   K 线周期（daily/weekly/monthly）
     * @param startDate 起始日期
     * @param endDate   截止日期
     * @param adjust    复权方式（qfq-前复权 hfq-后复权）
     */
    public JSONArray stockZhAHist(String symbol, String period, String startDate, String endDate, String adjust) {
        Map<String, Object> params = new HashMap<>(); // 构建请求参数
        params.put("symbol", symbol); // 股票代码
        if (period != null) params.put("period", period); // 周期（可选）
        if (startDate != null) params.put("start_date", startDate); // 起始日期（可选）
        if (endDate != null) params.put("end_date", endDate); // 截止日期（可选）
        if (adjust != null) params.put("adjust", adjust); // 复权方式（可选）
        return callApi("stock_zh_a_hist", params); // 调用 AKTools API
    }

    /**
     * 获取 A 股实时行情（全市场）
     */
    public JSONArray stockZhASpotEm() {
        return callApi("stock_zh_a_spot_baostock", new HashMap<>()); // 调用实时行情 API
    }

    /**
     * 获取个股基本信息
     * @param symbol 股票代码
     */
    public JSONArray stockIndividualInfoEm(String symbol) {
        Map<String, Object> params = new HashMap<>(); // 构建参数
        params.put("symbol", symbol); // 股票代码
        return callApi("stock_individual_info_em", params); // 调用个股信息 API
    }

    /**
     * 获取指数日线数据
     * @param symbol    指数代码
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    public JSONArray stockZhIndexDaily(String symbol, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>(); // 构建参数
        params.put("symbol", symbol); // 指数代码
        if (startDate != null) params.put("start_date", startDate); // 起始日期（可选）
        if (endDate != null) params.put("end_date", endDate); // 截止日期（可选）
        return callApi("stock_zh_index_daily", params); // 调用指数日线 API
    }

    /**
     * 获取 A 股分钟级 K 线
     * @param symbol    股票代码
     * @param period    分钟周期（1/5/15/30/60）
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    public JSONArray stockZhAHistMinEm(String symbol, String period, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>(); // 构建参数
        params.put("symbol", symbol); // 股票代码
        if (period != null) params.put("period", period); // 周期（可选）
        if (startDate != null) params.put("start_date", startDate); // 起始日期（可选）
        if (endDate != null) params.put("end_date", endDate); // 截止日期（可选）
        return callApi("stock_zh_a_hist_min_em", params); // 调用分钟 K 线 API
    }

    /**
     * 通用 POST 方法调用 AKTools API
     * @param function 函数名称
     * @param params   请求体参数
     * @return JSON 数组
     */
    public JSONArray callApiPost(String function, Map<String, Object> params) {
        String url = baseUrl + "/api/public/" + function; // 拼接请求 URL
        try (HttpResponse response = HttpRequest.post(url) // 发起 POST 请求
                .form(params) // 设置表单参数
                .timeout(30000) // 30 秒超时
                .execute()) { // 执行请求
            String body = response.body(); // 获取响应
            Object obj = JSON.parse(body); // 解析 JSON
            if (obj instanceof JSONArray) { // 数组直接返回
                return (JSONArray) obj;
            } else if (obj instanceof JSONObject) { // 对象包装为数组
                JSONArray arr = new JSONArray();
                arr.add(obj);
                return arr;
            }
            return new JSONArray(); // 其他返回空数组
        } catch (Exception e) {
            log.error("AKTools POST call failed: {} - {}", function, e.getMessage()); // 异常日志
            throw new RuntimeException("AKTools 接口调用失败: " + function, e); // 抛出异常
        }
    }
}