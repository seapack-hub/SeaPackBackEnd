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

import java.util.HashMap;
import java.util.Map;

/**
 * 文件生成技能执行器
 * <p>通过 RestTemplate 调用内部文档生成 API，获取生成的文件并返回下载链接。</p>
 * <p>典型流程：调用 endpoint（如 /api/v1/documents/generate）→ 获取 {url, fileName, fileSize} → 返回 file 类型结果。</p>
 */
@Slf4j
@Component
public class FileGenSkillHandler implements SkillHandler {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server.port:8090}")
    private int serverPort;

    @Override
    public String getSkillType() {
        return "file_gen";
    }

    @Override
    public SkillExecutionResult execute(Skill skill, Map<String, Object> params,
                                         String authToken, SseEmitter emitter) {
        long start = System.currentTimeMillis();

        String endpoint = skill.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return SkillExecutionResult.json(400, "FILE_GEN", skill.getCode(),
                    0, Map.of("error", "技能未配置 endpoint"));
        }

        // 构建完整 URL（内部接口补全 host:port）
        String url = endpoint;
        boolean isInternal = endpoint.startsWith("/");
        if (isInternal) {
            url = "http://localhost:" + serverPort + endpoint;
        }

        // 构建请求体：与 DocumentGenerateRequest 格式一致
        // 从 params 中提取 templateType，其余作为文档参数
        Object templateType = params.get("templateType");
        Map<String, Object> docParams = new HashMap<>(params);
        docParams.remove("templateType");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("templateType", templateType);
        requestBody.put("params", docParams);

        // 构建 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8));
        if (isInternal && authToken != null && !authToken.isBlank()) {
            headers.set("Authorization", authToken);
            log.info("文件生成技能[{}] 转发 Token: len={}, prefix={}",
                    skill.getName(), authToken.length(),
                    authToken.length() > 15 ? authToken.substring(0, 15) + "..." : authToken);
        } else {
            log.warn("文件生成技能[{}] authToken 为空! isInternal={}, token={}",
                    skill.getName(), isInternal, authToken);
        }

        // 静默 RestTemplate
        RestTemplate silentRt = new RestTemplate();
        silentRt.setRequestFactory(restTemplate.getRequestFactory());
        silentRt.setErrorHandler(new ResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse resp) { return false; }
            public void handleError(ClientHttpResponse resp) {}
        });

        // 调用文档生成 API
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response;
        try {
            response = silentRt.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("文件生成技能[{}] HTTP 响应: status={}, body={}",
                    skill.getName(),
                    response != null ? response.getStatusCode().value() : "null",
                    response != null ? response.getBody() : "null");
        } catch (Exception e) {
            log.error("文件生成技能[{}] 调用失败: {}", skill.getName(), e.getMessage());
            long durationMs = System.currentTimeMillis() - start;
            return SkillExecutionResult.json(500, "FILE_GEN", endpoint, durationMs,
                    Map.of("error", "调用文档生成接口失败: " + e.getMessage()));
        }

        long durationMs = System.currentTimeMillis() - start;

        if (response == null || response.getBody() == null) {
            return SkillExecutionResult.json(500, "FILE_GEN", endpoint, durationMs,
                    Map.of("error", "文档生成接口返回为空"));
        }

        Map<String, Object> body = response.getBody();

        // 如果返回了 error
        if (body.containsKey("error")) {
            int code = response.getStatusCode().value();
            return SkillExecutionResult.json(code, "FILE_GEN", endpoint, durationMs, body);
        }

        // 兼容包装结构 {code, msg, data: {...}} 和扁平结构 {url, fileName, fileSize}
        Map<String, Object> data = body;
        if (body.containsKey("data") && body.get("data") instanceof Map) {
            data = (Map<String, Object>) body.get("data");
        }

        // 成功：提取 url、fileName、fileSize
        String fileUrl = data.getOrDefault("url", "").toString();
        String fileName = data.getOrDefault("fileName", "generated_file").toString();
        long fileSize = data.containsKey("fileSize") ? ((Number) data.get("fileSize")).longValue() : 0;

        log.info("文件生成技能[{}] 完成: url={}, fileName={}, size={}, duration={}ms",
                skill.getName(), fileUrl, fileName, fileSize, durationMs);

        return SkillExecutionResult.file(fileUrl, fileName, fileSize, durationMs);
    }
}
