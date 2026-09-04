-- ============================================================
-- 5. 工作流调度表（可选，用于定时触发）
-- ============================================================

CREATE TABLE workflow_schedule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workflow_id BIGINT NOT NULL COMMENT '工作流ID',
  name VARCHAR(100) COMMENT '调度名称',
  description TEXT COMMENT '调度描述',
  
  -- 调度配置
  schedule_type VARCHAR(20) NOT NULL COMMENT '调度类型: cron/interval/once',
  cron_expression VARCHAR(100) COMMENT 'Cron表达式',
  interval_seconds INT COMMENT '间隔秒数',
  scheduled_time DATETIME COMMENT '定时执行时间',
  
  -- 输入参数
  input_params JSON COMMENT '每次执行的输入参数',
  
  -- 状态
  status TINYINT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
  last_run_at DATETIME COMMENT '上次执行时间',
  next_run_at DATETIME COMMENT '下次执行时间',
  run_count INT DEFAULT 0 COMMENT '已执行次数',
  
  -- 元数据
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id) ON DELETE CASCADE,
  INDEX idx_workflow_id (workflow_id),
  INDEX idx_status (status),
  INDEX idx_next_run_at (next_run_at)
) COMMENT '工作流调度表';