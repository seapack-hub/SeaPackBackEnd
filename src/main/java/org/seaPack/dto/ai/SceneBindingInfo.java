package org.seaPack.dto.ai;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 场景全量绑定信息 DTO
 * <p>用于 /ai/scenes/bindings/all 接口返回，JOIN 多表组装。</p>
 */
@Data
public class SceneBindingInfo {

    /** 场景ID */
    private Long sceneId;

    /** 场景名称 */
    private String sceneName;

    /** 场景编码 */
    private String sceneCode;

    /** 前端模块标识 */
    private String moduleKey;

    /** 位置标识 */
    private String positionKey;

    /** 默认 Agent ID */
    private Long agentId;

    /** Agent 名称 */
    private String agentName;

    /** Agent 编码 */
    private String agentCode;

    /** 部署配置（JSON） */
    private String deploymentConfig;

    /** 是否该位置的默认场景 */
    private Integer isDefault;

    /** 部署状态 */
    private Integer status;

    /** 关联知识库 ID 列表 */
    private List<Long> knowledgeIds;

    /** 场景级覆盖模型编码 */
    private String agentModel;

    /** 场景级覆盖温度 */
    private BigDecimal agentTemperature;

    /** 场景级覆盖最大 Token */
    private Integer agentMaxTokens;

    /** 场景级 System Prompt 追加 */
    private String agentSystemPrompt;

    /** 场景级输出格式 */
    private String agentOutputFormat;

    /** 场景级上下文限制 Token 数 */
    private Integer agentContextLimit;
}
