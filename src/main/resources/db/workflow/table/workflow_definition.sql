-- 1.1 工作流定义表
CREATE TABLE workflow_definition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '工作流名称',
  code VARCHAR(100) NOT NULL UNIQUE COMMENT '工作流编码',
  description TEXT COMMENT '工作流描述',
  category_id BIGINT COMMENT '分类ID',
  version INT DEFAULT 1 COMMENT '当前版本号',
  status TINYINT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
  
  -- X6画布视觉数据
  nodes JSON COMMENT '节点视觉数据: [{id, shape, x, y, width, height, attrs, ports}]',
  edges JSON COMMENT '边视觉数据: [{id, source, target, attrs, labels, vertices}]',
  
  -- 业务配置数据（分离存储）
  node_configs JSON COMMENT '节点业务配置: [{nodeId, nodeType, name, config, inputVars, outputVars}]',
  edge_configs JSON COMMENT '边业务配置: [{edgeId, edgeType, conditionExpression, priority}]',
  
  -- 工作流级配置
  variables JSON COMMENT '变量定义: [{name, type, defaultValue, scope, description, required}]',
  viewport JSON COMMENT '画布视口状态: {zoom, scrollX, scrollY}',
  
  -- 元数据
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX idx_category (category_id),
  INDEX idx_status (status),
  INDEX idx_created_by (created_by)
) COMMENT '工作流定义表';