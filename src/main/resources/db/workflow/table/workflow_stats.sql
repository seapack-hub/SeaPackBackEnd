-- ============================================================
-- 6. 工作流执行统计表（可选，用于Dashboard）
-- ============================================================

CREATE TABLE workflow_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workflow_id BIGINT NOT NULL COMMENT '工作流ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  
  -- 执行统计
  total_runs INT DEFAULT 0 COMMENT '总执行次数',
  success_count INT DEFAULT 0 COMMENT '成功次数',
  failed_count INT DEFAULT 0 COMMENT '失败次数',
  running_count INT DEFAULT 0 COMMENT '运行中次数',
  
  -- 耗时统计
  avg_duration_ms INT COMMENT '平均耗时（毫秒）',
  max_duration_ms INT COMMENT '最大耗时（毫秒）',
  min_duration_ms INT COMMENT '最小耗时（毫秒）',
  
  -- Token统计（AI技能节点）
  total_tokens INT DEFAULT 0 COMMENT '总Token消耗',
  
  -- 人工任务统计
  human_tasks_count INT DEFAULT 0 COMMENT '人工任务数',
  human_tasks_avg_minutes INT COMMENT '人工任务平均处理时间（分钟）',
  
  FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id),
  UNIQUE KEY uk_workflow_date (workflow_id, stat_date)
) COMMENT '工作流执行统计表';
