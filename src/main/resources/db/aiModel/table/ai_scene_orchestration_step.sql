-- ============================================================
-- 2. ai_scene_orchestration_step 编排步骤表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_scene_orchestration_step` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `orchestration_id` BIGINT       NOT NULL                COMMENT '关联编排ID',
  `step_index`       INT          NOT NULL                COMMENT '步骤序号（从1开始，顺序执行时按此排序）',
  `step_name`        VARCHAR(128) NOT NULL                COMMENT '步骤名称（展示用）',
  `agent_id`         BIGINT       NOT NULL                COMMENT '关联AgentID',
  `input_mapping`    VARCHAR(512) DEFAULT NULL            COMMENT '输入映射：引用上一步输出的表达式，如 ${step_1.output}，为空则使用用户原始输入',
  `condition`        VARCHAR(512) DEFAULT NULL            COMMENT '执行条件（llm_tool 模式下由 LLM 判断，其他模式忽略），支持 ${variable} 表达式',
  `retry_count`      INT          NOT NULL DEFAULT 0      COMMENT '失败重试次数',
  `timeout_ms`       INT          DEFAULT NULL            COMMENT '超时时间（毫秒），NULL 不限',
  `status`           TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
  `sort_order`       INT          NOT NULL DEFAULT 0      COMMENT '排序号',
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_orchestration_id` (`orchestration_id`),
  KEY `idx_agent_id` (`agent_id`),
  UNIQUE KEY `uk_orch_step` (`orchestration_id`, `step_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='场景编排步骤表';