package org.seaPack.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AgentChatRequest;
import org.seaPack.dto.ai.AgentChatResponse;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.AgentPromptMapper;
import org.seaPack.mapper.ai.PromptTemplateMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.AgentPrompt;
import org.seaPack.model.ai.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 正式对话服务
 * <p>提供 Agent 的正式对话功能（非测试），组装 prompt → 调用 LLM → 返回结果。</p>
 */
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
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    /**
     * 执行 Agent 对话
     * <p>核心流程：加载 Agent → 组装系统提示词 → 构建消息列表（含可选历史记忆） → 调用 LLM。</p>
     *
     * @param request 对话请求（含 Agent ID、用户消息、可选历史）
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

        // 2. 组装系统提示词
        String systemPrompt = buildSystemPrompt(agent);

        // 3. 构建消息列表
        List<Map<String, String>> messages = buildMessages(agent, systemPrompt, request.getMessage(), request.getHistory());

        // 4. 获取 AI 提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 5. 构建 LLM API 请求
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        if (agent.getTemperature() != null) {
            requestBody.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            requestBody.put("max_tokens", agent.getMaxTokens());
        }

        // 6. 发送请求
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

            // 7. 解析响应
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

            // 8. 增加使用次数
            agentMapper.incrementUseCount(agent.getId());

            // 9. 组装响应
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
     * 组装系统提示词：Agent 基础 system_prompt + 已启用的模板内容
     */
    private String buildSystemPrompt(Agent agent) {
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

        String systemPrompt = systemPromptBuilder.toString();
        if (systemPrompt.isBlank()) {
            throw new RuntimeException("Agent 系统提示词为空: " + agent.getName());
        }
        return systemPrompt;
    }

    /**
     * 构建消息列表（含系统提示词、历史记忆、用户消息）
     */
    private List<Map<String, String>> buildMessages(Agent agent, String systemPrompt,
                                                     String userMessage, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 添加历史消息（如果启用记忆）
        if (agent.getMemoryEnabled() != null && agent.getMemoryEnabled() == 1
                && history != null && !history.isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> trimmedHistory = new ArrayList<>(history);
            // 按 memory_window 裁剪历史，保留最近的 window*2 条消息（用户+助手交替）
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
}
