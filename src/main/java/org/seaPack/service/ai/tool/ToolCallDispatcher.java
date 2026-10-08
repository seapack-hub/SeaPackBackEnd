package org.seaPack.service.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.SseEvent;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.model.ai.Skill;
import org.seaPack.service.ai.handler.SkillExecutionResult;
import org.seaPack.service.ai.handler.SkillHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * 工具调用分发器
 * <p>
 * 接收 LLM 通过 Function Calling 返回的 tool_calls，根据 function.name（即 Skill.code）
 * 匹配数据库中的 Skill 实体，路由到对应的 SkillHandler 执行，并将执行结果转换为
 * OpenAI tool role 消息格式，供下一轮 LLM 调用消费。
 * </p>
 *
 * <p>
 * 替代了原 AgentSkillExecutor.extractParamsByLLM() 中通过 prompt 提取参数的逻辑：
 * Function Calling 模式下，LLM 直接返回结构化的 arguments，不再需要 prompt 模拟。
 * </p>
 */
@Slf4j
@Component
public class ToolCallDispatcher {

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private List<SkillHandler> handlers;

    @Autowired
    private FileArtifactCollector fileArtifactCollector;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行一组 tool_calls，返回 OpenAI 格式的 tool role 消息列表
     *
     * @param toolCalls LLM 返回的 tool_calls 数组（每个元素含 id + function）
     * @param authToken 认证 Token（内部 API 调用时转发）
     * @param emitter   SSE 发射器（可选，用于推送执行进度）
     * @return tool role 的消息列表，可直接拼入 messages 继续对话
     */
    public List<Map<String, String>> dispatch(List<Map<String, Object>> toolCalls,
            String authToken, SseEmitter emitter, int stepIndex) {
        return dispatch(toolCalls, authToken, emitter, stepIndex, null);
    }

    /**
     * 执行一组 tool_calls（含编排步骤归属字段）
     *
     * @param orchestrationStepIndex 编排步骤序号（可空；编排场景下前端据此把事件归属到对应步骤卡片）
     */
    public List<Map<String, String>> dispatch(List<Map<String, Object>> toolCalls,
            String authToken, SseEmitter emitter, int stepIndex, Integer orchestrationStepIndex) {
        List<Map<String, String>> toolMessages = new ArrayList<>();

        if (toolCalls == null || toolCalls.isEmpty()) {
            return toolMessages;
        }

        for (Map<String, Object> toolCall : toolCalls) {
            String toolCallId = (String) toolCall.get("id");

            // 解析 function 字段
            @SuppressWarnings("unchecked")
            Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
            if (function == null) {
                toolMessages.add(buildToolMessage(toolCallId,
                        "{\"error\": \"tool_call 缺少 function 字段\"}"));
                continue;
            }

            String functionName = (String) function.get("name"); // 即 skill.code
            String argumentsJson = (String) function.get("arguments"); // JSON 字符串

            log.info("[ToolCall] id={}, function={}, arguments={}", toolCallId, functionName, argumentsJson);

            // 1. 根据 code 查找 Skill
            Skill skill = skillMapper.selectByCode(functionName);
            if (skill == null) {
                log.warn("[ToolCall] 未找到技能: code={}", functionName);
                toolMessages.add(buildToolMessage(toolCallId,
                        "{\"error\": \"技能不存在: " + functionName + "\"}"));
                continue;
            }
            if (skill.getStatus() == null || skill.getStatus() != 1) {
                log.warn("[ToolCall] 技能已禁用: code={}, name={}", functionName, skill.getName());
                toolMessages.add(buildToolMessage(toolCallId,
                        "{\"error\": \"技能已禁用: " + skill.getName() + "\"}"));
                continue;
            }

            // 2. 解析 LLM 返回的参数（已是结构化 JSON，不需要 prompt 提取）
            Map<String, Object> params = parseArguments(argumentsJson);

            // 3. 路由到对应的 SkillHandler
            SkillHandler handler = resolveHandler(skill.getSkillType());
            if (handler == null) {
                log.warn("[ToolCall] 未找到处理器: skillType={}", skill.getSkillType());
                toolMessages.add(buildToolMessage(toolCallId,
                        "{\"error\": \"不支持的技能类型: " + skill.getSkillType() + "\"}"));
                continue;
            }

            // 4. 发送 step_detail 事件：技能参数
            if (emitter != null) {
                Map<String, Object> paramsDetail = new LinkedHashMap<>();
                paramsDetail.put("type", "step_detail");
                paramsDetail.put("stepIndex", stepIndex);
                paramsDetail.put("detailType", "skill_params");
                paramsDetail.put("skillName", skill.getName());
                paramsDetail.put("skillCode", skill.getCode());
                paramsDetail.put("params", params);
                if (orchestrationStepIndex != null) {
                    paramsDetail.put("orchestrationStepIndex", orchestrationStepIndex);
                }
                SseEvent.send(emitter, "step_detail", paramsDetail);
            }

            // 5. 执行技能
            try {
                long start = System.currentTimeMillis();
                SkillExecutionResult result = handler.execute(skill, params, authToken, emitter);
                long durationMs = System.currentTimeMillis() - start;

                // HTTP 状态码 >= 400 视为调用失败：给 LLM 明确的错误结果而非原始错误体，
                // 避免 LLM 将 403 等错误响应误当作业务数据（如把 error=Forbidden 当查询结果）
                boolean httpFailed = result.getStatusCode() >= 400;
                if (httpFailed) {
                    String errorJson = buildHttpErrorResult(result);
                    toolMessages.add(buildToolMessage(toolCallId, errorJson));
                    log.warn("[ToolCall] 技能调用失败: name={}, status={}ms, status={}, url={}",
                            skill.getName(), result.getStatusCode(), durationMs, result.getUrl());
                } else {
                    String resultJson = serializeResult(result);
                    toolMessages.add(buildToolMessage(toolCallId, resultJson));
                    log.info("[ToolCall] 技能执行成功: name={}, duration={}ms", skill.getName(), durationMs);
                }

                // 发送 step_detail 事件：技能结果（失败时标记 failed，前端时间线可见）
                if (emitter != null) {
                    Map<String, Object> resultDetail = new LinkedHashMap<>();
                    resultDetail.put("type", "step_detail");
                    resultDetail.put("stepIndex", stepIndex);
                    resultDetail.put("detailType", "skill_result");
                    resultDetail.put("skillName", skill.getName());
                    resultDetail.put("skillCode", skill.getCode());
                    resultDetail.put("httpMethod", result.getHttpMethod());
                    resultDetail.put("url", result.getUrl());
                    resultDetail.put("status", httpFailed ? "failed" : "success");
                    resultDetail.put("durationMs", durationMs);
                    if (orchestrationStepIndex != null) {
                        resultDetail.put("orchestrationStepIndex", orchestrationStepIndex);
                    }
                    if (httpFailed) {
                        resultDetail.put("errorMessage", "HTTP " + result.getStatusCode()
                                + (authToken == null || authToken.isBlank() ? "（未携带认证令牌）" : ""));
                    } else {
                        resultDetail.put("resultPreview", truncatePreview(result.getBody()));
                        // 文件类结果：附带结构化字段，前端时间线据此渲染文件卡片
                        if ("file".equals(result.getOutputType()) && result.getBody() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> fileBody = (Map<String, Object>) result.getBody();
                            resultDetail.put("fileUrl", fileBody.getOrDefault("url", ""));
                            resultDetail.put("fileName", fileBody.getOrDefault("fileName", ""));
                            resultDetail.put("fileSize", fileBody.getOrDefault("fileSize", 0));
                        }
                    }
                    SseEvent.send(emitter, "step_detail", resultDetail);
                }

                // 文件类结果：收集产物 + 推送 file_generated 事件（前端据此渲染文件卡片/下载入口）
                if (!httpFailed && "file".equals(result.getOutputType())
                        && result.getBody() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fileBody = (Map<String, Object>) result.getBody();
                    Object urlVal = fileBody.get("url");
                    String fileUrl = urlVal != null ? urlVal.toString() : "";
                    if (fileUrl.isBlank()) {
                        // 空 URL 不收集、不推送，防止前端渲染无效文件卡片（假成功防线）
                        log.warn("[ToolCall] 文件结果缺少 url，跳过产物收集: name={}", skill.getName());
                    } else {
                        Map<String, Object> fileInfo = new LinkedHashMap<>();
                        fileInfo.put("url", fileUrl);
                        fileInfo.put("fileName", fileBody.getOrDefault("fileName", "generated_file"));
                        fileInfo.put("fileSize", fileBody.getOrDefault("fileSize", 0));
                        fileInfo.put("skillName", skill.getName());
                        if (orchestrationStepIndex != null) {
                            fileInfo.put("orchestrationStepIndex", orchestrationStepIndex);
                        }
                        fileArtifactCollector.record(emitter, fileInfo);

                        if (emitter != null) {
                            Map<String, Object> fileEvent = new LinkedHashMap<>(fileInfo);
                            fileEvent.put("stepIndex", stepIndex);
                            SseEvent.send(emitter, SseEvent.TYPE_FILE_GENERATED, fileEvent);
                        }
                    }
                }

                // 增加技能使用次数
                skillMapper.incrementUseCount(skill.getId());

            } catch (Exception e) {
                log.error("[ToolCall] 技能执行异常: name={}, error={}", skill.getName(), e.getMessage(), e);
                toolMessages.add(buildToolMessage(toolCallId,
                        "{\"error\": \"技能执行失败: " + escapeJson(e.getMessage()) + "\"}"));

                // 发送 step_detail 事件：技能失败
                if (emitter != null) {
                    Map<String, Object> errorDetail = new LinkedHashMap<>();
                    errorDetail.put("type", "step_detail");
                    errorDetail.put("stepIndex", stepIndex);
                    errorDetail.put("detailType", "skill_result");
                    errorDetail.put("skillName", skill.getName());
                    errorDetail.put("skillCode", skill.getCode());
                    errorDetail.put("status", "failed");
                    errorDetail.put("errorMessage", e.getMessage());
                    SseEvent.send(emitter, "step_detail", errorDetail);
                }
            }
        }

        log.info("[ToolCall] 分发完成: total={}, results={}", toolCalls.size(), toolMessages.size());
        return toolMessages;
    }

    /**
     * 根据 skillType 查找对应的 SkillHandler
     */
    private SkillHandler resolveHandler(String skillType) {
        if (skillType == null || skillType.isBlank()) {
            return null;
        }
        return handlers.stream()
                .filter(h -> h.getSkillType().equals(skillType))
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析 LLM 返回的 arguments JSON 字符串
     */
    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(argumentsJson, Map.class);
            return params != null ? params : new HashMap<>();
        } catch (Exception e) {
            log.warn("[ToolCall] 解析 arguments 失败: json={}, error={}", argumentsJson, e.getMessage());
            // 降级：将原始字符串作为 query 参数
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("query", argumentsJson);
            return fallback;
        }
    }

    /**
     * 构建 HTTP 错误结果 JSON（供 LLM 消费）
     * <p>
     * 明确标注失败原因与可行动提示，避免 LLM 把错误响应体当业务数据处理。
     * </p>
     */
    private String buildHttpErrorResult(SkillExecutionResult result) {
        try {
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("success", false);
            errorMap.put("error", "接口调用失败: HTTP " + result.getStatusCode());
            errorMap.put("url", result.getUrl());
            if (result.getStatusCode() == 401 || result.getStatusCode() == 403) {
                errorMap.put("hint", "认证或权限不足，请勿重试该工具，直接向用户说明无法获取该数据");
            } else if (result.getStatusCode() >= 500) {
                errorMap.put("hint", "服务端异常，请勿重试该工具，直接向用户说明服务暂时不可用");
            }
            return objectMapper.writeValueAsString(errorMap);
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"接口调用失败: HTTP " + result.getStatusCode() + "\"}";
        }
    }

    /**
     * 将 SkillExecutionResult 序列化为 JSON 字符串（作为 tool 消息的 content）
     */
    private String serializeResult(SkillExecutionResult result) {
        try {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("status", result.getStatusCode());
            resultMap.put("type", result.getOutputType());
            resultMap.put("durationMs", result.getDurationMs());
            if (result.getBody() != null) {
                resultMap.put("data", result.getBody());
            }
            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            return "{\"data\": \"" + result.getBody() + "\"}";
        }
    }

    /**
     * 构建 OpenAI tool role 消息
     */
    private Map<String, String> buildToolMessage(String toolCallId, String content) {
        Map<String, String> msg = new LinkedHashMap<>();
        msg.put("role", "tool");
        msg.put("tool_call_id", toolCallId);
        msg.put("content", content);
        return msg;
    }

    /**
     * 转义 JSON 字符串中的特殊字符（防止注入）
     */
    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 截断结果预览（避免 SSE 事件过大）
     */
    private String truncatePreview(Object body) {
        if (body == null)
            return "";
        String str = body.toString();
        return str.length() > 500 ? str.substring(0, 500) + "..." : str;
    }
}
