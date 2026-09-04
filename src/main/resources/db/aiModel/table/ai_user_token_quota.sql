CREATE TABLE ai_user_token_quota (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  user_id        BIGINT      NOT NULL COMMENT '用户ID（关联用户表）',
  quota_type     VARCHAR(20) NOT NULL DEFAULT 'daily'
                 COMMENT '配额类型：daily-日额度 / monthly-月额度 / total-总额度',
  quota_limit    BIGINT      NOT NULL COMMENT '额度上限（token数），0表示不限制',
  alert_threshold INT        NOT NULL DEFAULT 80
                 COMMENT '预警阈值（百分比，如80表示用量达80%时告警）',
  is_enabled     TINYINT     NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
  status         VARCHAR(20) NOT NULL DEFAULT 'normal'
                 COMMENT '状态：normal-正常 / exceeded-已超限 / disabled-已禁用',
  start_date     DATE        NOT NULL DEFAULT '9999-12-31'
                 COMMENT '配额周期开始日期（daily=当天 / monthly=当月1号 / total=9999-12-31）',
  end_date       DATE        DEFAULT NULL
                 COMMENT '配额周期结束日期（monthly=当月最后一天 / daily、total为NULL）',
  created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_quota_type (user_id, quota_type, start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户Token配额表';