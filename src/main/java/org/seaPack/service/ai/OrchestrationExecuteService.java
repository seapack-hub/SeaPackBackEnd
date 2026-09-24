package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import org.seaPack.service.ai.orchestration.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 编排执行服务（轻量委派层）
 * <p>负责加载编排配置、按策略委派执行、保存会话记录。
 * 具体执行逻辑由各策略类完成：</p>
 * <ul>
 *   <li>{@link SequentialStrategy} - 顺序执行（含条件分支、aggregate）</li>
 *   <li>{@link ParallelStrategy} - 并行执行</li>
 *   <li>{@link SupervisorStrategy} - Supervisor 模式（LLM 动态调度）</li>
 *   <li>{@link CrewStrategy} - Crew 模式（Agent 自主委托）</li>
 *   <li>{@link DynamicStrategy} - Dynamic 模式（LLM 动态规划）</li>
 * </ul>
 */
@Slf4j
@Service
public class OrchestrationExecuteService {

    @Autowired
    private SceneOrchestrationMapper orchestrationMapper;

    @Autowired
    private SceneOrchestrationStepMapper stepMapper;

    @Autowired
    private SceneMapper sceneMapper;

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== 策略实例注入 =====

    @Autowired
    private SequentialStrategy sequentialStrategy;

    @Autowired
    private ParallelStrategy parallelStrategy;

    @Autowired
    private SupervisorStrategy supervisorStrategy;

    @Autowired
    private CrewStrategy crewStrategy;

    @Autowired
    private DynamicStrategy dynamicStrategy;

    // ===== 主入口 =====

    /**
     * 执行编排（SSE 流式输出）
     *
     * @param request 执行请求（orchestrationId, message, history）
     * @param emitter SSE 发射器
     */
    public void execute(OrchestrationExecuteRequest request, Long userId, SseEmitter emitter) {
        long totalStart = System.currentTimeMillis();
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // 注册完成回调：客户端断连时标记中断
        emitter.onCompletion(() -> {
            log.info("编排 SSE 连接已关闭，设置中断标记");
            isCompleted.set(true);
        });
        emitter.onTimeout(() -> {
            log.warn("编排 SSE 连接超时");
            isCompleted.set(true);
        });

        try {
            // 1. 加载编排：先尝试编排ID，再尝试场景ID
            SceneOrchestration orchestration = orchestrationMapper.selectById(request.getOrchestrationId());
            if (orchestration == null) {
                // 不是编排ID，尝试作为场景ID查询
                Scene scene = sceneMapper.selectById(request.getOrchestrationId());
                if (scene == null) {
                    sendSseError(emitter, "未找到编排或场景: " + request.getOrchestrationId());
                    return;
                }
                // 查找该场景下第一个启用的编排（按 sort_order 升序）
                List<SceneOrchestration> sceneOrchestrations = orchestrationMapper.selectBySceneId(scene.getId());
                orchestration = sceneOrchestrations.stream()
                        .filter(o -> o.getStatus() != null && o.getStatus() == 1)
                        .findFirst()
                        .orElse(null);
                if (orchestration == null) {
                    sendSseError(emitter, "场景 [" + scene.getName() + "] 下没有启用的编排");
                    return;
                }
                log.info("通过场景ID [{}] 找到编排 [{}]", scene.getId(), orchestration.getName());
            }
            if (orchestration.getStatus() == null || orchestration.getStatus() != 1) {
                sendSseError(emitter, "编排已禁用: " + orchestration.getName());
                return;
            }

            // 2. 加载步骤（按 step_index 升序）
            List<SceneOrchestrationStep> steps = stepMapper.selectByOrchestrationId(orchestration.getId());
            if (steps == null || steps.isEmpty()) {
                sendSseError(emitter, "编排没有定义任何步骤: " + orchestration.getName());
                return;
            }

            // 3. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            // 4. 按策略执行
            String strategy = orchestration.getStrategy() != null ? orchestration.getStrategy() : "sequential";

            // 4a. 发送编排启动事件：告知前端编排的基本信息和执行计划
            sendSseEvent(emitter, "orchestration_start", Map.of(
                    "orchestrationId", orchestration.getId(),
                    "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "",
                    "strategy", strategy,
                    "totalSteps", steps.size(),
                    "provider", providerName,
                    "chatModel", config.getChatModel() != null ? config.getChatModel() : "",
                    "message", "编排 [" + orchestration.getName() + "] 开始执行，策略: " + strategy + "，共 " + steps.size() + " 个步骤"
            ));

            // 4b. 构建执行参数并委派给策略
            OrchestrationStrategyHandler.ExecuteParams params = new OrchestrationStrategyHandler.ExecuteParams();
            params.steps = steps;
            params.request = request;
            params.config = config;
            params.emitter = emitter;
            params.isCompleted = isCompleted;
            params.userId = userId;
            params.orchestration = orchestration;

            OrchestrationStrategyHandler handler = selectStrategy(strategy);
            OrchestrationStrategyHandler.OrchestrationResult result = handler.execute(params);

            if (isCompleted.get()) {
                log.info("编排执行被中断，跳过 done 事件");
                return;
            }

            // 5. 发送完成事件
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", result.output);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("strategy", strategy);
            doneData.put("totalSteps", steps.size());
            doneData.put("tokens", Map.of(
                    "prompt", result.tokensPrompt,
                    "completion", result.tokensCompletion
            ));
            doneData.put("message", "编排执行完成，共耗时 " + totalDuration + "ms");
            sendSseEvent(emitter, "done", doneData);

            // 6. 保存执行会话（用于刷新后链路追踪历史查询）
            try {
                saveSession(request, orchestration, result.output, totalDuration,
                        result.tokensPrompt, result.tokensCompletion,
                        config.getChatModel(), "success", null, userId, result.steps, strategy);
            } catch (Exception ex) {
                log.warn("保存编排执行会话失败: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("编排执行异常", e);
            sendSseError(emitter, "编排执行失败: " + e.getMessage());
        } finally {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    // ===== 动态编排执行（LLM 路由构建的步骤） =====

    /**
     * 执行动态构建的编排步骤（不查数据库，直接执行传入的步骤列表）
     * <p>用于 LLM 动态选择 Agent 后的多 Agent 协作场景。</p>
     *
     * @param steps     动态构建的步骤列表（stepIndex 从 1 开始）
     * @param strategy  执行策略：sequential / parallel
     * @param message   用户输入消息
     * @param history   对话历史
     * @param emitter   SSE 发射器
     */
    public void executeDynamic(List<SceneOrchestrationStep> steps, String strategy,
                               String message, List<Map<String, String>> history,
                               Long sceneId, String conversationId, String requestId,
                               Long userId, SseEmitter emitter) {
        long totalStart = System.currentTimeMillis();
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        emitter.onCompletion(() -> {
            log.info("动态编排 SSE 连接已关闭");
            isCompleted.set(true);
        });
        emitter.onTimeout(() -> {
            log.warn("动态编排 SSE 连接超时");
            isCompleted.set(true);
        });

        try {
            // 1. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            // 2. 发送编排启动事件
            sendSseEvent(emitter, "orchestration_start", Map.of(
                    "orchestrationName", "动态编排",
                    "strategy", strategy != null ? strategy : "sequential",
                    "totalSteps", steps.size(),
                    "provider", providerName,
                    "chatModel", config.getChatModel() != null ? config.getChatModel() : "",
                    "message", "LLM 选择了 " + steps.size() + " 个 Agent，策略: " + (strategy != null ? strategy : "sequential")
            ));

            // 3. 构造请求对象
            OrchestrationExecuteRequest request = new OrchestrationExecuteRequest();
            request.setMessage(message);
            request.setHistory(history);
            request.setSceneId(sceneId);
            request.setConversationId(conversationId);
            request.setRequestId(requestId);

            // 4. 按策略执行
            String execStrategy = strategy != null ? strategy : "sequential";

            OrchestrationStrategyHandler.ExecuteParams params = new OrchestrationStrategyHandler.ExecuteParams();
            params.steps = steps;
            params.request = request;
            params.config = config;
            params.emitter = emitter;
            params.isCompleted = isCompleted;
            params.userId = userId;

            OrchestrationStrategyHandler handler = selectStrategy(execStrategy);
            OrchestrationStrategyHandler.OrchestrationResult result = handler.execute(params);

            if (isCompleted.get()) {
                log.info("动态编排执行被中断");
                return;
            }

            // 5. 发送完成事件
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", result.output);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("strategy", execStrategy);
            doneData.put("totalSteps", steps.size());
            doneData.put("tokens", Map.of(
                    "prompt", result.tokensPrompt,
                    "completion", result.tokensCompletion
            ));
            doneData.put("message", "动态编排执行完成，共耗时 " + totalDuration + "ms");
            sendSseEvent(emitter, "done", doneData);

            // 6. 保存动态编排执行会话（用于刷新后链路追踪历史查询）
            try {
                saveDynamicSession(request, result.output, totalDuration,
                        result.tokensPrompt, result.tokensCompletion,
                        config.getChatModel(), "success", null, userId, result.steps, execStrategy);
            } catch (Exception ex) {
                log.warn("保存动态编排执行会话失败: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("动态编排执行异常", e);
            sendSseError(emitter, "动态编排执行失败: " + e.getMessage());
        } finally {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    // ===== 策略选择 =====

    /**
     * 根据策略名称选择对应的策略处理器
     */
    private OrchestrationStrategyHandler selectStrategy(String strategy) {
        if (strategy == null) strategy = "sequential";
        switch (strategy) {
            case "parallel":
                return parallelStrategy;
            case "supervisor":
                return supervisorStrategy;
            case "crew":
                return crewStrategy;
            case "dynamic":
                return dynamicStrategy;
            default:
                return sequentialStrategy;
        }
    }

    // ===== SSE 工具 =====

    /** 发送 SSE 事件 */
    private void sendSseEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        SseEvent.send(emitter, type, data);
    }

    /** 发送错误事件 */
    private void sendSseError(SseEmitter emitter, String errorMessage) {
        sendSseEvent(emitter, "error", Map.of("errorMessage", errorMessage));
    }

    // ===== 会话落库（链路追踪历史） =====

    /**
     * 保存编排执行会话
     */
    private void saveSession(OrchestrationExecuteRequest request, SceneOrchestration orchestration,
                             String output, long durationMs, int tokensPrompt, int tokensCompletion,
                             String modelName, String status, String errorMessage, Long userId,
                             List<AgentTraceStep> steps, String strategy) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("orchestration");
        session.setBizId(orchestration != null ? orchestration.getId() : 0L);
        session.setBizName(orchestration != null ? orchestration.getName() : "编排执行");
        session.setSceneId(request != null ? request.getSceneId() : null);
        session.setConversationId(request != null ? request.getConversationId() : null);
        session.setRequestId(request != null ? request.getRequestId() : null);
        session.setUserMessage(request != null ? request.getMessage() : null);
        session.setOutputResult(output);
        session.setTraceSnapshot(buildTraceSnapshot(steps, durationMs, tokensPrompt, tokensCompletion,
                "orchestration", orchestration != null ? orchestration.getName() : null, strategy));
        session.setTotalDurationMs((int) durationMs);
        session.setTokensPrompt(tokensPrompt);
        session.setTokensCompletion(tokensCompletion);
        session.setTokensTotal(tokensPrompt + tokensCompletion);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    /**
     * 保存动态编排执行会话
     */
    private void saveDynamicSession(OrchestrationExecuteRequest request,
                                    String output, long durationMs, int tokensPrompt, int tokensCompletion,
                                    String modelName, String status, String errorMessage, Long userId,
                                    List<AgentTraceStep> steps, String strategy) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("orchestration");
        session.setBizId(0L);
        session.setBizName("动态编排");
        session.setSceneId(request != null ? request.getSceneId() : null);
        session.setConversationId(request != null ? request.getConversationId() : null);
        session.setRequestId(request != null ? request.getRequestId() : null);
        session.setUserMessage(request != null ? request.getMessage() : null);
        session.setOutputResult(output);
        session.setTraceSnapshot(buildTraceSnapshot(steps, durationMs, tokensPrompt, tokensCompletion,
                "orchestration", "动态编排", strategy));
        session.setTotalDurationMs((int) durationMs);
        session.setTokensPrompt(tokensPrompt);
        session.setTokensCompletion(tokensCompletion);
        session.setTokensTotal(tokensPrompt + tokensCompletion);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    /**
     * 构建链路追踪快照 JSON（新方案结构）
     * <p>编排执行：{route, orchestrationName, strategy, steps:[{stepIndex, stepName, agentId, agentName,
     * model, input, output, durationMs, tokensPrompt, tokensCompletion, status}],
     * totalTokensPrompt, totalTokensCompletion, totalDurationMs}</p>
     */
    private String buildTraceSnapshot(List<AgentTraceStep> steps, long durationMs,
                                      int tokensPrompt, int tokensCompletion,
                                      String route, String routeName, String strategy) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("route", route);
        if (routeName != null && !routeName.isBlank()) {
            snapshot.put("orchestrationName", routeName);
        }
        if (strategy != null && !strategy.isBlank()) {
            snapshot.put("strategy", strategy);
        }
        List<Map<String, Object>> stepMaps = new ArrayList<>();
        if (steps != null) {
            for (AgentTraceStep s : steps) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("stepIndex", s.getStepIndex());
                m.put("stepName", s.getStepName());
                m.put("status", s.getStatus());
                m.put("durationMs", s.getDurationMs());
                m.put("input", s.getInput());
                m.put("output", s.getOutput());
                Map<String, Object> meta = s.getMetadata();
                if (meta != null) {
                    if (meta.containsKey("agentId")) m.put("agentId", meta.get("agentId"));
                    if (meta.containsKey("agentName")) m.put("agentName", meta.get("agentName"));
                    if (meta.containsKey("model")) m.put("model", meta.get("model"));
                    if (meta.containsKey("tokensPrompt")) m.put("tokensPrompt", meta.get("tokensPrompt"));
                    if (meta.containsKey("tokensCompletion")) m.put("tokensCompletion", meta.get("tokensCompletion"));
                }
                stepMaps.add(m);
            }
        }
        snapshot.put("steps", stepMaps);
        snapshot.put("totalTokensPrompt", tokensPrompt);
        snapshot.put("totalTokensCompletion", tokensCompletion);
        snapshot.put("totalDurationMs", durationMs);
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.warn("序列化链路快照失败: {}", e.getMessage());
            return "{}";
        }
    }
}
