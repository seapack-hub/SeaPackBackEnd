package org.seaPack.service.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.ai.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具定义转换器
 * <p>将 Skill 实体转换为 OpenAI Function Calling 的 tools 定义格式。
 * 原来 AgentSkillExecutor.selectSkillsByLLM() 通过 prompt 模拟让 LLM 返回技能选择 JSON，
 * 改用原生 Function Calling 后，由本类将技能列表转为 tools 参数，LLM 直接通过 function_call 决定调用哪个技能。</p>
 *
 * <p>转换映射：</p>
 * <ul>
 *     <li>Skill.code → function.name（唯一标识）</li>
 *     <li>Skill.description → function.description</li>
 *     <li>Skill.inputSchema → function.parameters（JSON Schema 格式，已兼容）</li>
 * </ul>
 */
@Slf4j
public class ToolDefinitionConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 Skill 列表转换为 OpenAI tools 数组格式
     *
     * @param skills 已启用的技能列表
     * @return OpenAI tools 格式的列表，可直接放入 requestBody.put("tools", ...)
     */
    public static List<Map<String, Object>> toTools(List<Skill> skills) {
        List<Map<String, Object>> tools = new ArrayList<>();
        if (skills == null || skills.isEmpty()) {
            return tools;
        }

        for (Skill skill : skills) {
            Map<String, Object> tool = toTool(skill);
            if (tool != null) {
                tools.add(tool);
            }
        }

        log.info("[ToolDefinition] 转换完成: skills={}, tools={}", skills.size(), tools.size());
        return tools;
    }

    /**
     * 将单个 Skill 转换为 OpenAI function 格式
     */
    private static Map<String, Object> toTool(Skill skill) {
        if (skill == null || skill.getCode() == null || skill.getCode().isBlank()) {
            return null;
        }

        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");

        Map<String, Object> function = new HashMap<>();
        // 使用 code 作为函数名（OpenAI 要求 function.name 只能包含 a-z, A-Z, 0-9, _, -）
        function.put("name", sanitizeFunctionName(skill.getCode()));
        function.put("description", skill.getDescription() != null ? skill.getDescription() : skill.getName());

        // inputSchema 已经是 JSON Schema 格式，直接解析为 Map
        if (skill.getInputSchema() != null && !skill.getInputSchema().isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> schema = objectMapper.readValue(skill.getInputSchema(), Map.class);
                function.put("parameters", schema);
            } catch (Exception e) {
                log.warn("[ToolDefinition] 解析 inputSchema 失败: skillCode={}, error={}",
                        skill.getCode(), e.getMessage());
                function.put("parameters", defaultParameters(skill.getName()));
            }
        } else {
            function.put("parameters", defaultParameters(skill.getName()));
        }

        tool.put("function", function);
        return tool;
    }

    /**
     * 清洗函数名：OpenAI function.name 只允许 [a-zA-Z0-9_-]，最长 64 字符
     * <p>将 code 中的特殊字符替换为下划线。</p>
     */
    private static String sanitizeFunctionName(String code) {
        String name = code.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (name.length() > 64) {
            name = name.substring(0, 64);
        }
        return name;
    }

    /**
     * 当 inputSchema 为空时，生成默认的 JSON Schema（接受任意 query 字符串）
     */
    private static Map<String, Object> defaultParameters(String skillName) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "query", Map.of(
                        "type", "string",
                        "description", "用户关于「" + skillName + "」的查询内容"
                )
        ));
        schema.put("required", List.of("query"));
        return schema;
    }
}
