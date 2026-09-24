package org.seaPack.service.ai.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.AgentTraceStep;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * 编排策略执行基类
 * <p>提供共享的结果类、SSE 发送、输入解析、条件评估等工具方法。
 * 各策略子类只需实现 {@link #execute} 方法。</p>
 */
@Slf4j
public abstract class OrchestrationStrategyHandler {

    // ===== 共享结果类 =====

    /** 编排执行结果 */
    public static class OrchestrationResult {
        public String output = "";
        public int tokensPrompt = 0;
        public int tokensCompletion = 0;
        public List<AgentTraceStep> steps = new ArrayList<>();
    }

    // ===== SSE 发送工具 =====

    /** 发送 SSE 事件 */
    public static void sendSseEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        SseEvent.send(emitter, type, data);
    }

    /** 发送错误事件 */
    public static void sendSseError(SseEmitter emitter, String errorMessage) {
        sendSseEvent(emitter, "error", Map.of("errorMessage", errorMessage));
    }

    // ===== 输入解析工具 =====

    /**
     * 根据 inputMode 解析节点输入
     */
    public static String resolveInput(SceneOrchestrationStep step, Map<Integer, String> stepOutputs, String userMessage) {
        String inputMode = step.getInputMode() != null ? step.getInputMode() : "user_input";
        switch (inputMode) {
            case "prev_output":
                Integer prevIndex = findPrevStepWithOutput(step.getStepIndex(), stepOutputs);
                if (prevIndex != null) {
                    return stepOutputs.get(prevIndex);
                }
                return userMessage;
            case "shared_state":
                if (step.getInputMapping() != null && step.getInputMapping().startsWith("state:")) {
                    String key = step.getInputMapping().substring(6);
                    return "[shared_state:" + key + "]";
                }
                return userMessage;
            case "supervisor_instruction":
                if (step.getInputMapping() != null) {
                    return step.getInputMapping();
                }
                return userMessage;
            default: // user_input
                return resolveInputMapping(step.getInputMapping(), stepOutputs, userMessage);
        }
    }

    /**
     * 查找当前步骤之前最近一个有输出的步骤
     */
    public static Integer findPrevStepWithOutput(int currentStepIndex, Map<Integer, String> stepOutputs) {
        Integer best = null;
        for (Integer idx : stepOutputs.keySet()) {
            if (idx < currentStepIndex && (best == null || idx > best)) {
                best = idx;
            }
        }
        return best;
    }

    /**
     * 获取下一个要执行的步骤的 stepIndex
     */
    public static Integer getNextStepIndex(SceneOrchestrationStep currentStep, Map<Integer, SceneOrchestrationStep> stepMap) {
        List<Integer> sortedKeys = new ArrayList<>(stepMap.keySet());
        Collections.sort(sortedKeys);
        int pos = sortedKeys.indexOf(currentStep.getStepIndex());
        if (pos >= 0 && pos < sortedKeys.size() - 1) {
            return sortedKeys.get(pos + 1);
        }
        return null;
    }

    /**
     * 解析输入映射（旧方式兼容）
     * <p>支持占位符替换：${step_N.output} 引用某步骤输出，${user_message} 引用用户原始输入</p>
    */
    public static String resolveInputMapping(String inputMapping, Map<Integer, String> stepOutputs, String userMessage) {
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
    public static boolean evaluateCondition(String condition, Map<Integer, String> stepOutputs,
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

    // ===== 抽象方法 =====

    /**
     * 执行编排策略
     *
     * @param params 执行参数
     * @return 执行结果
     */
    public abstract OrchestrationResult execute(ExecuteParams params);

    /**
     * 执行参数封装
     */
    public static class ExecuteParams {
        public List<SceneOrchestrationStep> steps;
        public org.seaPack.dto.ai.OrchestrationExecuteRequest request;
        public org.seaPack.config.AIProperties.ProviderConfig config;
        public SseEmitter emitter;
        public java.util.concurrent.atomic.AtomicBoolean isCompleted;
        public Long userId;
        /** 编排实体（Supervisor/Crew/Dynamic 模式需要） */
        public org.seaPack.model.ai.SceneOrchestration orchestration;
    }
}
