-- 1.2 工作流版本历史表
CREATE TABLE workflow_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workflow_id BIGINT NOT NULL COMMENT '工作流ID',
  version INT NOT NULL COMMENT '版本号',
  
  -- 完整定义快照
  nodes JSON COMMENT '节点视觉数据快照',
  edges JSON COMMENT '边视觉数据快照',
  node_configs JSON COMMENT '节点业务配置快照',
  edge_configs JSON COMMENT '边业务配置快照',
  variables JSON COMMENT '变量定义快照',
  viewport JSON COMMENT '视口快照',
  
  change_log TEXT COMMENT '变更说明',
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id) ON DELETE CASCADE,
  UNIQUE KEY uk_workflow_version (workflow_id, version)
) COMMENT '工作流版本历史表';