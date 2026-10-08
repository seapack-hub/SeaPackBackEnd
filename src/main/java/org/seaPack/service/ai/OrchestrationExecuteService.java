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
 * <p>
 * 负责加载编排配置、按策略委派执行、保存会话记录。
 * 具体执行逻辑由各策略类完成：
 * </p>
 * <ul>
 * <li>{@link SequentialStrategy} - 顺序执行（含条件分支、aggregate）</li>
 * <li>{@link ParallelStrategy} - 并行执行</li>
 * <li>{@link SupervisorStrategy} - Supervisor 模式（LLM 动态调度）</li>
 * <li>{@link CrewStrategy} - Crew 模式（Agent 自主委托）</li>
 * <li>{@link DynamicStrategy} - Dynamic 模式（LLM 动态规划）</li>
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

    @Autowired
    private org.seaPack.service.ai.tool.FileArtifactCollector fileArtifactCollector;

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
     * @param request   执行请求（orchestrationId, message, history）
     * @param userId    用户ID
     * @param authToken 认证令牌（透传给技能执行，内部 API 调用时携带）
     * @param emitter   SSE 发射器
     */
    public void execute(OrchestrationExecuteRequest request, Long userId, String authToken, SseEmitter emitter) {
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

        Long sessionId = null;
        SceneOrchestration orchestration = null;
        AIProperties.ProviderConfig config = null;
        String strategy = null;

        try {
            // 1. 加载编排：先尝试编排ID，再尝试场景ID
            orchestration = orchestrationMapper.selectById(request.getOrchestrationId());
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
            config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            // 4. 按策略执行
            strategy = orchestration.getStrategy() != null ? orchestration.getStrategy() : "sequential";

            // 4a. 落库 running 会话：执行开始即可见，中断/失败时收尾更新为 cancelled/failed，
            // 避免"连接中断后后端无任何状态记录"导致前端/历史永远停在执行中
            sessionId = insertRunningSession(request, orchestration, userId, config.getChatModel());

            // 4b. 发送编排启动事件：告知前端编排的基本信息和执行计划
            sendSseEvent(emitter, "orchestration_start", Map.of(
                    "orchestrationId", orchestration.getId(),
                    "orchestrationName", orchestration.getName() != null ? orchestration.getName() : "",
                    "strategy", strategy,
                    "totalSteps", steps.size(),
                    "provider", providerName,
                    "chatModel", config.getChatModel() != null ? config.getChatModel() : "",
                    "message",
                    "编排 [" + orchestration.getName() + "] 开始执行，策略: " + strategy + "，共 " + steps.size() + " 个步骤"));

            // 4c. 构建执行参数并委派给策略
            OrchestrationStrategyHandler.ExecuteParams params = new OrchestrationStrategyHandler.ExecuteParams();
            params.steps = steps;
            params.request = request;
            params.config = config;
            params.emitter = emitter;
            params.isCompleted = isCompleted;
            params.userId = userId;
            params.orchestration = orchestration;
            params.authToken = authToken;

            OrchestrationStrategyHandler handler = selectStrategy(strategy);
            OrchestrationStrategyHandler.OrchestrationResult result = handler.execute(params);

            if (isCompleted.get()) {
                // 连接已断开：不再静默丢弃，尽力补发 stop 事件并把会话收尾为 cancelled
                log.info("编排执行被中断，跳过 done 事件，收尾会话状态为 cancelled");
                long interruptedDuration = System.currentTimeMillis() - totalStart;
                SseEvent.trySend(emitter, "stop", Map.of(
                        "message", "编排执行中断（连接已关闭）",
                        "durationMs", interruptedDuration));
                List<Map<String, Object>> interruptedFiles = fileArtifactCollector.drain(emitter);
                updateSession(sessionId, orchestration, "cancelled", "编排执行中断（SSE 连接关闭）",
                        result != null ? result.output : null, interruptedDuration,
                        result != null ? result.tokensPrompt : 0, result != null ? result.tokensCompletion : 0,
                        config.getChatModel(), result != null ? result.steps : null, strategy, interruptedFiles);
                return;
            }

            // 5. 发送完成事件（汇总本次编排产出的文件，前端据此渲染文件卡片/下载入口）
            List<Map<String, Object>> generatedFiles = fileArtifactCollector.drain(emitter);
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", result.output);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("strategy", strategy);
            doneData.put("totalSteps", steps.size());
            doneData.put("tokens", Map.of(
                    "prompt", result.tokensPrompt,
                    "completion", result.tokensCompletion));
            if (generatedFiles != null) {
                doneData.put("files", generatedFiles);
            }
            doneData.put("message", "编排执行完成，共耗时 " + totalDuration + "ms");
            if (!SseEvent.trySend(emitter, "done", doneData)) {
                log.warn("编排 done 事件发送失败（连接可能已断开），会话仍按实际执行结果落库");
            }

            // 6. 更新执行会话为 success（用于刷新后链路追踪历史查询）
            try {
                updateSession(sessionId, orchestration, "success", null, result.output, totalDuration,
                        result.tokensPrompt, result.tokensCompletion,
                        config.getChatModel(), result.steps, strategy, generatedFiles);
            } catch (Exception ex) {
                log.warn("保存编排执行会话失败: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("编排执行异常", e);
            sendSseError(emitter, "编排执行失败: " + e.getMessage());
            try {
                updateSession(sessionId, orchestration, "failed", e.getMessage(), null,
                        System.currentTimeMillis() - totalStart, 0, 0,
                        config != null ? config.getChatModel() : null, null, strategy, null);
            } catch (Exception ex) {
                log.warn("更新编排执行会话(failed)失败: {}", ex.getMessage());
            }
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
     * <p>
     * 用于 LLM 动态选择 Agent 后的多 Agent 协作场景。
     * </p>
     *
     * @param steps     动态构建的步骤列表（stepIndex 从 1 开始）
     * @param strategy  执行策略：sequential / parallel
     * @param message   用户输入消息
     * @param history   对话历史
     * @param authToken 认证令牌（透传给技能执行）
     * @param emitter   SSE 发射器
     */
    public void executeDynamic(List<SceneOrchestrationStep> steps, String strategy,
            String message, List<Map<String, String>> history,
            Long sceneId, String conversationId, String requestId,
            Long userId, String authToken, SseEmitter emitter) {
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

        Long sessionId = null;
        AIProperties.ProviderConfig config = null;
        String execStrategy = null;

        try {
            // 1. 获取 AI 配置
            String providerName = aiProperties.getActiveProvider();
            config = aiProperties.getProviders().get(providerName);
            if (config == null) {
                sendSseError(emitter, "AI 配置错误：未找到提供商 [" + providerName + "]");
                return;
            }

            execStrategy = strategy != null ? strategy : "sequential";

            // 2. 落库 running 会话（中断/失败时收尾更新状态）
            OrchestrationExecuteRequest runningReq = new OrchestrationExecuteRequest();
            runningReq.setMessage(message);
            runningReq.setSceneId(sceneId);
            runningReq.setConversationId(conversationId);
            runningReq.setRequestId(requestId);
            sessionId = insertRunningSessionInternal(runningReq, 0L, "动态编排", userId, config.getChatModel());

            // 3. 发送编排启动事件
            sendSseEvent(emitter, "orchestration_start", Map.of(
                    "orchestrationName", "动态编排",
                    "strategy", execStrategy,
                    "totalSteps", steps.size(),
                    "provider", providerName,
                    "chatModel", config.getChatModel() != null ? config.getChatModel() : "",
                    "message",
                    "LLM 选择了 " + steps.size() + " 个 Agent，策略: " + execStrategy));

            // 4. 构造请求对象
            OrchestrationExecuteRequest request = new OrchestrationExecuteRequest();
            request.setMessage(message);
            request.setHistory(history);
            request.setSceneId(sceneId);
            request.setConversationId(conversationId);
            request.setRequestId(requestId);

            // 5. 按策略执行
            OrchestrationStrategyHandler.ExecuteParams params = new OrchestrationStrategyHandler.ExecuteParams();
            params.steps = steps;
            params.request = request;
            params.config = config;
            params.emitter = emitter;
            params.isCompleted = isCompleted;
            params.userId = userId;
            params.authToken = authToken;

            OrchestrationStrategyHandler handler = selectStrategy(execStrategy);
            OrchestrationStrategyHandler.OrchestrationResult result = handler.execute(params);

            if (isCompleted.get()) {
                log.info("动态编排执行被中断，收尾会话状态为 cancelled");
                long interruptedDuration = System.currentTimeMillis() - totalStart;
                SseEvent.trySend(emitter, "stop", Map.of(
                        "message", "动态编排执行中断（连接已关闭）",
                        "durationMs", interruptedDuration));
                List<Map<String, Object>> interruptedFiles = fileArtifactCollector.drain(emitter);
                updateSessionInternal(sessionId, "动态编排", "cancelled", "动态编排执行中断（SSE 连接关闭）",
                        result != null ? result.output : null, interruptedDuration,
                        result != null ? result.tokensPrompt : 0, result != null ? result.tokensCompletion : 0,
                        config.getChatModel(), result != null ? result.steps : null, execStrategy, interruptedFiles);
                return;
            }

            // 6. 发送完成事件（汇总本次编排产出的文件）
            List<Map<String, Object>> generatedFiles = fileArtifactCollector.drain(emitter);
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", result.output);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("strategy", execStrategy);
            doneData.put("totalSteps", steps.size());
            doneData.put("tokens", Map.of(
                    "prompt", result.tokensPrompt,
                    "completion", result.tokensCompletion));
            if (generatedFiles != null) {
                doneData.put("files", generatedFiles);
            }
            doneData.put("message", "动态编排执行完成，共耗时 " + totalDuration + "ms");
            if (!SseEvent.trySend(emitter, "done", doneData)) {
                log.warn("动态编排 done 事件发送失败（连接可能已断开），会话仍按实际执行结果落库");
            }

            // 7. 更新动态编排执行会话为 success（用于刷新后链路追踪历史查询）
            try {
                updateSessionInternal(sessionId, "动态编排", "success", null, result.output, totalDuration,
                        result.tokensPrompt, result.tokensCompletion,
                        config.getChatModel(), result.steps, execStrategy, generatedFiles);
            } catch (Exception ex) {
                log.warn("保存动态编排执行会话失败: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("动态编排执行异常", e);
            sendSseError(emitter, "动态编排执行失败: " + e.getMessage());
            try {
                updateSessionInternal(sessionId, "动态编排", "failed", e.getMessage(), null,
                        System.currentTimeMillis() - totalStart, 0, 0,
                        config != null ? config.getChatModel() : null, null, execStrategy, null);
            } catch (Exception ex) {
                log.warn("更新动态编排执行会话(failed)失败: {}", ex.getMessage());
            }
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
        if (strategy == null)
            strategy = "sequential";
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

    // ===== 会话状态机（链路追踪历史） =====

    /**
     * 落库 running 会话（编排执行开始时调用）
     * <p>
     * 执行结束（成功/中断/失败）时通过 updateSession 收尾更新状态，
     * 保证任何终态下历史记录都不会停留在"无记录/执行中"。
     * </p>
     */
    private Long insertRunningSession(OrchestrationExecuteRequest request, SceneOrchestration orchestration,
            Long userId, String modelName) {
        return insertRunningSessionInternal(request,
                orchestration != null ? orchestration.getId() : 0L,
                orchestration != null ? orchestration.getName() : "编排执行",
                userId, modelName);
    }

    /** 落库 running 会话（内部实现，兼容动态编排无编排实体的场景） */
    private Long insertRunningSessionInternal(OrchestrationExecuteRequest request, Long bizId, String bizName,
            Long userId, String modelName) {
        try {
            ExecutionSession session = new ExecutionSession();
            session.setBizType("orchestration");
            session.setBizId(bizId != null ? bizId : 0L);
            session.setBizName(bizName != null ? bizName : "编排执行");
            session.setSceneId(request != null ? request.getSceneId() : null);
            session.setConversationId(request != null ? request.getConversationId() : null);
            session.setRequestId(request != null ? request.getRequestId() : null);
            session.setUserMessage(request != null ? request.getMessage() : null);
            session.setStatus("running");
            session.setModelName(modelName);
            session.setTokensPrompt(0);
            session.setTokensCompletion(0);
            session.setTokensTotal(0);
            session.setCreatedBy(userId);
            executionSessionMapper.insert(session);
            return session.getId();
        } catch (Exception e) {
            log.warn("保存编排执行会话(running)失败: {}", e.getMessage());
            return null;
        }
    }

    /** 更新编排执行会话终态（success/cancelled/failed） */
    private void updateSession(Long sessionId, SceneOrchestration orchestration,
            String status, String errorMessage, String output, long durationMs,
            int tokensPrompt, int tokensCompletion, String modelName,
            List<AgentTraceStep> steps, String strategy, List<Map<String, Object>> files) {
        updateSessionInternal(sessionId, orchestration != null ? orchestration.getName() : null,
                status, errorMessage, output, durationMs, tokensPrompt, tokensCompletion,
                modelName, steps, strategy, files);
    }

    /** 更新执行会话终态（内部实现） */
    private void updateSessionInternal(Long sessionId, String routeName,
            String status, String errorMessage, String output, long durationMs,
            int tokensPrompt, int tokensCompletion, String modelName,
            List<AgentTraceStep> steps, String strategy, List<Map<String, Object>> files) {
        if (sessionId == null) {
            return;
        }
        try {
            ExecutionSession session = new ExecutionSession();
            session.setId(sessionId);
            session.setStatus(status);
            session.setErrorMessage(errorMessage);
            session.setOutputResult(output);
            session.setTraceSnapshot(buildTraceSnapshot(steps, durationMs, tokensPrompt, tokensCompletion,
                    "orchestration", routeName, strategy, files));
            session.setTotalDurationMs((int) durationMs);
            session.setTokensPrompt(tokensPrompt);
            session.setTokensCompletion(tokensCompletion);
            session.setTokensTotal(tokensPrompt + tokensCompletion);
            session.setModelName(modelName);
            executionSessionMapper.update(session);
        } catch (Exception e) {
            log.warn("更新编排执行会话失败: {}", e.getMessage());
        }
    }

    /**
     * 构建链路追踪快照 JSON（新方案结构）
     * <p>
     * 编排执行：{route, orchestrationName, strategy, steps:[{stepIndex, stepName,
     * agentId, agentName,
     * model, input, output, durationMs, tokensPrompt, tokensCompletion, status}],
     * totalTokensPrompt, totalTokensCompletion, totalDurationMs}
     * </p>
     */
    private String buildTraceSnapshot(List<AgentTraceStep> steps, long durationMs,
            int tokensPrompt, int tokensCompletion,
            String route, String routeName, String strategy, List<Map<String, Object>> files) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("route", route);
        if (routeName != null && !routeName.isBlank()) {
            snapshot.put("orchestrationName", routeName);
        }
        if (strategy != null && !strategy.isBlank()) {
            snapshot.put("strategy", strategy);
        }
        if (files != null && !files.isEmpty()) {
            snapshot.put("files", files);
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
                    if (meta.containsKey("agentId"))
                        m.put("agentId", meta.get("agentId"));
                    if (meta.containsKey("agentName"))
                        m.put("agentName", meta.get("agentName"));
                    if (meta.containsKey("model"))
                        m.put("model", meta.get("model"));
                    if (meta.containsKey("tokensPrompt"))
                        m.put("tokensPrompt", meta.get("tokensPrompt"));
                    if (meta.containsKey("tokensCompletion"))
                        m.put("tokensCompletion", meta.get("tokensCompletion"));
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
