package org.seaPack.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.*;
import org.seaPack.dto.ai.SkillExecuteResult;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 测试对话服务
 * <p>负责测试对话的完整链路编排：提示词组装 → 知识库检索 → 技能调用 → LLM 调用，
 * 并记录每步的链路追踪信息。</p>
 */
@Slf4j
@Service
public class AgentTestChatService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentPromptMapper agentPromptMapper;

    @Autowired
    private AgentKnowledgeMapper agentKnowledgeMapper;

    @Autowired
    private PromptTemplateMapper promptTemplateMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private AgentSkillExecutor skillExecutor;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行测试对话（含完整链路追踪）
     * <p>核心流程：加载 Agent → 提示词组装 → 知识库检索 → 技能调用 → LLM 调用 → 保存测试会话。</p>
     *
     * @param request 测试对话请求
     * @param userId  当前用户 ID
     * @return 测试对话响应（含链路追踪快照）
     */
    @Transactional
    public AgentTestChatResponse testChat(AgentTestChatRequest request, Long userId) {
        long totalStart = System.currentTimeMillis();
        List<AgentTraceStep> steps = new ArrayList<>();
        int stepIndex = 1;

        // 1. 加载 Agent 并校验状态
        Agent agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new RuntimeException("Agent 不存在: " + request.getAgentId());
        }
        if (agent.getStatus() == null || agent.getStatus() != 1) {
            throw new RuntimeException("Agent 已禁用: " + agent.getName());
        }

        // ===== Step 1: 提示词组装 =====
        String systemPrompt;
        try {
            AgentTraceStepResult stepResult = assemblePrompt(agent, stepIndex);
            systemPrompt = stepResult.output;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "prompt_assembly", "提示词组装", e.getMessage()));
            return buildErrorResponse(agent, request, steps, totalStart, userId, e);
        }

        // ===== Step 2: 知识库检索 =====
        String knowledgeContext = "";
        try {
            AgentTraceStepResult stepResult = retrieveKnowledge(agent, request.getMessage(), stepIndex);
            knowledgeContext = stepResult.output;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);

            if (knowledgeContext != null && !knowledgeContext.isBlank()) {
                systemPrompt += "\n\n【参考知识】\n" + knowledgeContext;
            }
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "knowledge_retrieval", "知识库检索", e.getMessage()));
        }

        // ===== Step 3: 技能调用 =====
        String skillContext = "";
        try {
            SkillExecuteResult skillResult = skillExecutor.executeSkills(agent.getId(), request.getMessage());
            skillContext = skillResult.getOutput();

            AgentTraceStep step = new AgentTraceStep();
            step.setStepIndex(stepIndex++);
            step.setStepType("skill_execution");
            step.setStepName("技能调用");
            step.setStatus(skillResult.getExecutedCount() > 0 ? "success" : "skip");
            step.setDurationMs(skillResult.getDurationMs());
            step.setOutput(skillContext);

            // 填充 metadata
            Map<String, Object> skillMeta = new HashMap<>();
            skillMeta.put("totalSkillCount", skillResult.getTotalSkillCount());
            skillMeta.put("executedCount", skillResult.getExecutedCount());
            skillMeta.put("failedCount", skillResult.getFailedCount());
            skillMeta.put("skillNames", skillResult.getSkillNames());
            step.setMetadata(skillMeta);

            steps.add(step);

            if (skillContext != null && !skillContext.isBlank()) {
                systemPrompt += "\n\n【技能执行结果】\n" + skillContext;
            }
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "skill_execution", "技能调用", e.getMessage()));
        }

        // ===== Step 4: LLM 调用 =====
        String replyContent;
        int promptTokens;
        int completionTokens;
        String modelName;
        try {
            AgentTraceStepResult stepResult = callLLM(agent, systemPrompt, request, stepIndex);
            replyContent = stepResult.output;
            promptTokens = stepResult.tokensPrompt;
            completionTokens = stepResult.tokensCompletion;
            modelName = stepResult.modelName;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "llm_call", "LLM 调用", e.getMessage()));
            return buildErrorResponse(agent, request, steps, totalStart, userId, e);
        }

        // ===== 组装链路追踪快照 =====
        long totalDuration = System.currentTimeMillis() - totalStart;
        AgentTraceSnapshot snapshot = buildTraceSnapshot(steps, totalDuration, promptTokens, completionTokens);

        // ===== 保存测试会话 =====
        saveTestSession(agent, request, replyContent, snapshot, (int) totalDuration,
                promptTokens, completionTokens, modelName, "success", null, userId);

        agentMapper.incrementUseCount(agent.getId());

        AgentTestChatResponse response = new AgentTestChatResponse();
        response.setContent(replyContent);
        response.setTokensPrompt(promptTokens);
        response.setTokensCompletion(completionTokens);
        response.setDurationMs((int) totalDuration);
        response.setTraceSnapshot(snapshot);
        return response;
    }

    // ===== Step 1: 提示词组装 =====

    /**
     * 组装系统提示词
     * <p>将 Agent 基础提示词与关联的提示词模板按顺序拼接。</p>
     */
    private AgentTraceStepResult assemblePrompt(Agent agent, int stepIndex) {
        long stepStart = System.currentTimeMillis();
        StringBuilder systemPromptBuilder = new StringBuilder();

        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            systemPromptBuilder.append(agent.getSystemPrompt());
        }

        List<AgentPrompt> enabledPrompts = agentPromptMapper.selectByAgentId(agent.getId()).stream()
                .filter(p -> p.getEnabled() != null && p.getEnabled() == 1)
                .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
                .collect(Collectors.toList());

        // 批量查询模板，避免 N+1 查询
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

        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("prompt_assembly");
        step.setStepName("提示词组装");
        step.setStatus("success");
        step.setDurationMs(System.currentTimeMillis() - stepStart);
        step.setInput(agent.getSystemPrompt());
        step.setOutput(systemPrompt);
        Map<String, Object> meta = new HashMap<>();
        meta.put("templateCount", enabledPrompts.size());
        step.setMetadata(meta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = systemPrompt;
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    // ===== Step 2: 知识库检索 =====

    /**
     * 从 Agent 关联的知识库中检索相关内容
     */
    private AgentTraceStepResult retrieveKnowledge(Agent agent, String query, int stepIndex) {
        long stepStart = System.currentTimeMillis();
        StringBuilder knowledgeBuilder = new StringBuilder();
        int totalChunks = 0;

        List<AgentKnowledge> enabledKnowledge = agentKnowledgeMapper.selectByAgentId(agent.getId()).stream()
                .filter(k -> k.getEnabled() != null && k.getEnabled() == 1)
                .sorted(Comparator.comparingInt(k -> k.getSortOrder() != null ? k.getSortOrder() : 0))
                .collect(Collectors.toList());

        for (AgentKnowledge ak : enabledKnowledge) {
            int topK = ak.getRetrievalCount() != null ? ak.getRetrievalCount() : 3;
            List<RetrievalResult> results = knowledgeBaseService.retrieve(ak.getKnowledgeId(), query, topK);

            if (!results.isEmpty()) {
                knowledgeBuilder.append("【").append(ak.getKnowledgeName() != null ? ak.getKnowledgeName() : "知识库").append("】\n");
                for (RetrievalResult r : results) {
                    knowledgeBuilder.append("- ").append(r.getContent()).append("\n");
                    totalChunks++;
                }
                knowledgeBuilder.append("\n");
            }
        }

        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("knowledge_retrieval");
        step.setStepName("知识库检索");
        step.setStatus(totalChunks > 0 ? "success" : "skip");
        step.setDurationMs(System.currentTimeMillis() - stepStart);
        step.setOutput(knowledgeBuilder.toString());
        Map<String, Object> meta = new HashMap<>();
        meta.put("knowledgeCount", enabledKnowledge.size());
        meta.put("chunkCount", totalChunks);
        step.setMetadata(meta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = knowledgeBuilder.toString();
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    // ===== Step 4: LLM 调用 =====

    /**
     * 调用 LLM API
     * <p>构建消息列表并调用 LLM，返回响应内容和 Token 统计。</p>
     */
    private AgentTraceStepResult callLLM(Agent agent, String systemPrompt,
                                          AgentTestChatRequest request, int stepIndex) {
        long llmStart = System.currentTimeMillis();
        String modelName = agent.getModelCode() != null ? agent.getModelCode() :
                aiProperties.getProviders().get(aiProperties.getActiveProvider()).getChatModel();

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 添加历史消息（如果启用记忆）
        if (agent.getMemoryEnabled() != null && agent.getMemoryEnabled() == 1
                && request.getHistory() != null && !request.getHistory().isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> history = new ArrayList<>(request.getHistory());
            if (history.size() > window * 2) {
                history = history.subList(history.size() - window * 2, history.size());
            }
            messages.addAll(history);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);

        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        if (agent.getTemperature() != null) {
            requestBody.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            requestBody.put("max_tokens", agent.getMaxTokens());
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);

        String replyContent = "";
        int promptTokens = 0;
        int completionTokens = 0;

        if (apiResponse != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.get("content") != null) {
                    replyContent = message.get("content");
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
            if (usage != null) {
                promptTokens = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                completionTokens = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
            }
        }

        long llmDuration = System.currentTimeMillis() - llmStart;
        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("llm_call");
        step.setStepName("LLM 调用");
        step.setStatus("success");
        step.setDurationMs(llmDuration);
        step.setInput(systemPrompt);
        step.setOutput(replyContent);
        Map<String, Object> llmMeta = new HashMap<>();
        llmMeta.put("model", modelName);
        llmMeta.put("tokensPrompt", promptTokens);
        llmMeta.put("tokensCompletion", completionTokens);
        llmMeta.put("temperature", agent.getTemperature());
        step.setMetadata(llmMeta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = replyContent;
        result.tokensPrompt = promptTokens;
        result.tokensCompletion = completionTokens;
        result.modelName = modelName;
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    // ===== 辅助方法 =====

    private AgentTraceStep buildFailStep(int stepIndex, String stepType, String stepName, String errorMessage) {
        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType(stepType);
        step.setStepName(stepName);
        step.setStatus("fail");
        step.setDurationMs(0L);
        step.setOutput(errorMessage);
        step.setMetadata(new HashMap<>());
        return step;
    }

    private AgentTraceSnapshot buildTraceSnapshot(List<AgentTraceStep> steps, long totalDuration,
                                                   int promptTokens, int completionTokens) {
        AgentTraceSnapshot snapshot = new AgentTraceSnapshot();
        snapshot.setSteps(steps);
        snapshot.setTotalDurationMs(totalDuration);
        AgentTraceSnapshot.TotalTokens tokens = new AgentTraceSnapshot.TotalTokens();
        tokens.setPrompt(promptTokens);
        tokens.setCompletion(completionTokens);
        snapshot.setTotalTokens(tokens);
        return snapshot;
    }

    private AgentTestChatResponse buildErrorResponse(Agent agent, AgentTestChatRequest request,
                                                     List<AgentTraceStep> steps, long totalStart,
                                                     Long userId, Exception e) {
        long totalDuration = System.currentTimeMillis() - totalStart;
        AgentTraceSnapshot snapshot = new AgentTraceSnapshot();
        snapshot.setSteps(steps);
        snapshot.setTotalDurationMs(totalDuration);
        AgentTraceSnapshot.TotalTokens tokens = new AgentTraceSnapshot.TotalTokens();
        tokens.setPrompt(0);
        tokens.setCompletion(0);
        snapshot.setTotalTokens(tokens);

        saveTestSession(agent, request, null, snapshot, (int) totalDuration,
                0, 0, null, "fail", e.getMessage(), userId);

        AgentTestChatResponse response = new AgentTestChatResponse();
        response.setContent("");
        response.setTokensPrompt(0);
        response.setTokensCompletion(0);
        response.setDurationMs((int) totalDuration);
        response.setTraceSnapshot(snapshot);
        return response;
    }

    private void saveTestSession(Agent agent, AgentTestChatRequest request, String reply,
                                 AgentTraceSnapshot snapshot, int durationMs,
                                 int promptTokens, int completionTokens, String modelName,
                                 String status, String errorMessage, Long userId) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("agent");
        session.setBizId(agent.getId());
        session.setBizName(agent.getName());
        session.setUserMessage(request.getMessage());
        session.setOutputResult(reply);
        try {
            session.setTraceSnapshot(objectMapper.writeValueAsString(snapshot));
        } catch (JsonProcessingException ex) {
            session.setTraceSnapshot("{}");
        }
        session.setTotalDurationMs(durationMs);
        session.setTokensPrompt(promptTokens);
        session.setTokensCompletion(completionTokens);
        session.setTokensTotal(promptTokens + completionTokens);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    /** Step 结果内部类 */
    static class AgentTraceStepResult {
        AgentTraceStep step;
        String output;
        int nextStepIndex;
        int tokensPrompt;
        int tokensCompletion;
        String modelName;
    }
}
