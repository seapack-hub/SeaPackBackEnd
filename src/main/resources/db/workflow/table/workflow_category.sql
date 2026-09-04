-- ============================================================
-- 工作流分类表（支持树形结构）
-- ============================================================
CREATE TABLE workflow_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父级分类ID，0表示顶级',
  sort_order INT DEFAULT 0 COMMENT '排序序号',
  status TINYINT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
  
  created_by BIGINT COMMENT '创建人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX idx_parent_id (parent_id),
  INDEX idx_status (status)
) COMMENT '工作流分类表';