package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.mapper.ai.AgentKnowledgeMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.TokenUsageLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Agent 意图规划服务
 * <p>调用轻量 LLM 判断用户意图，输出需要执行的步骤列表（动态编排）。</p>
 */
@Slf4j
@Service
public class AgentPlanService {

    @Autowired
    private AgentKnowledgeMapper agentKnowledgeMapper;

    @Autowired
    private LlmSseHelper llmSseHelper;

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private TokenStatsService tokenStatsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 意图规划：调用 LLM 判断用户意图并输出执行步骤列表。
     * 返回 Map 包含 intent、steps、reason 三个字段。
     *
     * @param agent           当前 Agent
     * @param userMessage     用户消息
     * @param toolDefinitions Agent 关联的 tools 定义
     * @param userId          用户 ID
     * @param sceneId         场景 ID
     * @param agentId         Agent ID
     * @param requestId       请求 ID
     * @return 规划结果 Map
     */
    public Map<String, Object> planSteps(Agent agent, String userMessage,
                                         List<Map<String, Object>> toolDefinitions,
                                         Long userId, Long sceneId, Long agentId, String requestId) {
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            return buildDefaultPlan();
        }

        // 构建技能描述
        StringBuilder skillsDesc = new StringBuilder();
        if (toolDefinitions != null && !toolDefinitions.isEmpty()) {
            skillsDesc.append("该 Agent 可用的技能/工具：\n");
            for (Map<String, Object> tool : toolDefinitions) {
                @SuppressWarnings("unchecked")
                Map<String, Object> func = (Map<String, Object>) tool.get("function");
                if (func != null) {
                    skillsDesc.append("- ").append(func.get("name")).append(": ").append(func.get("description")).append("\n");
                }
            }
        } else {
            skillsDesc.append("该 Agent 没有配置任何技能/工具。");
        }

        boolean hasKnowledge = agentKnowledgeMapper.selectByAgentId(agentId).stream()
                .anyMatch(k -> k.getEnabled() != null && k.getEnabled() == 1);

        String planSystemPrompt = "你是一个意图规划器。根据用户消息和 Agent 的能力，判断用户意图并输出需要执行的步骤。\n\n"
                + "可选步骤及说明：\n"
                + "- prompt_assembly：提示词组装（加载 Agent 提示词 + 智能选择模板）\n"
                + "- knowledge_retrieval：知识库检索（从向量库中检索相关知识）\n"
                + "- llm_call：LLM 对话（调用大模型生成回复）\n\n"
                + skillsDesc.toString() + "\n"
                + (hasKnowledge ? "该 Agent 已关联知识库，可以检索相关知识内容。\n\n" : "该 Agent 未关联知识库。\n\n")
                + "===== 判断步骤（必须按顺序执行） =====\n\n"
                + "第一步：检查用户请求是否与上面列出的某个技能匹配。\n"
                + "匹配方法：看用户的请求是否涉及该技能能处理的数据或操作。\n"
                + "例如：用户的请求包含“分红”且 Agent 有分红查询技能 → 匹配成功 → business。\n"
                + "例如：用户的请求包含“行情/股价”且 Agent 有行情查询技能 → 匹配成功 → business。\n"
                + "如果匹配成功，返回 [\"prompt_assembly\", \"llm_call\"]。\n\n"
                + "第二步（仅当第一步未匹配时）：判断是否为闲聊。\n"
                + "不涉及任何数据查询、不涉及任何技能、不需要知识库 → chat，返回 [\"llm_call\"]。\n\n"
                + "第三步（仅当第一、二步都未匹配时）：判断是否为知识问答。\n"
                + "问题是关于概念、定义、原理的解释，不需要调用技能 → knowledge。\n"
                + "如果有知识库，返回 [\"knowledge_retrieval\", \"llm_call\"]。\n"
                + "如果无知识库，返回 [\"llm_call\"]。\n\n"
                + "输出格式：\n"
                + "返回 JSON 对象，包含 intent、steps、reason 三个字段，不要返回任何 markdown 标记。\n\n"
                + "用户消息：" + userMessage;

        List<Map<String, String>> messages = new ArrayList<>();
        System.out.println(planSystemPrompt);
        messages.add(Map.of("role", "system", "content", planSystemPrompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0);

        try {
            String url = config.getBaseUrl().replaceAll("/+", "") + "/chat/completions";
            long llmStart = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = llmSseHelper.callSync(url, config.getApiKey(), requestBody);
            long llmDuration = System.currentTimeMillis() - llmStart;

            int promptTokens = 0;
            int completionTokens = 0;
            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
                if (usage != null) {
                    promptTokens = usage.get("prompt_tokens") != null ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
                    completionTokens = usage.get("completion_tokens") != null ? ((Number) usage.get("completion_tokens")).intValue() : 0;
                }
            }
            try {
                TokenUsageLog tokenLog = new TokenUsageLog();
                tokenLog.setCallTime(new Date());
                tokenLog.setModelName(config.getChatModel());
                tokenLog.setTokensInput(promptTokens);
                tokenLog.setTokensOutput(completionTokens);
                tokenLog.setDurationMs((int) llmDuration);
                tokenLog.setStatus("success");
                tokenLog.setUserId(userId);
                tokenLog.setBizType("agent_plan");
                tokenLog.setSceneId(sceneId);
                tokenLog.setAgentId(agentId);
                tokenLog.setRequestId(requestId);
                tokenStatsService.recordCall(tokenLog);
            } catch (Exception ex) {
                log.error("[意图规划] 记录 Token 统计失败: {}", ex.getMessage(), ex);
            }

            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        String content = message.get("content").trim();
                        log.info("[意图规划] LLM 返回: {}", content);
                        if (content.startsWith("```")) {
                            content = content.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Object> plan = objectMapper.readValue(content, Map.class);
                        if (plan != null && plan.containsKey("steps")) {
                            return plan;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[意图规划] LLM 调用异常，降级为全步骤执行: {}", e.getMessage());
        }

        return buildDefaultPlan();
    }

    /**
     * 构建默认的全步骤执行计划（降级用）
     */
    public Map<String, Object> buildDefaultPlan() {
        return new LinkedHashMap<>() {{
            put("intent", "knowledge");
            put("steps", List.of("knowledge_retrieval", "llm_call"));
            put("reason", "降级为知识问答模式");
        }};
    }
}
