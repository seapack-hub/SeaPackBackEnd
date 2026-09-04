CREATE TABLE ai_user_token_usage (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  user_id     BIGINT NOT NULL COMMENT '用户ID（关联用户表）',
  usage_date  DATE   NOT NULL COMMENT '统计日期',
  tokens_used BIGINT NOT NULL DEFAULT 0 COMMENT '当日已用Token数',
  call_count  INT    NOT NULL DEFAULT 0 COMMENT '当日调用次数',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_date (user_id, usage_date),
  INDEX idx_usage_date (usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户每日Token实际使用量';