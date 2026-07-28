package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.OrchestrationExecuteRequest;
import org.seaPack.mapper.ai.AgentMapper;
import org.seaPack.mapper.ai.SceneOrchestrationMapper;
import org.seaPack.mapper.ai.SceneOrchestrationStepMapper;
import org.seaPack.model.ai.Agent;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 编排执行服务
 * <p>负责解析编排策略并按步骤依次/并行执行 Agent，
 * 通过 SSE 流式输出每步的进度、内容和最终结果。
 * 直接调用 LLM API（流式），不复用 AgentTestChatService。</p>
 */
@Slf4j
@Service
public class OrchestrationExecuteService {

    @Autowired
    private SceneOrchestrationMapper orchestrationMapper;

    @Autowired
    private SceneOrchestrationStepMapper stepMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AIProperties aiProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== 主入口 =====

    /**
     * 执行编排（SSE 流式输出）
     *
     * @param request 执行请求（orchestrationId, message, history）
     * @param emitter SSE 发射器
     */
    public void execute(OrchestrationExecuteRequest request, SseEmitter emitter) {
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
            // 1. 加载编排
            SceneOrchestration orchestration = orchestrationMapper.selectById(request.getOrchestrationId());
            if (orchestration == null) {
                sendSseError(emitter, "编排不存在: " + request.getOrchestrationId());
                return;
            }
            if (orchestration.getStatus() == null || orchestration.getStatus() != 1) {
                sendSseError(emitter, "编排已禁用: " + orchestration.getName());
                return;
            }

            // 2. 加载步骤（按 step_index 升序）
            List<SceneOrchestrationStep> steps = stepMapper.selectByOrchestrationId(request.getOrchestrationId());
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
            String mergedResult;
            int totalTokensPrompt = 0;
            int totalTokensCompletion = 0;

            switch (strategy) {
                case "parallel":
                    OrchestrationResult parallelResult = executeParallel(steps, request, config, emitter, isCompleted);
                    mergedResult = parallelResult.output;
                    totalTokensPrompt = parallelResult.tokensPrompt;
                    totalTokensCompletion = parallelResult.tokensCompletion;
                    break;
                default:
                    // sequential（含 auto 单步骤时退化）
                    OrchestrationResult sequentialResult = executeSequential(steps, request, config, emitter, isCompleted);
                    mergedResult = sequentialResult.output;
                    totalTokensPrompt = sequentialResult.tokensPrompt;
                    totalTokensCompletion = sequentialResult.tokensCompletion;
                    break;
            }

            if (isCompleted.get()) {
                log.info("编排执行被中断，跳过 done 事件");
                return;
            }

            // 5. 发送完成事件
            long totalDuration = System.currentTimeMillis() - totalStart;
            Map<String, Object> doneData = new HashMap<>();
            doneData.put("result", mergedResult);
            doneData.put("totalDurationMs", totalDuration);
            doneData.put("tokens", Map.of(
                    "prompt", totalTokensPrompt,
                    "completion", totalTokensCompletion
            ));
            sendSseEvent(emitter, "done", doneData);

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

    // ===== 顺序执行 =====

    /**
     * 顺序执行：按步骤索引依次执行，上一步的输出可作为下一步的输入映射源
     */
    private OrchestrationResult executeSequential(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            AIProperties.ProviderConfig config,
            SseEmitter emitter,
            AtomicBoolean isCompleted) {

        StringBuilder overallOutput = new StringBuilder();
        // 缓存每步输出 key: stepIndex, value: output
        Map<Integer, String> stepOutputs = new HashMap<>();
        // 缓存每步状态
        Map<Integer, String> stepStatuses = new HashMap<>();
        int totalPrompt = 0;
        int totalCompletion = 0;

        for (SceneOrchestrationStep step : steps) {
            if (isCompleted.get()) break;

            // 跳过已禁用的步骤
            if (step.getStatus() != null && step.getStatus() != 1) {
                continue;
            }

            long stepStart = System.currentTimeMillis();
            int stepIdx = step.getStepIndex();

            // 4a. 发送 step_start
            sendSseEvent(emitter, "step_start", Map.of(
                    "stepIndex", stepIdx,
                    "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)
            ));

            try {
                // 4b. 条件评估
                if (step.getCondition() != null && !step.getCondition().isBlank()) {
                    boolean conditionMet = evaluateCondition(step.getCondition(), stepOutputs, stepStatuses);
                    if (!conditionMet) {
                        sendSseEvent(emitter, "step_done", Map.of(
                                "stepIndex", stepIdx,
                                "stepName", step.getStepName(),
                                "status", "skip",
                                "message", "条件不满足: " + step.getCondition(),
                                "durationMs", 0
                        ));
                        stepStatuses.put(stepIdx, "skip");
                        continue;
                    }
                }

                // 4c. 加载 Agent
                Agent agent = agentMapper.selectById(step.getAgentId());
                if (agent == null) {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "errorMessage", "Agent 不存在: " + step.getAgentId()
                    ));
                    stepStatuses.put(stepIdx, "fail");
                    continue;
                }
                if (agent.getStatus() == null || agent.getStatus() != 1) {
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "errorMessage", "Agent 已禁用: " + agent.getName()
                    ));
                    stepStatuses.put(stepIdx, "fail");
                    continue;
                }

                // 4d. 解析输入映射
                String stepInput = resolveInputMapping(step.getInputMapping(), stepOutputs, request.getMessage());

                // 4e. 调用 LLM 流式输出
                sendSseEvent(emitter, "step_progress", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "message", "正在调用 " + agent.getName() + " ..."
                ));

                StepLlmResult llmResult = callLlmStream(agent, stepInput, request.getHistory(),
                        config, emitter, stepIdx, isCompleted);

                if (isCompleted.get()) break;

                // 4f. 保存输出
                stepOutputs.put(stepIdx, llmResult.output);
                stepStatuses.put(stepIdx, "success");
                totalPrompt += llmResult.tokensPrompt;
                totalCompletion += llmResult.tokensCompletion;

                // 追加到总输出
                if (overallOutput.length() > 0 && !llmResult.output.isEmpty()) {
                    overallOutput.append("\n\n");
                }
                overallOutput.append(llmResult.output);

                long stepDuration = System.currentTimeMillis() - stepStart;
                sendSseEvent(emitter, "step_done", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "status", "success",
                        "durationMs", stepDuration,
                        "output", llmResult.output.length() > 200
                                ? llmResult.output.substring(0, 200) + "..."
                                : llmResult.output
                ));

            } catch (Exception e) {
                log.warn("步骤[{}]执行异常: {}", step.getStepName(), e.getMessage());
                stepStatuses.put(stepIdx, "fail");

                // 重试逻辑
                if (step.getRetryCount() != null && step.getRetryCount() > 0) {
                    boolean retried = false;
                    for (int i = 0; i < step.getRetryCount(); i++) {
                        if (isCompleted.get()) break;
                        log.info("步骤[{}] 第{}次重试", step.getStepName(), i + 1);
                        try {
                            Agent agent = agentMapper.selectById(step.getAgentId());
                            if (agent == null) continue;

                            sendSseEvent(emitter, "step_progress", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "message", "第" + (i + 1) + "次重试..."
                            ));

                            String stepInput = resolveInputMapping(step.getInputMapping(), stepOutputs, request.getMessage());
                            StepLlmResult llmResult = callLlmStream(agent, stepInput, request.getHistory(),
                                    config, emitter, stepIdx, isCompleted);

                            stepOutputs.put(stepIdx, llmResult.output);
                            stepStatuses.put(stepIdx, "success");
                            totalPrompt += llmResult.tokensPrompt;
                            totalCompletion += llmResult.tokensCompletion;

                            if (overallOutput.length() > 0 && !llmResult.output.isEmpty()) {
                                overallOutput.append("\n\n");
                            }
                            overallOutput.append(llmResult.output);

                            long stepDuration = System.currentTimeMillis() - stepStart;
                            sendSseEvent(emitter, "step_done", Map.of(
                                    "stepIndex", stepIdx,
                                    "stepName", step.getStepName(),
                                    "status", "success",
                                    "durationMs", stepDuration
                            ));
                            retried = true;
                            break;
                        } catch (Exception retryEx) {
                            log.warn("步骤[{}] 第{}次重试失败: {}", step.getStepName(), i + 1, retryEx.getMessage());
                        }
                    }
                    if (retried) continue;
                }

                sendSseEvent(emitter, "step_error", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName(),
                        "errorMessage", e.getMessage()
                ));
            }
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt;
        result.tokensCompletion = totalCompletion;
        return result;
    }

    // ===== 并行执行 =====

    /**
     * 并行执行：所有步骤同时调用各自的 Agent，最终合并输出。
     * 注意：并行模式下 input_mapping 不能引用其他步骤的输出（因为同时执行）。
     */
    private OrchestrationResult executeParallel(
            List<SceneOrchestrationStep> steps,
            OrchestrationExecuteRequest request,
            AIProperties.ProviderConfig config,
            SseEmitter emitter,
            AtomicBoolean isCompleted) {

        StringBuilder overallOutput = new StringBuilder();
        int[] totalPrompt = {0};
        int[] totalCompletion = {0};
        // 按步骤索引排序的并行结果
        int stepCount = steps.size();
        String[] orderedOutputs = new String[stepCount];

        // 使用 CompletableFuture 并发执行所有步骤
        @SuppressWarnings("unchecked")
        java.util.concurrent.CompletableFuture<Void>[] futures = new java.util.concurrent.CompletableFuture[stepCount];

        for (int i = 0; i < stepCount; i++) {
            SceneOrchestrationStep step = steps.get(i);
            int index = i;
            int stepIdx = step.getStepIndex();

            futures[i] = java.util.concurrent.CompletableFuture.runAsync(() -> {
                if (isCompleted.get()) return;

                long stepStart = System.currentTimeMillis();
                sendSseEvent(emitter, "step_start", Map.of(
                        "stepIndex", stepIdx,
                        "stepName", step.getStepName() != null ? step.getStepName() : ("步骤" + stepIdx)
                ));

                try {
                    if (step.getStatus() != null && step.getStatus() != 1) {
                        orderedOutputs[index] = "";
                        return;
                    }

                    Agent agent = agentMapper.selectById(step.getAgentId());
                    if (agent == null || agent.getStatus() == null || agent.getStatus() != 1) {
                        orderedOutputs[index] = "";
                        sendSseEvent(emitter, "step_error", Map.of(
                                "stepIndex", stepIdx,
                                "stepName", step.getStepName(),
                                "errorMessage", "Agent 不可用: " + (agent != null ? agent.getName() : step.getAgentId())
                        ));
                        return;
                    }

                    String stepInput = resolveInputMapping(step.getInputMapping(), new HashMap<>(), request.getMessage());
                    StepLlmResult llmResult = callLlmStream(agent, stepInput, request.getHistory(),
                            config, emitter, stepIdx, isCompleted);

                    orderedOutputs[index] = llmResult.output;
                    totalPrompt[0] += llmResult.tokensPrompt;
                    totalCompletion[0] += llmResult.tokensCompletion;

                    long stepDuration = System.currentTimeMillis() - stepStart;
                    sendSseEvent(emitter, "step_done", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "status", "success",
                            "durationMs", stepDuration
                    ));
                } catch (Exception e) {
                    orderedOutputs[index] = "";
                    sendSseEvent(emitter, "step_error", Map.of(
                            "stepIndex", stepIdx,
                            "stepName", step.getStepName(),
                            "errorMessage", e.getMessage()
                    ));
                }
            });
        }

        // 等待所有步骤完成
        try {
            java.util.concurrent.CompletableFuture.allOf(futures).get();
        } catch (Exception e) {
            log.warn("并行执行等待中断: {}", e.getMessage());
        }

        // 按顺序合并输出
        for (int i = 0; i < stepCount; i++) {
            if (orderedOutputs[i] != null && !orderedOutputs[i].isEmpty()) {
                if (overallOutput.length() > 0) {
                    overallOutput.append("\n\n");
                }
                overallOutput.append(orderedOutputs[i]);
            }
        }

        OrchestrationResult result = new OrchestrationResult();
        result.output = overallOutput.toString();
        result.tokensPrompt = totalPrompt[0];
        result.tokensCompletion = totalCompletion[0];
        return result;
    }

    // ===== LLM 流式调用 =====

    /**
     * 流式调用 LLM API，逐 token 发送 content 事件
     *
     * @param agent       Agent 实体（含 systemPrompt、modelCode、temperature 等）
     * @param userMessage 当前输入（经过 input_mapping 解析后）
     * @param history     历史消息列表
     * @param config      AI 提供商配置
     * @param emitter     SSE 发射器
     * @param stepIndex   当前步骤索引（用于事件）
     * @param isCompleted 中断标记
     * @return 完整输出和 token 统计
     */
    private StepLlmResult callLlmStream(
            Agent agent,
            String userMessage,
            List<Map<String, String>> history,
            AIProperties.ProviderConfig config,
            SseEmitter emitter,
            int stepIndex,
            AtomicBoolean isCompleted) {

        long llmStart = System.currentTimeMillis();
        String modelName = agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel();

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // system prompt
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", agent.getSystemPrompt());
            messages.add(systemMsg);
        }

        // 历史消息（受限窗口）
        if (history != null && !history.isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> trimmedHistory = history;
            if (history.size() > window * 2) {
                trimmedHistory = history.subList(history.size() - window * 2, history.size());
            }
            messages.addAll(trimmedHistory);
        }

        // 用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        // 构建请求
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        if (agent.getTemperature() != null) {
            requestBody.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            requestBody.put("max_tokens", agent.getMaxTokens());
        }

        StringBuilder outputBuilder = new StringBuilder();
        int[] tokenUsage = {0, 0}; // [prompt, completion]

        try {
            // 使用 HttpURLConnection 实现流式请求
            HttpURLConnection connection = createStreamingConnection(url, config.getApiKey(), requestBody);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000); // 5 分钟读取超时

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isCompleted.get()) {
                        log.info("编排中断，取消 LLM 流式调用");
                        break;
                    }

                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }

                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> chunk = objectMapper.readValue(data, Map.class);

                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> choice = choices.get(0);
                                @SuppressWarnings("unchecked")
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                if (delta != null && delta.get("content") != null) {
                                    String content = delta.get("content").toString();
                                    outputBuilder.append(content);

                                    // 发送 content 事件
                                    sendSseEvent(emitter, "content", Map.of("text", content));
                                }
                            }

                            @SuppressWarnings("unchecked")
                            Map<String, Object> usage = (Map<String, Object>) chunk.get("usage");
                            if (usage != null) {
                                tokenUsage[0] = usage.get("prompt_tokens") != null
                                        ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
                                tokenUsage[1] = usage.get("completion_tokens") != null
                                        ? ((Number) usage.get("completion_tokens")).intValue() : 0;
                            }
                        } catch (Exception e) {
                            log.warn("解析 LLM 响应块失败: {}", e.getMessage());
                        }
                    }
                }
            }
            connection.disconnect();

        } catch (Exception e) {
            throw new RuntimeException("LLM 流式调用失败: " + e.getMessage(), e);
        }

        long llmDuration = System.currentTimeMillis() - llmStart;
        log.info("步骤 LLM 调用完成: model={}, tokens={}/{}, duration={}ms",
                modelName, tokenUsage[0], tokenUsage[1], llmDuration);

        StepLlmResult result = new StepLlmResult();
        result.output = outputBuilder.toString();
        result.tokensPrompt = tokenUsage[0];
        result.tokensCompletion = tokenUsage[1];
        result.modelName = modelName;
        result.durationMs = llmDuration;
        return result;
    }

    // ===== 辅助方法 =====

    /**
     * 解析输入映射
     * <p>支持占位符替换：${step_1.output} 引用某步骤输出，${user_message} 引用用户原始输入</p>
     */
    private String resolveInputMapping(String inputMapping, Map<Integer, String> stepOutputs, String userMessage) {
        if (inputMapping == null || inputMapping.isBlank()) {
            return userMessage;
        }
        String resolved = inputMapping;
        // 替换 ${step_N.output}
        for (Map.Entry<Integer, String> entry : stepOutputs.entrySet()) {
            String placeholder = "${step_" + entry.getKey() + ".output}";
            resolved = resolved.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        // 替换 ${user_message}
        resolved = resolved.replace("${user_message}", userMessage != null ? userMessage : "");
        return resolved;
    }

    /**
     * 评估条件表达式
     * <p>支持 ${step_N.status} == "success" 等简单条件</p>
     */
    private boolean evaluateCondition(String condition, Map<Integer, String> stepOutputs,
                                      Map<Integer, String> stepStatuses) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        String evalExpr = condition;
        // 替换 ${step_N.status}
        for (Map.Entry<Integer, String> entry : stepStatuses.entrySet()) {
            String placeholder = "${step_" + entry.getKey() + ".status}";
            evalExpr = evalExpr.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        // 简单解析：if contains "=="
        if (evalExpr.contains("==")) {
            String[] parts = evalExpr.split("==", 2);
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim().replace("\"", "");
                return left.equals(right);
            }
        }
        // 默认 true
        return true;
    }

    /**
     * 创建流式 HTTP 连接
     */
    private HttpURLConnection createStreamingConnection(String url, String apiKey,
                                                         Map<String, Object> requestBody) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        // 写入请求体
        try (java.io.OutputStream os = connection.getOutputStream()) {
            byte[] inputBytes = objectMapper.writeValueAsBytes(requestBody);
            os.write(inputBytes);
            os.flush();
        }
        return connection;
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>(data);
            event.put("type", type);
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("发送 SSE 事件失败: type={}, {}", type, e.getMessage());
        }
    }

    /**
     * 发送错误事件
     */
    private void sendSseError(SseEmitter emitter, String errorMessage) {
        sendSseEvent(emitter, "error", Map.of("errorMessage", errorMessage));
    }

    // ===== 内部结果类 =====

    /** 编排执行结果 */
    private static class OrchestrationResult {
        String output;
        int tokensPrompt;
        int tokensCompletion;
    }

    /** 单次 LLM 调用结果 */
    private static class StepLlmResult {
        String output;
        int tokensPrompt;
        int tokensCompletion;
        String modelName;
        long durationMs;
    }
}
