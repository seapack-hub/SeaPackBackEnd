-- ============================================================
-- Token 调用明细表（每次 LLM 调用一行记录）
-- ============================================================
CREATE TABLE ai_token_usage_log (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

  -- 调用信息
  call_time         DATETIME NOT NULL COMMENT '调用时间',
  model_name        VARCHAR(64) NOT NULL COMMENT '模型编码（如 gpt-4o、deepseek-chat）',
  tokens_input      INT NOT NULL DEFAULT 0 COMMENT '输入 Token 数',
  tokens_output     INT NOT NULL DEFAULT 0 COMMENT '输出 Token 数',
  duration_ms       INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
  cost_yuan         DECIMAL(12,6) DEFAULT NULL COMMENT '费用（元）',
  status            VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '状态：success / fail',

  -- 归属信息（谁、在做什么）
  user_id           BIGINT NOT NULL COMMENT '用户ID（关联 sys_user.id）',
  biz_type          VARCHAR(30) NOT NULL COMMENT '用途：orchestration-编排 / agent-Agent对话 / chat-通用对话 / skill-Skill执行',
  scene_id          BIGINT DEFAULT NULL COMMENT '场景ID（关联 ai_scene.id）',
  agent_id          BIGINT DEFAULT NULL COMMENT 'Agent ID（关联 ai_agent.id）',
  skill_id          BIGINT DEFAULT NULL COMMENT 'Skill ID（关联 ai_skill.id）',
  orchestration_step INT DEFAULT NULL COMMENT '编排步骤序号（biz_type=orchestration 时有效）',

  -- 关联信息
  session_id        BIGINT DEFAULT NULL COMMENT '关联 ai_execution_session.id',
  request_id        VARCHAR(64) DEFAULT NULL COMMENT '前端生成的请求ID（用于追踪单轮对话）',
  error_message     TEXT DEFAULT NULL COMMENT '错误信息（status=fail 时记录）',

  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  -- 索引
  INDEX idx_call_time (call_time),
  INDEX idx_user_id (user_id),
  INDEX idx_scene_id (scene_id),
  INDEX idx_agent_id (agent_id),
  INDEX idx_skill_id (skill_id),
  INDEX idx_model_name (model_name),
  INDEX idx_biz_type (biz_type),
  INDEX idx_session_id (session_id),
  INDEX idx_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Token 调用明细表（每次 LLM 调用一行）';
