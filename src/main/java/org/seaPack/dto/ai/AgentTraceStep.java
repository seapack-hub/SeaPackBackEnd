package org.seaPack.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 链路追踪步骤 DTO
 * <p>记录 Agent 调用链路中的单个步骤（提示词组装、知识库检索、技能调用、LLM 调用）。</p>
 */
@Data
public class AgentTraceStep {

    /** 步骤序号（从1开始） */
    private Integer stepIndex;

    /** 步骤类型：prompt_assembly / knowledge_retrieval / skill_execution / llm_call */
    private String stepType;

    /** 步骤名称 */
    private String stepName;

    /** 步骤状态：success / fail / skip */
    private String status;

    /** 步骤耗时（毫秒） */
    private Long durationMs;

    /** 步骤输入 */
    private Object input;

    /** 步骤输出 */
    private Object output;

    /** 步骤元数据 */
    private Map<String, Object> metadata;
}
