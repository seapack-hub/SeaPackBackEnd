-- ============================================================
-- 2. ai_scene_orchestration_step 编排步骤/节点表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_scene_orchestration_step` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `orchestration_id`  BIGINT       NOT NULL                COMMENT '关联编排ID',
  `step_index`        INT          NOT NULL                COMMENT '步骤序号（从1开始）',
  `step_name`         VARCHAR(128) NOT NULL                COMMENT '步骤名称（展示用）',
  `node_type`         VARCHAR(20)  NOT NULL DEFAULT 'agent' COMMENT '节点类型：agent-执行Agent | condition-条件判断 | aggregate-结果汇总 | handoff-交接给指定Agent',
  `agent_id`          BIGINT       NULL DEFAULT NULL       COMMENT '关联Agent ID（node_type=agent时必填）',
  `input_mode`        VARCHAR(20)  NOT NULL DEFAULT 'user_input' COMMENT '输入来源：user_input-用户原始输入 | prev_output-上一步输出 | shared_state-共享状态 | supervisor_instruction-Supervisor指令',
  `input_mapping`     VARCHAR(512) NULL DEFAULT NULL       COMMENT '输入映射表达式：${step_1.output} / ${state.key}，兼容旧版',
  `output_target`     VARCHAR(20)  NOT NULL DEFAULT 'next_step' COMMENT '输出去向：next_step-下一步 | supervisor-回传Supervisor | shared_state-写入共享状态 | all_peers-广播给所有同轮Agent',
  `condition`         VARCHAR(512) NULL DEFAULT NULL       COMMENT '执行条件表达式（condition节点或步骤级条件），支持 ${variable} 占位符',
  `branch_true_step`  INT          NULL DEFAULT NULL       COMMENT '条件为真时跳转到的 step_index（仅 condition 节点有效）',
  `branch_false_step` INT          NULL DEFAULT NULL       COMMENT '条件为假时跳转到的 step_index（仅 condition 节点有效）',
  `description`       VARCHAR(500) NULL DEFAULT NULL       COMMENT '节点描述，供动态规划时 LLM 理解节点用途',
  `retry_count`       INT          NOT NULL DEFAULT 0      COMMENT '失败重试次数',
  `timeout_ms`        INT          NULL DEFAULT NULL       COMMENT '超时时间（毫秒），NULL 不限',
  `status`            TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
  `sort_order`        INT          NOT NULL DEFAULT 0      COMMENT '排序号',
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_orch_step`(`orchestration_id` ASC, `step_index` ASC) USING BTREE,
  INDEX `idx_orchestration_id`(`orchestration_id` ASC) USING BTREE,
  INDEX `idx_agent_id`(`agent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '场景编排步骤/节点表' ROW_FORMAT = Dynamic;
