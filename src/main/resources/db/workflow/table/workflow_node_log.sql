-- 2.2 工作流节点执行日志表
CREATE TABLE workflow_node_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_id BIGINT NOT NULL COMMENT '工作流实例ID',
  
  -- 节点标识
  node_id VARCHAR(100) NOT NULL COMMENT 'X6节点ID',
  node_type VARCHAR(50) NOT NULL COMMENT '节点类型: start/end/skill/http_request/condition/approval等',
  node_name VARCHAR(100) COMMENT '节点名称',
  
  -- 执行状态
  status TINYINT DEFAULT 0 COMMENT '状态: 0=待执行, 1=执行中, 2=已完成, 3=失败, 4=跳过, 5=等待人工, 6=超时, 7=已取消',
  
  -- 输入/输出数据
  input_data JSON COMMENT '传入的变量数据',
  output_data JSON COMMENT '输出的变量数据',
  
  -- 节点配置快照（该节点执行时的配置）
  node_config_snapshot JSON COMMENT '节点业务配置快照',
  
  -- 执行元数据
  retry_count INT DEFAULT 0 COMMENT '重试次数',
  max_retries INT DEFAULT 0 COMMENT '最大重试次数',
  executor_type VARCHAR(20) COMMENT '执行者类型: system/human/ai',
  executor_ref VARCHAR(100) COMMENT '执行者引用: 技能ID/用户ID/模型名称',
  
  -- 错误信息
  error_message TEXT COMMENT '错误信息',
  error_stack TEXT COMMENT '错误堆栈',
  
  -- 时间信息
  started_at DATETIME COMMENT '开始时间',
  completed_at DATETIME COMMENT '完成时间',
  duration_ms INT COMMENT '执行耗时（毫秒）',
  
  FOREIGN KEY (instance_id) REFERENCES workflow_instance(id) ON DELETE CASCADE,
  INDEX idx_instance_id (instance_id),
  INDEX idx_node_id (node_id),
  INDEX idx_status (status),
  INDEX idx_started_at (started_at)
) COMMENT '工作流节点执行日志表';