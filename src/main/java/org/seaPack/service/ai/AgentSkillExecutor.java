package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.SkillExecuteResult;
import org.seaPack.mapper.ai.AgentSkillMapper;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.model.ai.AgentSkill;
import org.seaPack.model.ai.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 技能执行引擎
 * <p>负责技能的 LLM 智能选择、参数提取、endpoint 调用和响应解析。</p>
 */
@Slf4j
@Component
public class AgentSkillExecutor {

    @Autowired
    private AgentSkillMapper agentSkillMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    @Value("${server.port:8080}")
    private int serverPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行 Agent 关联的技能
     * <p>流程：获取已启用技能 → LLM 智能选择 → 逐个提取参数并调用 endpoint → 返回汇总结果。</p>
     *
     * @param agentId    Agent ID
     * @param userMessage 用户原始消息
     * @return 技能执行结果（含输出文本和统计元数据）
     */
    public SkillExecuteResult executeSkills(Long agentId, String userMessage) {
        return executeSkills(agentId, userMessage, null);
    }

    /**
     * 执行 Agent 关联的技能（可传入认证 Token）
     * <p>流程：获取已启用技能 → LLM 智能选择 → 逐个提取参数并调用 endpoint → 返回汇总结果。</p>
     *
     * @param agentId     Agent ID
     * @param userMessage 用户原始消息
     * @param authToken   Bearer Token（用于内部API调用的认证，SSE 异步线程中需手动传入）
     * @return 技能执行结果（含输出文本和统计元数据）
     */
    public SkillExecuteResult executeSkills(Long agentId, String userMessage, String authToken) {
        return executeSkills(agentId, userMessage, authToken, null);
    }

    /**
     * 执行 Agent 关联的技能（支持 SSE 流式进度）
     * <p>流程：获取已启用技能 → LLM 智能选择 → 逐个提取参数并调用 endpoint → 返回汇总结果。</p>
     *
     * @param agentId     Agent ID
     * @param userMessage 用户原始消息
     * @param authToken   Bearer Token（用于内部API调用的认证，SSE 异步线程中需手动传入）
     * @param emitter     SSE 发射器（可选，用于发送流式进度）
     * @return 技能执行结果（含输出文本和统计元数据）
     */
    public SkillExecuteResult executeSkills(Long agentId, String userMessage, String authToken, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        long start = System.currentTimeMillis();
        StringBuilder skillBuilder = new StringBuilder();
        int executedCount = 0;
        int failedCount = 0;
        List<String> skillNames = new ArrayList<>();
        List<Map<String, Object>> skillDetails = new ArrayList<>();

        // 获取 Agent 关联的已启用技能
        List<AgentSkill> enabledSkills = agentSkillMapper.selectByAgentId(agentId).stream()
                .filter(s -> s.getEnabled() != null && s.getEnabled() == 1)
                .sorted(Comparator.comparingInt(s -> s.getSortOrder() != null ? s.getSortOrder() : 0))
                .collect(Collectors.toList());
        log.info("[技能调用] AgentId={}, 从DB查到已启用技能数={}", agentId, enabledSkills.size());

        if (emitter != null) {
            sendSseEvent(emitter, "step_progress", Map.of(
                    "stepIndex", 0,
                    "message", "共找到 " + enabledSkills.size() + " 个已启用技能"
            ));
        }

        if (!enabledSkills.isEmpty()) {
            for (AgentSkill as : enabledSkills) {
                log.info("[技能调用]   技能关联记录: skillId={}, skillName={}, sortOrder={}",
                        as.getSkillId(), as.getSkillName(), as.getSortOrder());
            }
        }

        // 获取 AI 配置（用于 LLM 智能选择技能）
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        log.info("[技能调用] activeProvider={}, config是否为null={}", providerName, config == null);

        // LLM 智能选择最匹配的技能，避免全部执行
        if (config != null) {
            if (emitter != null) {
                sendSseEvent(emitter, "step_progress", Map.of(
                        "stepIndex", 0,
                        "message", "正在通过 LLM 智能选择技能..."
                ));
            }

            int beforeSelectCount = enabledSkills.size();
            enabledSkills = selectSkillsByLLM(userMessage, enabledSkills, config);
            log.info("[技能调用] LLM选择后: 技能数从{}变为{}", beforeSelectCount, enabledSkills.size());

            if (emitter != null) {
                sendSseEvent(emitter, "step_progress", Map.of(
                        "stepIndex", 0,
                        "message", "LLM 选中 " + enabledSkills.size() + " 个技能"
                ));
            }
        } else {
            log.warn("[技能调用] config为null, 跳过LLM选择, 将直接执行{}个技能(但循环内还会再次判断config)", enabledSkills.size());
        }

        for (AgentSkill as : enabledSkills) {
            Skill skill = skillMapper.selectById(as.getSkillId());
            if (skill == null || skill.getStatus() == null || skill.getStatus() != 1) {
                log.warn("[技能调用] 技能skillId={} 无效: skill={}, status={}", as.getSkillId(), skill, skill != null ? skill.getStatus() : "N/A");
                continue;
            }
            if (skill.getEndpoint() == null || skill.getEndpoint().isBlank()) {
                log.warn("[技能调用] 技能[{}] endpoint为空, 跳过", skill.getName());
                continue;
            }

            skillNames.add(skill.getName());

            if (emitter != null) {
                sendSseEvent(emitter, "step_progress", Map.of(
                        "stepIndex", 0,
                        "message", "正在调用技能: " + skill.getName()
                ));
            }

            Map<String, Object> skillDetail = new HashMap<>();
            skillDetail.put("skillId", skill.getId());
            skillDetail.put("skillName", skill.getName());
            skillDetail.put("endpoint", skill.getEndpoint());
            skillDetail.put("description", skill.getDescription());

            try {
                if (config == null) {
                    log.warn("[技能调用] 技能[{}] 因config为null被跳过(activeProvider={}获取不到配置)", skill.getName(), providerName);
                    skillDetail.put("status", "skipped");
                    skillDetail.put("reason", "AI 配置错误");
                    skillDetails.add(skillDetail);
                    continue;
                }

                // LLM 提取参数
                if (emitter != null) {
                    sendSseEvent(emitter, "step_progress", Map.of(
                            "stepIndex", 0,
                            "message", "正在为技能 [" + skill.getName() + "] 提取参数..."
                    ));
                }

                Map<String, Object> extractedParams = extractParamsByLLM(userMessage, skill.getInputSchema(), config);
                skillDetail.put("extractedParams", extractedParams);

                if (emitter != null) {
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", 0,
                            "detailType", "skill_params",
                            "skillName", skill.getName(),
                            "params", extractedParams
                    ));
                }

                // 处理 URL
                String url = skill.getEndpoint();
                boolean isInternalCall = url.startsWith("/");
                if (isInternalCall) {
                    url = "http://localhost:" + serverPort + url;
                }

                // 构建请求体
                Map<String, Object> requestBody;
                boolean isGetRequest = false;
                if (isInternalCall) {
                    requestBody = flattenParams(extractedParams);
                    // 内部接口补充分页默认值，确保 PageHelper 分页正常
                    requestBody.putIfAbsent("pageNum", 1);
                    requestBody.putIfAbsent("pageSize", 10);
                    log.info("技能[{}] 请求体: {}", skill.getName(), objectMapper.writeValueAsString(requestBody));
                } else {
                    requestBody = new HashMap<>();
                    requestBody.put("model", config.getChatModel());
                    requestBody.put("query", userMessage);
                    requestBody.put("params", extractedParams);
                }

                skillDetail.put("requestBody", requestBody);

                // 构建 Headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (isInternalCall) {
                    // 优先使用传入的 authToken，其次从请求上下文中获取
                    if (authToken != null && !authToken.isBlank()) {
                        headers.set("Authorization", authToken);
                    } else {
                        org.springframework.web.context.request.ServletRequestAttributes attrs =
                                (org.springframework.web.context.request.ServletRequestAttributes)
                                        org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                        if (attrs != null) {
                            String authHeader = attrs.getRequest().getHeader("Authorization");
                            if (authHeader != null) {
                                headers.set("Authorization", authHeader);
                            }
                        }
                    }
                } else {
                    headers.set("Authorization", "Bearer " + config.getApiKey());
                }

                // 创建不抛异常的 RestTemplate
                RestTemplate silentRt = new RestTemplate();
                silentRt.setRequestFactory(restTemplate.getRequestFactory());
                silentRt.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
                    public boolean hasError(org.springframework.http.client.ClientHttpResponse resp) { return false; }
                    public void handleError(org.springframework.http.client.ClientHttpResponse resp) {}
                });

                // 使用 UriComponentsBuilder 构建 GET URL，自动处理编码
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
                for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                    if (entry.getValue() != null) {
                        uriBuilder.queryParam(entry.getKey(), entry.getValue().toString());
                    }
                }
                URI uri = uriBuilder.build().encode().toUri();

                // 先试 POST，失败则 GET 降级
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                log.info("技能[{}] POST请求: url={}, body={}", skill.getName(), url, objectMapper.writeValueAsString(requestBody));
                ResponseEntity<Map> responseEntity = null;
                try {
                    responseEntity = silentRt.exchange(url, HttpMethod.POST, entity, Map.class);
                } catch (Exception postEx) {
                    log.info("技能[{}] POST请求异常: {}，降级为 GET 请求: {}", skill.getName(), postEx.getMessage(), uri);
                    isGetRequest = true;
                    HttpEntity<Void> getEntity = new HttpEntity<>(null, headers);
                    responseEntity = silentRt.exchange(uri, HttpMethod.GET, getEntity, Map.class);
                }

                if (responseEntity != null && responseEntity.getStatusCode().isError()) {
                    isGetRequest = true;
                    log.info("技能[{}] POST失败(status={})，降级为 GET 请求: {}", skill.getName(), responseEntity.getStatusCode(), uri);
                    HttpEntity<Void> getEntity = new HttpEntity<>(null, headers);
                    responseEntity = silentRt.exchange(uri, HttpMethod.GET, getEntity, Map.class);
                }

                Map<String, Object> response = responseEntity != null ? responseEntity.getBody() : null;
                log.info("技能[{}] 最终响应: {}", skill.getName(), response != null ? objectMapper.writeValueAsString(response) : "null");

                // 解析响应
                if (response != null) {
                    Object resultData = extractResponseData(response);
                    log.info("[技能调用] 技能[{}] extractResponseData结果是否为null={}", skill.getName(), resultData == null);

                    if (resultData != null) {
                        String httpMethod = isGetRequest ? "GET" : "POST";
                        String displayUrl = skill.getEndpoint();
                        if (isInternalCall) {
                            if (isGetRequest) {
                                displayUrl = uri.toString().replace("http://localhost:" + serverPort, "");
                            } else if (url.contains("?")) {
                                displayUrl = displayUrl + url.substring(url.indexOf("?"));
                            }
                        }
                        skillBuilder.append("【").append(skill.getName()).append("】\n");
                        skillBuilder.append("调用路径: ").append(httpMethod).append(" ").append(displayUrl).append("\n");
                        skillBuilder.append("调用参数: ").append(objectMapper.writeValueAsString(extractedParams)).append("\n");
                        skillBuilder.append("--- 返回结果 ---\n");
                        skillBuilder.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultData)).append("\n\n");
                        executedCount++;

                        skillDetail.put("status", "success");
                        skillDetail.put("httpMethod", httpMethod);
                        skillDetail.put("actualUrl", displayUrl);
                        skillDetail.put("result", resultData);

                        if (emitter != null) {
                            sendSseEvent(emitter, "step_detail", Map.of(
                                    "stepIndex", 0,
                                    "detailType", "skill_result",
                                    "skillName", skill.getName(),
                                    "status", "success",
                                    "httpMethod", httpMethod,
                                    "url", displayUrl,
                                    "resultPreview", objectMapper.writeValueAsString(resultData).length() > 500
                                            ? objectMapper.writeValueAsString(resultData).substring(0, 500) + "..."
                                            : objectMapper.writeValueAsString(resultData)
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                failedCount++;
                log.error("[技能调用] 技能[{}] 执行异常: {}", skill.getName(), e.getMessage(), e);
                skillBuilder.append("【").append(skill.getName()).append("】执行失败: ").append(e.getMessage()).append("\n\n");

                skillDetail.put("status", "failed");
                skillDetail.put("errorMessage", e.getMessage());

                if (emitter != null) {
                    sendSseEvent(emitter, "step_detail", Map.of(
                            "stepIndex", 0,
                            "detailType", "skill_result",
                            "skillName", skill.getName(),
                            "status", "failed",
                            "errorMessage", e.getMessage()
                    ));
                }
            }

            skillDetails.add(skillDetail);
        }

        // 组装返回结果
        SkillExecuteResult result = new SkillExecuteResult();
        result.setOutput(skillBuilder.toString());
        result.setTotalSkillCount(enabledSkills.size());
        result.setExecutedCount(executedCount);
        result.setFailedCount(failedCount);
        result.setSkillNames(skillNames);
        result.setDurationMs(System.currentTimeMillis() - start);
        log.info("[技能调用] 执行完毕: totalSkillCount={}, executedCount={}, failedCount={}, skillNames={}",
                enabledSkills.size(), executedCount, failedCount, skillNames);
        return result;
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter, String type, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>(data);
            event.put("type", type);
            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(event), org.springframework.http.MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("发送 SSE 事件失败: {}", e.getMessage());
        }
    }

    /**
     * LLM 提取技能参数
     * <p>根据 inputSchema 和用户消息，调用 LLM 提取结构化参数。</p>
     */
    Map<String, Object> extractParamsByLLM(String userMessage, String inputSchema,
                                            AIProperties.ProviderConfig config) {
        if (inputSchema == null || inputSchema.isBlank()) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("keywords", userMessage);
            return fallback;
        }

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        String systemPrompt = "你是一个参数提取器。根据以下 JSON Schema 定义的参数结构，从用户消息中提取对应的参数值。\n" +
                "规则：\n" +
                "1. 只返回一个 JSON 对象，不要返回任何其他文字、解释或 markdown 标记\n" +
                "2. 如果用户消息中没有提到某个参数，不要包含该字段\n" +
                "3. 参数类型必须与 Schema 定义一致（字符串、整数等）\n" +
                "4. 如果无法从用户消息中提取任何参数，返回空对象 {}\n\n" +
                "Schema 定义：\n" + inputSchema;

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        Map<String, String> usrMsg = new HashMap<>();
        usrMsg.put("role", "user");
        usrMsg.put("content", userMessage);
        messages.add(usrMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);

            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        String content = message.get("content").trim();
                        if (content.startsWith("```")) {
                            content = content.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Object> extracted = objectMapper.readValue(content, Map.class);
                        log.info("LLM 提取参数: {}", objectMapper.writeValueAsString(extracted));
                        return extracted;
                    }
                }
            }
        } catch (Exception e) {
            // 参数提取失败，返回兜底参数
        }

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("keywords", userMessage);
        return fallback;
    }

    /**
     * LLM 智能选择技能
     * <p>根据用户消息和技能列表，调用 LLM 选出最匹配的技能。</p>
     */
    private List<AgentSkill> selectSkillsByLLM(String userMessage, List<AgentSkill> enabledSkills,
                                                AIProperties.ProviderConfig config) {
        if (enabledSkills == null || enabledSkills.isEmpty()) {
            log.info("[LLM技能选择] enabledSkills为空或null, 直接返回");
            return enabledSkills;
        }
        if (enabledSkills.size() == 1) {
            log.info("[LLM技能选择] 仅1个技能, 跳过LLM选择直接返回: skillId={}", enabledSkills.get(0).getSkillId());
            return enabledSkills;
        }
        log.info("[LLM技能选择] 开始LLM选择, 共{}个技能", enabledSkills.size());

        StringBuilder skillListDesc = new StringBuilder("[");
        for (int i = 0; i < enabledSkills.size(); i++) {
            AgentSkill as = enabledSkills.get(i);
            Skill skill = skillMapper.selectById(as.getSkillId());
            if (skill == null) {
                continue;
            }
            if (i > 0) {
                skillListDesc.append(", ");
            }
            skillListDesc.append("{\"code\":\"").append(skill.getCode())
                    .append("\",\"name\":\"").append(skill.getName())
                    .append("\",\"description\":\"")
                    .append(skill.getDescription() != null ? skill.getDescription() : "")
                    .append("\"}");
        }
        skillListDesc.append("]");

        String systemPrompt = "你是一个技能选择器。根据用户消息，从技能列表中选出最匹配的技能。\n\n" +
                "可用技能：\n" + skillListDesc + "\n\n" +
                "规则：\n" +
                "1. 只返回 JSON 数组，包含选中技能的 code，如 [\"stock_market_quote\"]\n" +
                "2. 根据用户意图选择最相关的技能，可以选多个\n" +
                "3. 如果用户意图不明确或与所有技能无关，返回空数组 []\n" +
                "4. 不要返回任何解释文字、markdown 标记或其他内容\n\n" +
                "用户消息：" + userMessage;

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0);

        try {
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);

            if (apiResponse != null) {
                log.debug("[LLM技能选择] LLM原始响应: {}", objectMapper.writeValueAsString(apiResponse));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        String content = message.get("content").trim();
                        log.info("[LLM技能选择] LLM返回内容: {}", content);
                        if (content.startsWith("```")) {
                            content = content.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
                        }
                        @SuppressWarnings("unchecked")
                        List<String> selectedCodes = objectMapper.readValue(content, List.class);
                        if (selectedCodes != null && !selectedCodes.isEmpty()) {
                            log.info("[LLM技能选择] LLM选中技能codes: {}", selectedCodes);
                            Set<String> codeSet = new HashSet<>(selectedCodes);
                            return enabledSkills.stream()
                                    .filter(as -> {
                                        Skill skill = skillMapper.selectById(as.getSkillId());
                                        return skill != null && codeSet.contains(skill.getCode());
                                    })
                                    .collect(Collectors.toList());
                        }
                        log.warn("[LLM技能选择] LLM返回空数组或解析后selectedCodes为空, 返回空列表");
                        return new ArrayList<>();
                    }
                }
                log.warn("[LLM技能选择] LLM响应中无有效content: choices={}", choices);
            }
        } catch (Exception e) {
            log.warn("[LLM技能选择] 调用异常, 降级为执行所有技能: {}", e.getMessage(), e);
        }

        log.info("[LLM技能选择] 降级返回全部{}个技能", enabledSkills.size());
        return enabledSkills;
    }

    /**
     * 从响应中提取结果数据
     * <p>适配多种返回格式：result / data / list / records</p>
     * <p>同时处理 PageHelper PageInfo 格式（含 list + total）和普通业务返回（含 code + data）</p>
     */
    @SuppressWarnings("unchecked")
    private Object extractResponseData(Map<String, Object> response) {
        // 1. 先尝试标准业务返回格式：code + data
        Object data = response.get("result");
        if (data == null) data = response.get("data");
        if (data != null) return data;

        // 2. 尝试 PageHelper PageInfo 格式：list + total
        Object list = response.get("list");
        if (list != null) {
            // PageInfo 同时有 total 字段，用 Map 包装返回完整分页信息
            Map<String, Object> pageResult = new LinkedHashMap<>();
            pageResult.put("list", list);
            Object total = response.get("total");
            pageResult.put("total", total != null ? total : 0);
            Object pageNum = response.get("pageNum");
            if (pageNum != null) pageResult.put("pageNum", pageNum);
            Object pageSize = response.get("pageSize");
            if (pageSize != null) pageResult.put("pageSize", pageSize);
            return pageResult;
        }

        // 3. 尝试 records 格式
        data = response.get("records");
        if (data != null) return data;

        // 4. 无匹配格式，返回 null
        return null;
    }

    /**
     * 展平参数：将嵌套的 query 对象展开为平铺结构
     */
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
