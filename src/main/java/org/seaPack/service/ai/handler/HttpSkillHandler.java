package org.seaPack.service.ai.handler;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.ai.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP API 技能执行器
 * <p>通过 RestTemplate 调用内部或外部 HTTP 接口。</p>
 */
@Slf4j
@Component
public class HttpSkillHandler implements SkillHandler {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server.port:8090}")
    private int serverPort;

    @Override
    public String getSkillType() {
        return "http";
    }

    @Override
    public SkillExecutionResult execute(Skill skill, Map<String, Object> params,
                                         String authToken, SseEmitter emitter) {
        long start = System.currentTimeMillis();

        String url = skill.getEndpoint();
        boolean isInternalCall = url.startsWith("/");
        if (isInternalCall) {
            url = "http://localhost:" + serverPort + url;
        }

        // 展平参数
        Map<String, Object> flatParams = flattenParams(params != null ? params : new HashMap<>());

        // 日期参数兜底：自动填充缺失日期 + 范围截断
        applyDateDefaults(flatParams);

        if (isInternalCall) {
            flatParams.putIfAbsent("pageNum", 1);
            flatParams.putIfAbsent("pageSize", 10);
        }

        // 构建 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8));
        if (isInternalCall && authToken != null && !authToken.isBlank()) {
            headers.set("Authorization", authToken);
        }

        // 静默 RestTemplate（不抛异常）
        RestTemplate silentRt = new RestTemplate();
        silentRt.setRequestFactory(restTemplate.getRequestFactory());
        silentRt.setErrorHandler(new ResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse resp) { return false; }
            public void handleError(ClientHttpResponse resp) {}
        });

        // 构建 GET URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        for (Map.Entry<String, Object> entry : flatParams.entrySet()) {
            if (entry.getValue() != null) {
                uriBuilder.queryParam(entry.getKey(), entry.getValue().toString());
            }
        }
        URI uri = uriBuilder.build().encode().toUri();

        // 先试 POST，失败则 GET 降级
        boolean isGetRequest = false;
        ResponseEntity<Map> responseEntity = null;
        try {
            HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(flatParams, headers);
            responseEntity = silentRt.exchange(url, HttpMethod.POST, postEntity, Map.class);
        } catch (Exception postEx) {
            log.info("技能调试[{}] POST异常: {}，降级GET", skill.getName(), postEx.getMessage());
            isGetRequest = true;
            HttpEntity<Void> getEntity = new HttpEntity<>(null, headers);
            responseEntity = silentRt.exchange(uri, HttpMethod.GET, getEntity, Map.class);
        }

        if (responseEntity != null && responseEntity.getStatusCode().isError()) {
            isGetRequest = true;
            HttpEntity<Void> getEntity = new HttpEntity<>(null, headers);
            responseEntity = silentRt.exchange(uri, HttpMethod.GET, getEntity, Map.class);
        }

        String httpMethod = isGetRequest ? "GET" : "POST";
        String displayUrl = skill.getEndpoint();
        if (isInternalCall) {
            if (isGetRequest) {
                displayUrl = uri.toString().replace("http://localhost:" + serverPort, "");
            } else if (url.contains("?")) {
                displayUrl = displayUrl + url.substring(url.indexOf("?"));
            }
        }

        long durationMs = System.currentTimeMillis() - start;
        int statusCode = responseEntity != null ? responseEntity.getStatusCode().value() : 0;
        Object body = responseEntity != null ? responseEntity.getBody() : null;

        return SkillExecutionResult.json(statusCode, httpMethod, displayUrl, durationMs, body);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flattenParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return new HashMap<>();
        }
        Object queryObj = params.get("query");
        if (queryObj instanceof Map) {
            Map<String, Object> flattened = new HashMap<>();
            flattened.putAll((Map<String, Object>) queryObj);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!"query".equals(entry.getKey()) && !flattened.containsKey(entry.getKey())) {
                    flattened.put(entry.getKey(), entry.getValue());
                }
            }
            return flattened;
        }
        return params;
    }

    /**
     * 日期参数兜底逻辑
     * <p>检测 startDate/endDate/startTime/endTime 等日期参数，
     * 自动填充缺失值并截断超大范围（默认最大 90 天）。</p>
     */
    private void applyDateDefaults(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return;

        // 识别日期参数名（支持多种命名约定）
        String endDateKey = findDateKey(params, "endDate", "end_date", "endTime", "end_time");
        String startDateKey = findDateKey(params, "startDate", "start_date", "startTime", "start_time");

        // 如果没有日期参数，跳过
        if (endDateKey == null && startDateKey == null) return;

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final int MAX_DAYS = 90;

        // 解析 endDate：缺失则默认今天
        LocalDate endDate = null;
        if (endDateKey != null) {
            endDate = parseDate(params.get(endDateKey));
        }
        if (endDate == null) {
            endDate = today;
            if (endDateKey != null) {
                params.put(endDateKey, endDate.format(fmt));
            }
        }

        // 解析 startDate：缺失则默认 endDate - 30 天
        LocalDate startDate = null;
        if (startDateKey != null) {
            startDate = parseDate(params.get(startDateKey));
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
            if (startDateKey != null) {
                params.put(startDateKey, startDate.format(fmt));
            }
        }

        // 范围截断：超过 MAX_DAYS 则向前压缩
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days > MAX_DAYS) {
            LocalDate newStart = endDate.minusDays(MAX_DAYS);
            if (startDateKey != null) {
                params.put(startDateKey, newStart.format(fmt));
                log.info("[HttpSkill] 日期范围截断: {} ~ {} → {} ~ {} (最大 {} 天)",
                        startDate.format(fmt), endDate.format(fmt),
                        newStart.format(fmt), endDate.format(fmt), MAX_DAYS);
            }
        }
    }

    /**
     * 在 params 中查找匹配的日期参数名
     */
    private String findDateKey(Map<String, Object> params, String... candidates) {
        for (String key : candidates) {
            if (params.containsKey(key)) {
                return key;
            }
        }
        return null;
    }

    /**
     * 尝试解析日期值（支持 String 和数值型时间戳）
     */
    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        String str = value.toString().trim();
        if (str.isEmpty() || "auto".equalsIgnoreCase(str)) return null;

        // 尝试 yyyy-MM-dd 格式
        try {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ignored) {}

        // 尝试 yyyy/MM/dd 格式
        try {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (Exception ignored) {}

        // 尝试时间戳（毫秒）
        try {
            long ts = Long.parseLong(str);
            if (ts > 9999999999L) {
                // 毫秒时间戳
                return java.time.Instant.ofEpochMilli(ts)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else {
                // 秒时间戳
                return java.time.Instant.ofEpochSecond(ts)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception ignored) {}

        return null;
    }
}
