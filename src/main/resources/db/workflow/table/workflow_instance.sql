-- 2.1 工作流执行实例表
CREATE TABLE workflow_instance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workflow_id BIGINT NOT NULL COMMENT '工作流定义ID',
  workflow_version INT COMMENT '执行时的工作流版本',
  workflow_name VARCHAR(100) COMMENT '工作流名称（冗余，便于查询）',
  
  -- 定义快照（防止运行中定义变化导致问题）
  definition_snapshot JSON COMMENT '完整工作流定义快照',
  
  -- 执行状态
  status TINYINT DEFAULT 0 COMMENT '状态: 0=待执行, 1=运行中, 2=已完成, 3=失败, 4=暂停, 5=已取消',
  trigger_type VARCHAR(20) COMMENT '触发类型: manual/api/schedule/event',
  
  -- 输入/输出
  input_params JSON COMMENT '输入变量数据',
  output_result JSON COMMENT '输出变量数据',
  
  -- 执行进度
  current_node_id VARCHAR(100) COMMENT '当前执行到的节点ID',
  completed_nodes JSON COMMENT '已完成的节点ID列表',
  total_nodes INT DEFAULT 0 COMMENT '总节点数',
  completed_count INT DEFAULT 0 COMMENT '已完成节点数',
  
  -- 时间信息
  started_at DATETIME COMMENT '开始执行时间',
  finished_at DATETIME COMMENT '执行完成时间',
  duration_ms INT COMMENT '执行耗时（毫秒）',
  
  -- 错误信息
  error_message TEXT COMMENT '错误信息',
  error_node_id VARCHAR(100) COMMENT '出错的节点ID',
  
  -- 元数据
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id),
  INDEX idx_workflow_id (workflow_id),
  INDEX idx_status (status),
  INDEX idx_created_by (created_by),
  INDEX idx_created_at (created_at)
) COMMENT '工作流执行实例表';
