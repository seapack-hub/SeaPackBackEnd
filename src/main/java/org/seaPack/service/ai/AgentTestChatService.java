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
 * <p>负责测试对话的完整链路编排，固定四步流程：</p>
 * <ol>
 *   <li>提示词组装（加载 Agent 基础提示词 + 启用的模板 + 工具约束规则）</li>
 *   <li>知识库检索（有知识库则检索）</li>
 *   <li>LLM 流式调用（始终传 tools，由 LLM 自主决定调用）</li>
 *   <li>构建响应并保存会话</li>
 * </ol>
 * <p>Agent 是"执行者"，不做意图分类。路由决策应在 Agent 之外完成。</p>
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
     * <p>固定四步流程：提示词组装 → 知识库检索 → LLM 流式调用 → 保存测试会话。</p>
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
            String userMsg = extractMessage(request);

            // 2. 加载 Agent 关联的技能定义
            List<Map<String, Object>> toolDefinitions = new ArrayList<>();
            try {
                toolDefinitions = skillExecutor.getToolDefinitions(request.getAgentId());
                log.info("Agent[{}] 加载技能定义成功: count={}", agent.getName(), toolDefinitions.size());
            } catch (Exception e) {
                log.error("Agent[{}] 加载技能定义失败: {}", agent.getName(), e.getMessage(), e);
            }

            // ===== 固定四步流程（Agent 是执行者，不做意图分类） =====
            String systemPrompt = null;
            String knowledgeContext = null;
            int stepIndex = 0;

            // Step 1: 提示词组装（固定执行）
            if (emitter != null) {
                SseEvent.send(emitter, "step_start", Map.of(
                        "stepIndex", stepIndex,
                        "stepType", "prompt_assembly",
                        "stepName", "提示词组装"
                ));
            }
            AgentTraceStepResult promptResult = agentPromptService.assemblePrompt(agent, stepIndex,
                    userMsg, emitter, userId, request.getSceneId(), agent.getId(), request.getRequestId(),
                    !toolDefinitions.isEmpty());
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

            // Step 2: 知识库检索（有知识库则检索，无条件执行）
            boolean hasKnowledge = agentKnowledgeMapper.selectByAgentId(agent.getId()).stream()
                    .anyMatch(k -> k.getEnabled() != null && k.getEnabled() == 1);
            if (hasKnowledge) {
                if (emitter != null) {
                    SseEvent.send(emitter, "step_start", Map.of(
                            "stepIndex", stepIndex,
                            "stepType", "knowledge_retrieval",
                            "stepName", "知识库检索"
                    ));
                }
                AgentTraceStepResult kbResult = knowledgeBaseService.retrieveKnowledge(agent, userMsg, stepIndex, emitter);
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

            // Step 3 & 4: 工具调用 + LLM 输出（核心步骤，由 AgentLlmCaller 内部推送 SSE 事件）
            // toolStepIndex: 工具调用步骤的索引，llmStepIndex: LLM 输出步骤的索引
            int toolStepIndex = stepIndex;
            int llmStepIndex = hasKnowledge ? stepIndex + 1 : stepIndex;
            if (!toolDefinitions.isEmpty()) {
                // 有工具时：step3=工具调用，step4=LLM输出
                llmStepIndex = stepIndex + 1;
            }
            AgentTraceStepResult llmResult = agentLlmCaller.callLLMStreamWithTools(agent, systemPrompt,
                    knowledgeContext, request, toolStepIndex, llmStepIndex, emitter,
                    new AtomicBoolean(false), cancelFlag, toolDefinitions, authToken);

            // 构建步骤列表：顺序必须是 工具调用 → LLM 输出
            // extraSteps 包含工具调用步骤（含子步骤），需插入到 LLM 步骤之前
            if (llmResult.extraSteps != null && !llmResult.extraSteps.isEmpty()) {
                steps.addAll(llmResult.extraSteps);  // Step 3: 工具调用（含子步骤）
            }
            steps.add(llmResult.step);  // Step 4: LLM 输出

            // 调试日志：验证步骤结构
            log.info("Agent 步骤构建完成: totalSteps={}, steps=[{}]", steps.size(),
                    steps.stream().map(s -> s.getStepType() + "(" + s.getStepIndex() + ")"
                            + "[children=" + (s.getChildren() != null ? s.getChildren().size() : 0) + "]")
                            .collect(java.util.stream.Collectors.joining(", ")));

            if (emitter != null) {
                SseEvent.send(emitter, "step_done", Map.of(
                        "stepIndex", llmStepIndex,
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
            String snapshotJson = objectMapper.writeValueAsString(snapshot);
            session.setTraceSnapshot(snapshotJson);
            log.info("saveTestSession: snapshotJson长度={}, 包含children={}",
                    snapshotJson.length(),
                    snapshotJson.contains("\"children\""));
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
}
