package org.seaPack.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AgentChatRequest;
import org.seaPack.dto.ai.AgentChatResponse;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.AgentPromptMapper;
import org.seaPack.mapper.ai.PromptTemplateMapper;
import org.seaPack.mapper.ai.SceneAgentConfigMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.AgentPrompt;
import org.seaPack.model.ai.PromptTemplate;
import org.seaPack.model.ai.SceneAgentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentChatService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentPromptMapper agentPromptMapper;

    @Autowired
    private PromptTemplateMapper promptTemplateMapper;

    @Autowired
    private SceneAgentConfigMapper sceneAgentConfigMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    /**
     * 执行 Agent 对话
     * <p>核心流程：加载 Agent → 解析场景级配置覆盖 → 组装系统提示词 → 构建消息列表 → 调用 LLM。</p>
     *
     * @param request 对话请求（含 Agent ID、用户消息、可选历史、可选场景 ID）
     * @return 对话响应（含回复内容、Token 统计、耗时）
     */
    public AgentChatResponse chat(AgentChatRequest request) {
        // 1. 加载 Agent 并校验状态
        Agent agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new RuntimeException("Agent 不存在: " + request.getAgentId());
        }
        if (agent.getStatus() == null || agent.getStatus() != 1) {
            throw new RuntimeException("Agent 已禁用: " + agent.getName());
        }

        // 2. 解析场景级配置覆盖
        SceneAgentConfig sceneConfig = null;
        if (request.getSceneId() != null) {
            sceneConfig = sceneAgentConfigMapper.selectBySceneAndAgent(request.getSceneId(), request.getAgentId());
            if (sceneConfig != null) {
                log.info("Agent[{}] 应用场景级配置(场景={})", agent.getName(), request.getSceneId());
            }
        }

        // 3. 组装系统提示词（含场景级追加内容）
        String systemPrompt = buildSystemPrompt(agent, sceneConfig);

        // 4. 构建消息列表
        List<Map<String, String>> messages = buildMessages(agent, systemPrompt, request.getMessage(), request.getHistory());

        // 5. 获取 AI 提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 6. 构建 LLM API 请求
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        String modelName = resolveModel(agent, sceneConfig, config);
        Double temperature = resolveTemperature(agent, sceneConfig);
        Integer maxTokens = resolveMaxTokens(agent, sceneConfig);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        if (temperature != null) {
            requestBody.put("temperature", temperature);
        }
        if (maxTokens != null) {
            requestBody.put("max_tokens", maxTokens);
        }

        // 7. 发送请求
        long startTime = System.currentTimeMillis();
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);
            long durationMs = System.currentTimeMillis() - startTime;

            // 8. 解析响应
            String content = "";
            Integer promptTokens = 0;
            Integer completionTokens = 0;

            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        content = message.get("content");
                    }
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
                if (usage != null) {
                    promptTokens = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                    completionTokens = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
                }
            }

            // 9. 增加使用次数
            agentMapper.incrementUseCount(agent.getId());

            // 10. 组装响应
            AgentChatResponse response = new AgentChatResponse();
            response.setContent(content);
            response.setTokensPrompt(promptTokens);
            response.setTokensCompletion(completionTokens);
            response.setDurationMs((int) durationMs);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Agent 对话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 组装系统提示词
     * <p>Agent 基础 system_prompt + 已启用的模板内容 + 场景级 System Prompt 追加内容。</p>
     *
     * @param agent       Agent 对象
     * @param sceneConfig 场景级配置（可选，用于追加 systemPrompt）
     * @return 组装完成的系统提示词
     */
    private String buildSystemPrompt(Agent agent, SceneAgentConfig sceneConfig) {
        StringBuilder systemPromptBuilder = new StringBuilder();
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            systemPromptBuilder.append(agent.getSystemPrompt());
        }

        List<AgentPrompt> enabledPrompts = agentPromptMapper.selectByAgentId(agent.getId()).stream()
                .filter(p -> p.getEnabled() != null && p.getEnabled() == 1)
                .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
                .collect(Collectors.toList());

        List<Long> templateIds = enabledPrompts.stream()
                .map(AgentPrompt::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!templateIds.isEmpty()) {
            Map<Long, PromptTemplate> templateMap = promptTemplateMapper.selectByIds(templateIds).stream()
                    .collect(Collectors.toMap(PromptTemplate::getId, t -> t, (a, b) -> a));
            for (AgentPrompt ap : enabledPrompts) {
                PromptTemplate template = templateMap.get(ap.getTemplateId());
                if (template != null && template.getContent() != null && !template.getContent().isBlank()) {
                    systemPromptBuilder.append("\n\n").append(template.getContent());
                }
            }
        }

        // 追加场景级 System Prompt
        if (sceneConfig != null && sceneConfig.getSystemPrompt() != null && !sceneConfig.getSystemPrompt().isBlank()) {
            systemPromptBuilder.append("\n\n").append(sceneConfig.getSystemPrompt());
        }

        String systemPrompt = systemPromptBuilder.toString();
        if (systemPrompt.isBlank()) {
            throw new RuntimeException("Agent 系统提示词为空: " + agent.getName());
        }
        return systemPrompt;
    }

    private List<Map<String, String>> buildMessages(Agent agent, String systemPrompt,
                                                     String userMessage, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        if (agent.getMemoryEnabled() != null && agent.getMemoryEnabled() == 1
                && history != null && !history.isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> trimmedHistory = new ArrayList<>(history);
            if (trimmedHistory.size() > window * 2) {
                trimmedHistory = trimmedHistory.subList(trimmedHistory.size() - window * 2, trimmedHistory.size());
            }
            messages.addAll(trimmedHistory);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        return messages;
    }

    /**
     * 解析模型
     * <p>优先级：场景级配置 > Agent 默认模型 > AI 提供商默认模型。</p>
     */
    private String resolveModel(Agent agent, SceneAgentConfig sceneConfig, AIProperties.ProviderConfig config) {
        if (sceneConfig != null && sceneConfig.getModel() != null && !sceneConfig.getModel().isBlank()) {
            return sceneConfig.getModel();
        }
        return agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel();
    }

    /**
     * 解析温度参数
     * <p>优先级：场景级配置 > Agent 默认温度。</p>
     */
    private Double resolveTemperature(Agent agent, SceneAgentConfig sceneConfig) {
        if (sceneConfig != null && sceneConfig.getTemperature() != null) {
            return sceneConfig.getTemperature().doubleValue();
        }
        return agent.getTemperature() != null ? agent.getTemperature().doubleValue() : null;
    }

    /**
     * 解析最大 Token 数
     * <p>优先级：场景级配置 > Agent 默认 MaxTokens。</p>
     */
    private Integer resolveMaxTokens(Agent agent, SceneAgentConfig sceneConfig) {
        if (sceneConfig != null && sceneConfig.getMaxTokens() != null) {
            return sceneConfig.getMaxTokens();
        }
        return agent.getMaxTokens();
    }
}
