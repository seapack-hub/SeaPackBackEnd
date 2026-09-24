package org.seaPack.service.ai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.SceneAgentMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.SceneAgent;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.TokenUsageLog;
import org.seaPack.service.ai.LlmSseHelper;
import org.seaPack.service.ai.TokenStatsService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 意图匹配服务（Intent-First Routing）
 * <p>
 * 核心职责：分析用户意图，智能选择最优执行路径。
 * 替代原有的"编排优先"策略，实现"意图优先"路由。
 * </p>
 *
 * <h3>路由决策流程：</h3>
 * <pre>
 * 用户消息 → LLM 意图分析
 *   ├── orchestration  — 匹配到最合适的预定义编排
 *   ├── agent          — 只需单个 Agent 执行
 *   ├── dynamic        — 需要多 Agent 协作（动态编排）
 *   └── llm            — 无需 Agent，通用 LLM 对话
 * </pre>
 *
 * <h3>与原有逻辑的区别：</h3>
 * <ul>
 *   <li>原逻辑：有编排 → 强制执行（不管意图是否匹配）</li>
 *   <li>新逻辑：LLM 分析意图 → 匹配最佳路径（编排只是选项之一）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentMatchService {

    private final AIProperties aiProperties;
    private final LlmSseHelper llmSseHelper;
    private final AgentMapper agentMapper;
    private final SceneAgentMapper sceneAgentMapper;
    private final TokenStatsService tokenStatsService;
    private final ObjectMapper objectMapper;

    /** 路由结果路由类型常量 */
    public static final String ROUTE_ORCHESTRATION = "orchestration";
    public static final String ROUTE_AGENT = "agent";
    public static final String ROUTE_DYNAMIC = "dynamic_orchestration";
    public static final String ROUTE_LLM = "llm";

    /**
     * 意图匹配结果
     */
    public static class MatchResult {
        /** 路由类型：orchestration / agent / dynamic_orchestration / llm */
        public final String route;
        /** 匹配的编排（route=orchestration 时有值） */
        public final SceneOrchestration orchestration;
        /** 选择的 Agent 列表（route=agent/dynamic 时有值） */
        public final List<Agent> agents;
        /** 执行策略（route=dynamic 时有值：sequential/parallel） */
        public final String strategy;
        /** 匹配原因说明 */
        public final String reason;

        public MatchResult(String route, SceneOrchestration orchestration,
                           List<Agent> agents, String strategy, String reason) {
            this.route = route;
            this.orchestration = orchestration;
            this.agents = agents != null ? agents : Collections.emptyList();
            this.strategy = strategy;
            this.reason = reason;
        }

        /** 便捷构造：编排路由 */
        public static MatchResult orchestration(SceneOrchestration orch, String reason) {
            return new MatchResult(ROUTE_ORCHESTRATION, orch, null, null, reason);
        }

        /** 便捷构造：单 Agent 路由 */
        public static MatchResult agent(Agent agent, String reason) {
            return new MatchResult(ROUTE_AGENT, null, List.of(agent), "sequential", reason);
        }

        /** 便捷构造：动态编排路由 */
        public static MatchResult dynamic(List<Agent> agents, String strategy, String reason) {
            return new MatchResult(ROUTE_DYNAMIC, null, agents, strategy, reason);
        }

        /** 便捷构造：通用 LLM 路由 */
        public static MatchResult llm(String reason) {
            return new MatchResult(ROUTE_LLM, null, null, null, reason);
        }
    }

    /**
     * 意图匹配主入口
     *
     * @param userMessage   用户消息
     * @param sceneId       场景 ID
     * @param orchestrations 场景下所有启用的编排
     * @param explicitOrchId 前端指定的编排 ID（可为 null，表示不强制）
     * @param explicitAgentId 前端指定的 Agent ID（可为 null）
     * @param userId        用户 ID（用于 Token 统计）
     * @param requestId     请求 ID（用于 Token 统计）
     * @return 路由决策结果
     */
    public MatchResult match(String userMessage, Long sceneId,
                             List<SceneOrchestration> orchestrations,
                             Long explicitOrchId, Long explicitAgentId,
                             Long userId, String requestId) {

        // 1. 前端显式指定编排 → 直接执行（尊重用户选择）
        if (explicitOrchId != null) {
            SceneOrchestration selected = orchestrations.stream()
                    .filter(o -> o.getId().equals(explicitOrchId))
                    .findFirst().orElse(null);
            if (selected != null) {
                log.info("[意图匹配] 前端指定编排: id={}, name={}", selected.getId(), selected.getName());
                return MatchResult.orchestration(selected, "前端指定编排 [" + selected.getName() + "]");
            }
        }

        // 2. 收集候选 Agent
        List<Agent> candidates = collectCandidates(sceneId, explicitAgentId);

        // 3. 无编排 + 无 Agent → 通用 LLM
        if (orchestrations.isEmpty() && candidates.isEmpty()) {
            log.info("[意图匹配] 无编排无 Agent，路由到通用 LLM");
            return MatchResult.llm("场景无可用编排和 Agent，使用通用 LLM");
        }

        // 4. 无编排 + 有 Agent → Agent 路由（复用已有 LLM 选择逻辑，返回 agent 类型）
        if (orchestrations.isEmpty()) {
            if (candidates.size() == 1) {
                Agent only = candidates.get(0);
                log.info("[意图匹配] 无编排，唯一 Agent: {}", only.getName());
                return MatchResult.agent(only, "唯一可用 Agent [" + only.getName() + "]");
            }
            // 多个 Agent，让 LLM 选择（返回 dynamic 类型，由调用方决定后续处理）
            log.info("[意图匹配] 无编排，{} 个候选 Agent，交给 LLM 选择", candidates.size());
            return buildAgentMatch(userMessage, candidates, userId, requestId);
        }

        // 5. 有编排 → LLM 意图分析（核心：判断用户意图是否匹配某个编排）
        return matchWithLLM(userMessage, orchestrations, candidates, userId, requestId);
    }

    /**
     * LLM 意图分析：将用户消息与编排/Agent 进行语义匹配
     */
    @SuppressWarnings("unchecked")
    private MatchResult matchWithLLM(String userMessage,
                                     List<SceneOrchestration> orchestrations,
                                     List<Agent> candidates,
                                     Long userId, String requestId) {
        long startTime = System.currentTimeMillis();

        try {
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                log.error("[意图匹配] AI 配置错误：未找到提供商 [{}]", providerName);
                return fallbackToFirstOrchestration(orchestrations);
            }

            String modelName = config.getChatModel();
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            // 构建编排描述
            StringBuilder orchDesc = new StringBuilder();
            for (int i = 0; i < orchestrations.size(); i++) {
                SceneOrchestration o = orchestrations.get(i);
                orchDesc.append("- 编排 ").append(i + 1)
                        .append(" (id=").append(o.getId())
                        .append(", name=\"").append(o.getName() != null ? o.getName() : "未命名").append("\"");
                if (o.getDescription() != null && !o.getDescription().isBlank()) {
                    orchDesc.append(", desc=\"").append(o.getDescription()).append("\"");
                }
                orchDesc.append(", strategy=\"").append(o.getStrategy() != null ? o.getStrategy() : "sequential").append("\"");
                orchDesc.append(")\n");
            }

            // 构建 Agent 描述
            StringBuilder agentDesc = new StringBuilder();
            for (int i = 0; i < candidates.size(); i++) {
                Agent a = candidates.get(i);
                agentDesc.append("- Agent ").append(i + 1)
                        .append(" (id=").append(a.getId())
                        .append(", name=\"").append(a.getName() != null ? a.getName() : "未命名").append("\"");
                if (a.getDescription() != null && !a.getDescription().isBlank()) {
                    agentDesc.append(", desc=\"").append(a.getDescription()).append("\"");
                }
                agentDesc.append(")\n");
            }

            // 构建 System Prompt
            String systemPrompt = "你是一个智能路由器。根据用户消息，分析用户意图，选择最合适的执行路径。\n\n"
                    + "=== 可用编排（预定义流程）===\n"
                    + (orchDesc.length() > 0 ? orchDesc.toString() : "（无可用编排）\n")
                    + "\n=== 可用 Agent（单个助手）===\n"
                    + (agentDesc.length() > 0 ? agentDesc.toString() : "（无可用 Agent）\n")
                    + "\n=== 返回格式 ===\n"
                    + "返回 JSON 对象（不要包含 markdown 标记或其他文字）：\n"
                    + "{\n"
                    + "  \"route\": \"orchestration\" | \"agent\" | \"dynamic_orchestration\" | \"llm\",\n"
                    + "  \"orchestrationId\": <匹配的编排ID，route=orchestration时必填>,\n"
                    + "  \"agents\": [{\"id\": <agent_id>, \"name\": \"<agent_name>\", \"reason\": \"选择原因\"}],\n"
                    + "  \"strategy\": \"sequential\" | \"parallel\",\n"
                    + "  \"reason\": \"路由决策原因\"\n"
                    + "}\n\n"
                    + "=== 路由规则 ===\n"
                    + "1. orchestration：用户意图明确匹配某个编排的完整流程（如需要多个步骤串联）\n"
                    + "   - 例如：用户需要[查数据+生成报告] -> 匹配包含这两个步骤的编排\n"
                    + "   - 例如：用户只需要[查数据] -> 不要选[查数据+生成报告]的编排\n"
                    + "2. agent：用户意图只需单个 Agent 即可完成\n"
                    + "   - 例如：用户问[工商银行股价] -> 只需行情查询 Agent\n"
                    + "3. dynamic_orchestration：需要多个 Agent 协作，但没有现成编排匹配\n"
                    + "   - agents 中列出所有需要的 Agent\n"
                    + "4. llm：通用对话/知识问答，不需要 Agent\n"
                    + "   - 例如：闲聊、概念解释\n\n"
                    + "=== 关键原则 ===\n"
                    + "- 用户说[查数据]但没说[生成报告]，不要选包含报告生成的编排\n"
                    + "- 用户说[查数据并生成PDF]，才选包含两个步骤的编排\n"
                    + "- 宁可选 agent 也不要强制走不匹配的编排\n"
                    + "- 只返回 JSON，不要包含其他文字";

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1);

            Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);
            long duration = System.currentTimeMillis() - startTime;

            // Token 统计
            recordTokenUsage(apiResponse, modelName, duration, userId, requestId);

            // 解析响应
            String content = extractContent(apiResponse);
            if (content.isBlank()) {
                log.warn("[意图匹配] LLM 返回空内容，降级到第一个编排");
                return fallbackToFirstOrchestration(orchestrations);
            }

            String jsonStr = content.trim();
            if (jsonStr.contains("```")) {
                jsonStr = jsonStr.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);
            String route = (String) result.getOrDefault("route", ROUTE_LLM);
            String reason = (String) result.getOrDefault("reason", "LLM 路由决策");

            log.info("[意图匹配] LLM 路由结果: route={}, reason={}, 耗时={}ms", route, reason, duration);

            // 根据路由类型构建结果
            return switch (route) {
                case ROUTE_ORCHESTRATION -> {
                    Number orchId = (Number) result.get("orchestrationId");
                    if (orchId != null) {
                        SceneOrchestration matched = orchestrations.stream()
                                .filter(o -> o.getId().equals(orchId.longValue()))
                                .findFirst().orElse(null);
                        if (matched != null) {
                            yield MatchResult.orchestration(matched, reason);
                        }
                    }
                    // orchestrationId 未指定或不匹配，降级
                    yield fallbackToFirstOrchestration(orchestrations);
                }
                case ROUTE_AGENT -> {
                    List<Map<String, Object>> selectedAgents = (List<Map<String, Object>>) result.get("agents");
                    if (selectedAgents != null && !selectedAgents.isEmpty()) {
                        List<Agent> matched = resolveAgents(selectedAgents, candidates);
                        if (!matched.isEmpty()) {
                            yield MatchResult.agent(matched.get(0), reason);
                        }
                    }
                    yield fallbackToFirstAgent(candidates, reason);
                }
                case ROUTE_DYNAMIC -> {
                    List<Map<String, Object>> selectedAgents = (List<Map<String, Object>>) result.get("agents");
                    String strategy = (String) result.getOrDefault("strategy", "sequential");
                    if (selectedAgents != null && !selectedAgents.isEmpty()) {
                        List<Agent> matched = resolveAgents(selectedAgents, candidates);
                        if (!matched.isEmpty()) {
                            yield MatchResult.dynamic(matched, strategy, reason);
                        }
                    }
                    yield MatchResult.llm(reason);
                }
                default -> MatchResult.llm(reason);
            };

        } catch (Exception e) {
            log.error("[意图匹配] LLM 调用失败: {}", e.getMessage(), e);
            return fallbackToFirstOrchestration(orchestrations);
        }
    }

    /**
     * 无编排时的 Agent 选择（多个候选时调用 LLM 选择）
     */
    @SuppressWarnings("unchecked")
    private MatchResult buildAgentMatch(String userMessage, List<Agent> candidates,
                                        Long userId, String requestId) {
        long startTime = System.currentTimeMillis();

        try {
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                return fallbackToFirstAgent(candidates, "AI 配置错误，使用第一个 Agent");
            }

            String modelName = config.getChatModel();
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            StringBuilder agentListBuilder = new StringBuilder();
            for (int i = 0; i < candidates.size(); i++) {
                Agent a = candidates.get(i);
                agentListBuilder.append("- Agent ").append(i + 1)
                        .append(" (id=").append(a.getId())
                        .append(", name=\"").append(a.getName() != null ? a.getName() : "未命名").append("\"");
                if (a.getDescription() != null && !a.getDescription().isBlank()) {
                    agentListBuilder.append(", desc=\"").append(a.getDescription()).append("\"");
                }
                agentListBuilder.append(")\n");
            }

            String systemPrompt = "你是一个 Agent 路由器。根据用户消息，从候选 Agent 列表中选择合适的 Agent。\n\n"
                    + "候选 Agent：\n" + agentListBuilder + "\n"
                    + "返回 JSON 格式（不要包含其他文字）：\n"
                    + "{\n"
                    + "  \"agents\": [{\"id\": <agent_id>, \"name\": \"<agent_name>\", \"reason\": \"选择原因\"}],\n"
                    + "  \"strategy\": \"sequential\" 或 \"parallel\",\n"
                    + "  \"reason\": \"选择原因\"\n"
                    + "}\n\n"
                    + "规则：\n"
                    + "1. 只需一个 Agent → 只返回一个\n"
                    + "2. 需要多个 Agent 协作 → 返回多个\n"
                    + "3. 不需要任何 Agent → 返回空数组\n"
                    + "4. 只返回 JSON";

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1);

            Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);
            long duration = System.currentTimeMillis() - startTime;

            recordTokenUsage(apiResponse, modelName, duration, userId, requestId);

            String content = extractContent(apiResponse);
            if (content.isBlank()) {
                return fallbackToFirstAgent(candidates, "LLM 返回空，使用第一个 Agent");
            }

            String jsonStr = content.trim().replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);

            List<Map<String, Object>> selectedAgents = (List<Map<String, Object>>) result.get("agents");
            String strategy = (String) result.getOrDefault("strategy", "sequential");
            String reason = (String) result.getOrDefault("reason", "LLM Agent 选择");

            if (selectedAgents == null || selectedAgents.isEmpty()) {
                return MatchResult.llm("LLM 判断不需要 Agent");
            }

            List<Agent> matched = resolveAgents(selectedAgents, candidates);
            if (matched.isEmpty()) {
                return fallbackToFirstAgent(candidates, "匹配失败，使用第一个 Agent");
            }

            if (matched.size() == 1) {
                return MatchResult.agent(matched.get(0), reason);
            }
            return MatchResult.dynamic(matched, strategy, reason);

        } catch (Exception e) {
            log.error("[意图匹配] Agent 选择失败: {}", e.getMessage(), e);
            return fallbackToFirstAgent(candidates, "LLM 调用异常，使用第一个 Agent");
        }
    }

    // ========================================================================
    //  辅助方法
    // ========================================================================

    /** 收集场景候选 Agent */
    private List<Agent> collectCandidates(Long sceneId, Long explicitAgentId) {
        List<Agent> candidates = new ArrayList<>();
        if (sceneId != null) {
            List<SceneAgent> sceneAgents = sceneAgentMapper.selectBySceneId(sceneId);
            if (sceneAgents != null) {
                for (SceneAgent sa : sceneAgents) {
                    Agent agent = agentMapper.selectById(sa.getAgentId());
                    if (agent != null && agent.getStatus() != null && agent.getStatus() == 1) {
                        candidates.add(agent);
                    }
                }
            }
        }
        if (candidates.isEmpty() && explicitAgentId != null) {
            Agent agent = agentMapper.selectById(explicitAgentId);
            if (agent != null && agent.getStatus() != null && agent.getStatus() == 1) {
                candidates.add(agent);
            }
        }
        return candidates;
    }

    /** 根据 LLM 返回的 agent id 列表，从候选中解析出 Agent 对象 */
    private List<Agent> resolveAgents(List<Map<String, Object>> selectedAgents, List<Agent> candidates) {
        List<Agent> result = new ArrayList<>();
        for (Map<String, Object> item : selectedAgents) {
            Number id = (Number) item.get("id");
            if (id == null) continue;
            candidates.stream()
                    .filter(a -> a.getId().equals(id.longValue()))
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }

    /** 降级：返回第一个启用的编排 */
    private MatchResult fallbackToFirstOrchestration(List<SceneOrchestration> orchestrations) {
        SceneOrchestration first = orchestrations.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                .findFirst().orElse(orchestrations.isEmpty() ? null : orchestrations.get(0));
        if (first != null) {
            return MatchResult.orchestration(first, "LLM 路由异常，降级到第一个编排");
        }
        return MatchResult.llm("无可用编排");
    }

    /** 降级：返回第一个 Agent */
    private MatchResult fallbackToFirstAgent(List<Agent> candidates, String reason) {
        if (!candidates.isEmpty()) {
            return MatchResult.agent(candidates.get(0), reason);
        }
        return MatchResult.llm(reason);
    }

    /** 从 LLM 响应中提取 content 字段 */
    private String extractContent(Map<String, Object> apiResponse) {
        if (apiResponse == null) return "";
        List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
            if (message != null && message.get("content") != null) {
                return message.get("content");
            }
        }
        return "";
    }

    /** 记录 Token 消耗 */
    private void recordTokenUsage(Map<String, Object> apiResponse, String modelName,
                                  long duration, Long userId, String requestId) {
        try {
            int promptTokens = 0;
            int completionTokens = 0;
            if (apiResponse != null) {
                Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
                if (usage != null) {
                    promptTokens = usage.get("prompt_tokens") != null
                            ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
                    completionTokens = usage.get("completion_tokens") != null
                            ? ((Number) usage.get("completion_tokens")).intValue() : 0;
                }
            }
            TokenUsageLog tokenLog = new TokenUsageLog();
            tokenLog.setCallTime(new Date());
            tokenLog.setModelName(modelName);
            tokenLog.setTokensInput(promptTokens);
            tokenLog.setTokensOutput(completionTokens);
            tokenLog.setDurationMs((int) duration);
            tokenLog.setStatus("success");
            tokenLog.setUserId(userId);
            tokenLog.setBizType("intent_match");
            tokenLog.setRequestId(requestId);
            tokenStatsService.recordCall(tokenLog);
        } catch (Exception ex) {
            log.error("[意图匹配] 记录 Token 统计失败: {}", ex.getMessage(), ex);
        }
    }
}
