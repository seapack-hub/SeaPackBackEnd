CREATE TABLE ai_token_usage_daily (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

  stat_date         DATE NOT NULL COMMENT '统计日期',
  user_id           BIGINT NOT NULL COMMENT '用户ID',
  model_name        VARCHAR(64) NOT NULL COMMENT '模型编码',
  biz_type          VARCHAR(30) NOT NULL COMMENT '用途：orchestration / agent / chat / skill',
  scene_id          BIGINT DEFAULT NULL COMMENT '场景ID',
  agent_id          BIGINT DEFAULT NULL COMMENT 'Agent ID',
  skill_id          BIGINT DEFAULT NULL COMMENT 'Skill ID',

  -- 聚合指标
  call_count        INT NOT NULL DEFAULT 0 COMMENT '调用次数',
  success_count     INT NOT NULL DEFAULT 0 COMMENT '成功次数',
  fail_count        INT NOT NULL DEFAULT 0 COMMENT '失败次数',
  tokens_input      BIGINT NOT NULL DEFAULT 0 COMMENT '输入 Token 总数',
  tokens_output     BIGINT NOT NULL DEFAULT 0 COMMENT '输出 Token 总数',
  tokens_total      BIGINT NOT NULL DEFAULT 0 COMMENT '总 Token 数',
  total_duration_ms BIGINT NOT NULL DEFAULT 0 COMMENT '总耗时（毫秒）',
  total_cost_yuan   DECIMAL(14,6) NOT NULL DEFAULT 0 COMMENT '总费用（元）',

  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 唯一键：直接包含列名，NULL值不参与唯一性校验
  UNIQUE KEY uk_daily_stat (stat_date, user_id, model_name, biz_type, scene_id, agent_id, skill_id),

  -- 常用查询索引
  INDEX idx_stat_date (stat_date),
  INDEX idx_user_date (user_id, stat_date),
  INDEX idx_scene_date (scene_id, stat_date),
  INDEX idx_agent_date (agent_id, stat_date),
  INDEX idx_model_date (model_name, stat_date),
  INDEX idx_biz_type_date (biz_type, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Token 日统计汇总表';