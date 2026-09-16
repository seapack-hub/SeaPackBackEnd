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
        if (isInternalCall) {
            flatParams.putIfAbsent("pageNum", 1);
            flatParams.putIfAbsent("pageSize", 10);
        }

        // 构建 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
        HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(flatParams, headers);
        ResponseEntity<Map> responseEntity = null;
        try {
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
}
