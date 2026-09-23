package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.SkillExecuteResult;
import org.seaPack.mapper.ai.AgentSkillMapper;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.model.ai.AgentSkill;
import org.seaPack.model.ai.Skill;
import org.seaPack.service.ai.tool.ToolCallDispatcher;
import org.seaPack.service.ai.tool.ToolDefinitionConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 技能执行引擎
 * <p>提供两大能力：</p>
 * <ul>
 *     <li>{@link #getToolDefinitions(Long)}: 将 Agent 关联技能转换为 OpenAI Function Calling 的 tools 定义</li>
 *     <li>{@link #executeToolCalls(List, String, SseEmitter, int)}: 执行 LLM 返回的 tool_calls，委托给 {@link ToolCallDispatcher}</li>
 * </ul>
 *
 * <p>改造说明：</p>
 * <p>原实现通过 selectSkillsByLLM() + extractParamsByLLM() 用两次 prompt 模拟 LLM 的工具选择和参数提取，
 * 现改用 OpenAI 原生 Function Calling：LLM 在一次调用中同时决定"调用哪个工具"和"传什么参数"，
 * 由本类负责暴露工具定义、接收 tool_calls 并分发执行。</p>
 */
@Slf4j
@Component
public class AgentSkillExecutor {

    @Autowired
    private AgentSkillMapper agentSkillMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private ToolCallDispatcher toolCallDispatcher;

    @Autowired
    private AIProperties aiProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========================================================================
    //  核心方法：暴露工具定义
    // ========================================================================

    /**
     * 获取 Agent 关联技能的 Function Calling tools 定义
     * <p>将数据库中的 Skill 实体转换为 OpenAI tools 格式，用于 LLM 调用时传入 tools 参数。
     * LLM 收到这些工具定义后，会根据用户意图自动决定是否调用、调用哪个、传什么参数。</p>
     *
     * @param agentId Agent ID
     * @return OpenAI tools 格式的列表（List of {type:"function", function:{name, description, parameters}}）
     */
    public List<Map<String, Object>> getToolDefinitions(Long agentId) {
        // 1. 获取 Agent 关联的已启用技能
        List<AgentSkill> enabledSkills = agentSkillMapper.selectByAgentId(agentId).stream()
                .filter(s -> s.getEnabled() != null && s.getEnabled() == 1)
                .sorted(Comparator.comparingInt(s -> s.getSortOrder() != null ? s.getSortOrder() : 0))
                .collect(Collectors.toList());

        if (enabledSkills.isEmpty()) {
            log.info("[技能工具] AgentId={} 无已启用技能", agentId);
            return Collections.emptyList();
        }

        // 2. 加载 Skill 实体
        List<Skill> skills = new ArrayList<>();
        for (AgentSkill as : enabledSkills) {
            Skill skill = skillMapper.selectById(as.getSkillId());
            if (skill != null && skill.getStatus() != null && skill.getStatus() == 1
                    && skill.getEndpoint() != null && !skill.getEndpoint().isBlank()) {
                skills.add(skill);
            }
        }

        log.info("[技能工具] AgentId={}, 已启用={}, 有效={}", agentId, enabledSkills.size(), skills.size());

        // 3. 转换为 OpenAI tools 格式
        return ToolDefinitionConverter.toTools(skills);
    }

    // ========================================================================
    //  核心方法：执行工具调用
    // ========================================================================

    /**
     * 执行 LLM 返回的 tool_calls
     * <p>由 AgentTestChatService 在收到 LLM 的 tool_calls 响应后调用，
     * 委托给 {@link ToolCallDispatcher} 路由到对应的 SkillHandler 执行。</p>
     *
     * @param toolCalls  LLM 返回的 tool_calls 数组
     * @param authToken  认证 Token（内部 API 调用时转发）
     * @param emitter    SSE 发射器（用于推送执行进度）
     * @return tool role 消息列表，可直接拼入 messages 继续对话
     */
    public List<Map<String, String>> executeToolCalls(
            List<Map<String, Object>> toolCalls, String authToken, SseEmitter emitter, int stepIndex) {

        log.info("[技能工具] 收到 tool_calls: count={}", toolCalls.size());

        // 打印 tool_calls 详情（调试用）
        for (Map<String, Object> tc : toolCalls) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fn = (Map<String, Object>) tc.get("function");
            if (fn != null) {
                log.info("[技能工具]   -> id={}, name={}, arguments={}",
                        tc.get("id"), fn.get("name"), fn.get("arguments"));
            }
        }

        return toolCallDispatcher.dispatch(toolCalls, authToken, emitter, stepIndex);
    }

    // ========================================================================
    //  向后兼容：旧版 executeSkills 方法（已废弃，保留签名以避免编译错误）
    // ========================================================================

    /**
     * @deprecated 改用 {@link #getToolDefinitions(Long)} + {@link #executeToolCalls(List, String, SseEmitter)} 组合。
     *             原方法通过 prompt 模拟 LLM 选择技能和提取参数，现已改为原生 Function Calling。
     */
    @Deprecated
    public SkillExecuteResult executeSkills(Long agentId, String userMessage) {
        throw new UnsupportedOperationException(
                "executeSkills 已废弃，请使用 getToolDefinitions() + executeToolCalls() 组合（原生 Function Calling）");
    }

    /**
     * @deprecated 同上
     */
    @Deprecated
    public SkillExecuteResult executeSkills(Long agentId, String userMessage, String authToken) {
        throw new UnsupportedOperationException(
                "executeSkills 已废弃，请使用 getToolDefinitions() + executeToolCalls() 组合（原生 Function Calling）");
    }

    /**
     * @deprecated 同上
     */
    @Deprecated
    public SkillExecuteResult executeSkills(Long agentId, String userMessage, String authToken, SseEmitter emitter) {
        throw new UnsupportedOperationException(
                "executeSkills 已废弃，请使用 getToolDefinitions() + executeToolCalls() 组合（原生 Function Calling）");
    }

    /**
     * @deprecated 同上
     */
    @Deprecated
    public SkillExecuteResult executeSkills(Long agentId, String userMessage, String authToken, SseEmitter emitter, int stepIndex) {
        throw new UnsupportedOperationException(
                "executeSkills 已废弃，请使用 getToolDefinitions() + executeToolCalls() 组合（原生 Function Calling）");
    }
}
