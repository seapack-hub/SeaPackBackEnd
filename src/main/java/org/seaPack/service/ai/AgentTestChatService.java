package org.seaPack.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.*;
import org.seaPack.dto.ai.SkillExecuteResult;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Agent 测试对话服务（编排层）
 * <p>负责测试对话的完整链路编排，将具体职责委托给各个子服务：</p>
 * <ul>
 *   <li>{@link AgentPlanService} - 意图规划（LLM 动态编排）</li>
 *   <li>{@link AgentPromptService} - 提示词组装（加载模板 + 安全约束）</li>
 *   <li>{@link KnowledgeBaseService} - 知识库检索</li>
 *   <li>{@link AgentLlmCaller} - LLM 流式调用（含 Function Calling）</li>
 *   <li>{@link AgentTraceHelper} - 链路追踪构建</li>
 * </ul>
 */
@Slf4j
@Service
public class AgentTestChatService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentKnowledgeMapper agentKnowledgeMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private AgentSkillExecutor skillExecutor;

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    @Autowired
    private SceneAgentConfigMapper sceneAgentConfigMapper;

    @Autowired
    private AgentPlanService agentPlanService;

    @Autowired
    private AgentPromptService agentPromptService;

    @Autowired
    private AgentLlmCaller agentLlmCaller;

    @Autowired
    private AgentTraceHelper agentTraceHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =====================================================================
    //  主流程编排
    // =====================================================================

    /**
     * 执行测试对话（SSE 流式返回）
     */
    public void testChatStream(AiDialogRequest request, Long userId, SseEmitter emitter, String authToken, HttpServletResponse response) {
        testChatStream(request, userId, emitter, authToken, response, null);
    }

    /**
     * 执行测试对话（SSE 流式返回，支持取消标志）
     * <p>核心流程：意图规划 → [提示词组装] → [知识库检索] → LLM 流式调用 → 保存测试会话。</p>
     */
    public void testChatStream(AiDialogRequest request, Long userId, SseEmitter emitter,
                               String authToken, HttpServletResponse response, AtomicBoolean cancelFlag) {
        long totalStart = System.currentTimeMillis();

        try {
            // 0. 校验请求参数
            if (request.getAgentId() == null) {
                throw new IllegalArgumentException("agentId 不能为空");
            }

            // 1. 加载 Agent 并应用场景级配置
            Agent agent = agentMapper.selectById(request.getAgentId());
            if (agent == null) {
                throw new IllegalArgumentException("Agent 不存在: agentId=" + request.getAgentId());
            }
            applySceneConfig(agent, request.getSceneId());

            List<AgentTraceStep> steps = new ArrayList<>();
            long totalDuration;

            // 2. 加载 Agent 关联的技能定义
            List<Map<String, Object>> toolDefinitions = new ArrayList<>();
            try {
                toolDefinitions = skillExecutor.getToolDefinitions(request.getAgentId());
                log.info("Agent[{}] 加载技能定义成功: count={}", agent.getName(), toolDefinitions.size());
            } catch (Exception e) {
                log.error("Agent[{}] 加载技能定义失败: {}", agent.getName(), e.getMessage(), e);
            }

            // 3. 意图规划（本地规则优先 + LLM 兜底）
            String userMsg = extractMessage(request);
            log.info("Agent[{}] 开始意图规划: message={}", agent.getName(),
                    request.getQuestion() != null ? request.getQuestion().substring(0, Math.min(50, request.getQuestion().length())) : "N/A");

            // 推送：规划开始
            if (emitter != null) {
                SseEvent.send(emitter, "step_start", Map.of(
                        "stepIndex", 0,
                        "stepType", "plan",
                        "stepName", "意图规划",
                        "message", "正在分析用户意图..."
                ));
                SseEvent.send(emitter, "step_progress", Map.of(
                        "stepIndex", 0,
                        "stepType", "plan",
                        "message", "用户消息: " + (userMsg.length() > 80 ? userMsg.substring(0, 80) + "..." : userMsg)
                ));
            }

            // 3.1 本地规则快速判断闲聊（省掉一次 LLM 调用）
            Map<String, Object> plan = detectQuickIntent(userMsg, toolDefinitions);
            String planStrategy;
            if (plan != null) {
                planStrategy = "本地规则匹配";
                if (emitter != null) {
                    SseEvent.send(emitter, "step_progress", Map.of(
                            "stepIndex", 0,
                            "stepType", "plan",
                            "message", "策略: 本地规则快速判断"
                    ));
                    SseEvent.send(emitter, "step_progress", Map.of(
                            "stepIndex", 0,
                            "stepType", "plan",
                            "message", "判定结果: " + plan.get("reason") + " → 跳过 LLM 规划"
                    ));
                }
            } else {
                planStrategy = "LLM 智能分析";
                if (emitter != null) {
                    SseEvent.send(emitter, "step_progress", Map.of(
                            "stepIndex", 0,
                            "stepType", "plan",
                            "message", "策略: 本地规则未命中，调用 LLM 进行意图分析..."
                    ));
                }
                // 未命中本地规则，走 LLM 规划
                plan = agentPlanService.planSteps(agent, userMsg, toolDefinitions,
                        userId, request.getSceneId(), agent.getId(), request.getRequestId());
                if (emitter != null) {
                    SseEvent.send(emitter, "step_progress", Map.of(
                            "stepIndex", 0,
                            "stepType", "plan",
                            "message", "LLM 分析完成，分类结果: " + plan.get("intent")
                    ));
                }
            }

            @SuppressWarnings("unchecked")
            List<String> plannedSteps = (List<String>) plan.getOrDefault("steps", List.of("prompt_assembly", "knowledge_retrieval", "llm_call"));
            String intent = (String) plan.getOrDefault("intent", "business");
            log.info("Agent[{}] 意图规划结果: intent={}, steps={}", agent.getName(), intent, plannedSteps);

            // 推送：规划结论
            if (emitter != null) {
                String stepLabels = plannedSteps.stream()
                        .map(s -> switch (s) {
                            case "prompt_assembly" -> "提示词组装";
                            case "knowledge_retrieval" -> "知识库检索";
                            case "llm_call" -> "LLM 对话";
                            default -> s;
                        })
                        .reduce((a, b) -> a + " → " + b)
                        .orElse("");
                SseEvent.send(emitter, "step_progress", Map.of(
                        "stepIndex", 0,
                        "stepType", "plan",
                        "message", "执行计划: " + stepLabels + "（共 " + plannedSteps.size() + " 步）",
                        "intent", intent,
                        "reason", plan.get("reason") != null ? plan.get("reason") : "",
                        "plannedSteps", plannedSteps,
                        "strategy", planStrategy
                ));
            }

            // 记录 plan step
            AgentTraceStep planStep = new AgentTraceStep();
            planStep.setStepIndex(0);
            planStep.setStepType("plan");
            planStep.setStepName("意图规划");
            planStep.setStatus("success");
            Map<String, Object> planMeta = new LinkedHashMap<>();
            planMeta.put("intent", intent);
            planMeta.put("strategy", planStrategy);
            planMeta.put("reason", plan.get("reason"));
            planMeta.put("plannedSteps", plannedSteps);
            planStep.setMetadata(planMeta);
            steps.add(planStep);

            if (emitter != null) {
                SseEvent.send(emitter, "step_done", Map.of(
                        "stepIndex", 0,
                        "status", "success"
                ));
            }

            // 4. 条件执行：根据 plan 动态执行各步骤
            String systemPrompt = null;
            String knowledgeContext = null;
            int stepIndex = 1;

            // Step 1: 提示词组装（条件执行）
            if (plannedSteps.contains("prompt_assembly")) {
                if (emitter != null) {
                    SseEvent.send(emitter, "step_start", Map.of(
                            "stepIndex", stepIndex,
                            "stepType", "prompt_assembly",
                            "stepName", "提示词组装"
                    ));
                }
                AgentTraceStepResult promptResult = agentPromptService.assemblePrompt(agent, stepIndex,
                        extractMessage(request), emitter, userId, request.getSceneId(), agent.getId(), request.getRequestId());
                systemPrompt = promptResult.output;
                steps.add(promptResult.step);
                if (emitter != null) {
                    SseEvent.send(emitter, "step_done", Map.of(
                            "stepIndex", stepIndex,
                            "status", "success",
                            "durationMs", promptResult.step.getDurationMs()
                    ));
                }
                stepIndex++;
            } else {
                // 跳过提示词组装时，使用基础提示词
                systemPrompt = agent.getSystemPrompt() != null ? agent.getSystemPrompt() : "";
            }

            // Step 2: 知识库检索（条件执行）
            // chat intent 强制跳过知识库检索
            boolean shouldRetrieveKnowledge = plannedSteps.contains("knowledge_retrieval") && !"chat".equals(intent);
            if (shouldRetrieveKnowledge) {
                if (emitter != null) {
                    SseEvent.send(emitter, "step_start", Map.of(
                            "stepIndex", stepIndex,
                            "stepType", "knowledge_retrieval",
                            "stepName", "知识库检索"
                    ));
                }
                AgentTraceStepResult kbResult = knowledgeBaseService.retrieveKnowledge(agent, extractMessage(request), stepIndex, emitter);
                knowledgeContext = kbResult.output;
                steps.add(kbResult.step);
                if (emitter != null) {
                    SseEvent.send(emitter, "step_done", Map.of(
                            "stepIndex", stepIndex,
                            "status", kbResult.step.getStatus(),
                            "durationMs", kbResult.step.getDurationMs()
                    ));
                }
                stepIndex++;
            }

            // Step 3: LLM 调用（核心步骤，始终执行）
            // 只有计划明确包含技能执行时才传工具定义，其他意图（chat/knowledge）不传工具防止误调用
            boolean needsTools = plannedSteps.contains("skill_execution")
                    || ("business".equals(intent) && !plannedSteps.contains("knowledge_retrieval"));
            List<Map<String, Object>> llmTools = needsTools ? toolDefinitions : List.of();
            if (emitter != null) {
                SseEvent.send(emitter, "step_start", Map.of(
                        "stepIndex", stepIndex,
                        "stepType", "llm_call",
                        "stepName", "LLM 调用"
                ));
            }
            AgentTraceStepResult llmResult = agentLlmCaller.callLLMStreamWithTools(agent, systemPrompt,
                    knowledgeContext, request, stepIndex, emitter,
                    new AtomicBoolean(false), cancelFlag, llmTools, authToken);
            steps.add(llmResult.step);

            if (emitter != null) {
                SseEvent.send(emitter, "step_done", Map.of(
                        "stepIndex", stepIndex,
                        "status", "success",
                        "durationMs", llmResult.step.getDurationMs(),
                        "tokensPrompt", llmResult.tokensPrompt,
                        "tokensCompletion", llmResult.tokensCompletion
                ));
            }

            // 5. 安全检测：输出端防泄漏
            if (agentLlmCaller.containsSystemPromptLeakage(llmResult.output, systemPrompt)) {
                log.warn("Agent[{}] 检测到可能的系统提示词泄漏，已拦截", agent.getName());
                llmResult.output = "抱歉，我无法提供相关回答。请换个问题试试。";
            }

            // 6. 构建最终响应
            totalDuration = System.currentTimeMillis() - totalStart;
            AgentTraceSnapshot snapshot = agentTraceHelper.buildTraceSnapshot(agent, steps, totalDuration,
                    llmResult.tokensPrompt, llmResult.tokensCompletion);

            AgentTestChatResponse chatResponse = new AgentTestChatResponse();
            chatResponse.setContent(llmResult.output);
            chatResponse.setTokensPrompt(llmResult.tokensPrompt);
            chatResponse.setTokensCompletion(llmResult.tokensCompletion);
            chatResponse.setDurationMs((int) totalDuration);
            chatResponse.setTraceSnapshot(snapshot);

            if (emitter != null) {
                SseEvent.send(emitter, SseEvent.TYPE_DONE, SseEvent.done(Map.of(
                                        "content", llmResult.output,
                                        "durationMs", totalDuration,
                                        "tokensPrompt", llmResult.tokensPrompt,
                                        "tokensCompletion", llmResult.tokensCompletion,
                                        "traceSnapshot", snapshot
                                )));
                emitter.complete();
            }

            // 7. 保存测试会话
            saveTestSession(agent, request, llmResult.output, snapshot, (int) totalDuration,
                    llmResult.tokensPrompt, llmResult.tokensCompletion, llmResult.modelName,
                    "success", null, userId);

            log.info("Agent[{}] 测试对话完成: duration={}ms, output长度={}", agent.getName(), totalDuration, llmResult.output.length());

        } catch (Exception e) {
            log.error("Agent 测试对话异常: {}", e.getMessage(), e);

            long totalDuration = System.currentTimeMillis() - totalStart;

            // 尝试获取 agent（可能在早期阶段失败）
            Agent agent = null;
            try {
                if (request.getAgentId() != null) {
                    agent = agentMapper.selectById(request.getAgentId());
                }
            } catch (Exception ex) {
                // ignore
            }

            List<AgentTraceStep> errorSteps = new ArrayList<>();
            errorSteps.add(agentTraceHelper.buildFailStep(0, "system", "系统", e.getMessage()));

            AgentTraceSnapshot errorSnapshot = agentTraceHelper.buildTraceSnapshot(agent, errorSteps, totalDuration, 0, 0);
            saveTestSession(agent, request, null, errorSnapshot, (int) totalDuration,
                    0, 0, null, "fail", e.getMessage(), userId);

            if (emitter != null) {
                SseEvent.send(emitter, SseEvent.TYPE_DONE, SseEvent.done(Map.of(
                                        "content", "抱歉，处理过程中出现异常，请稍后重试。",
                                        "durationMs", totalDuration,
                                        "tokensPrompt", 0,
                                        "tokensCompletion", 0
                                )));
                emitter.complete();
            }
        }
    }

    // =====================================================================
    //  场景配置 & 会话持久化
    // =====================================================================

    /**
     * 应用场景级配置覆盖
     * <p>修改 Agent 对象的相关字段，优先级：ai_scene_agent_config > ai_agent 默认值。</p>
     */
    private void applySceneConfig(Agent agent, Long sceneId) {
        if (sceneId == null) {
            return;
        }
        SceneAgentConfig config = sceneAgentConfigMapper.selectBySceneAndAgent(sceneId, agent.getId());
        if (config == null) {
            return;
        }
        if (config.getModel() != null && !config.getModel().isBlank()) {
            agent.setModelCode(config.getModel());
        }
        if (config.getTemperature() != null) {
            agent.setTemperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            agent.setMaxTokens(config.getMaxTokens());
        }
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isBlank()) {
            String existing = agent.getSystemPrompt();
            agent.setSystemPrompt(existing + "\n\n" + config.getSystemPrompt());
        }
        log.info("Agent[{}] 应用场景级配置(场景={}): model={}, temperature={}, maxTokens={}",
                agent.getName(), sceneId, config.getModel(), config.getTemperature(), config.getMaxTokens());
    }

    /**
     * 保存测试会话
     */
    private void saveTestSession(Agent agent, AiDialogRequest request, String reply,
                                 AgentTraceSnapshot snapshot, int durationMs,
                                 int promptTokens, int completionTokens, String modelName,
                                 String status, String errorMessage, Long userId) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("agent");
        session.setBizId(agent.getId());
        session.setBizName(agent.getName());
        session.setSceneId(request.getSceneId());
        session.setConversationId(request.getConversationId());
        session.setRequestId(request.getRequestId());
        session.setUserMessage(extractMessage(request));
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

    /**
     * 从统一请求中提取用户消息
     */
    private String extractMessage(AiDialogRequest request) {
        if (request.getQuestion() != null && !request.getQuestion().isBlank()) {
            return request.getQuestion();
        }
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            return request.getMessages().get(request.getMessages().size() - 1).getContent();
        }
        return "";
    }

    // =====================================================================
    //  本地意图快速判断
    // =====================================================================

    /**
     * 本地规则快速判断是否为闲聊，命中则直接返回 chat plan，未命中返回 null 走 LLM 规划。
     * <p>目的：对明确的闲聊场景（打招呼、自我介绍询问等）跳过 LLM 规划调用，
     * 既节省一次 LLM 请求开销，又避免 LLM 误判。</p>
     */
    private Map<String, Object> detectQuickIntent(String userMessage, List<Map<String, Object>> toolDefinitions) {
        if (userMessage == null || userMessage.isBlank()) {
            return null;
        }

        String msg = userMessage.trim().toLowerCase();

        // 无技能的 Agent，任何消息都直接走 llm_call（无业务能力可言）
        if (toolDefinitions == null || toolDefinitions.isEmpty()) {
            return quickChatPlan("Agent 无关联技能，直接对话");
        }

        // ===== 闲聊模式匹配 =====
        // 打招呼
        if (msg.matches("^(你好|hello|hi|hey|嗨|哈喽|早|早上好|下午好|晚上好|在吗|在不在|hey|yo|哈罗|您好|老师好|大侠好).*$")) {
            return quickChatPlan("打招呼，直接对话");
        }

        // 自我介绍 / 身份询问
        if (msg.matches("^(你是谁|你叫什么|你是什么|你能做什么|你会什么|你的名字|你是ai|你是机器人|你是人吗|你有什么能力|自我介绍|介绍下你自己|介绍一下你).*$")) {
            return quickChatPlan("询问身份，直接对话");
        }

        // 感谢 / 道别 / 通用礼貌
        if (msg.matches("^(谢谢|感谢|多谢|辛苦了|再见|拜拜|好的|ok|没问题|知道了|了解|明白|嗯|哦|啊|呀|额).*$")) {
            return quickChatPlan("礼貌用语，直接对话");
        }

        // 通用闲聊（极短消息且无业务关键词）
        if (msg.length() <= 6 && !containsBusinessKeyword(msg)) {
            return quickChatPlan("短消息闲聊，直接对话");
        }

        // 未命中本地规则，返回 null 交给 LLM 规划
        return null;
    }

    private boolean containsBusinessKeyword(String msg) {
        String[] keywords = {"股票", "基金", "行情", "涨", "跌", "价格", "k线", "k线",
                "财务", "利润", "营收", "分红", "市盈率", "pe", "roe",
                "查询", "分析", "诊断", "报告", "数据", "统计",
                "买入", "卖出", "持仓", "收益"};
        for (String kw : keywords) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }

    private Map<String, Object> quickChatPlan(String reason) {
        return new LinkedHashMap<>() {{
            put("intent", "chat");
            put("steps", List.of("llm_call"));
            put("reason", reason);
        }};
    }
}
