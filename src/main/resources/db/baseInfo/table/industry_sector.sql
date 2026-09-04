DROP TABLE IF EXISTS industry_sector;

CREATE TABLE industry_sector (
    -- 核心主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    
    -- 业务字段
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '业务编码 (如: 01, 0101)',
    label VARCHAR(50) NOT NULL COMMENT '行业名称 (如: 科技, 半导体)',
    
    -- 树形结构
    parent_id BIGINT DEFAULT NULL COMMENT '父节点ID',
    
    -- 层级与排序
    node_level TINYINT NOT NULL DEFAULT 1 COMMENT '层级深度 (1:一级, 2:二级)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    
    -- 【新增】逻辑删除字段：0表示未删除，1表示已删除
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 (0:正常, 1:已删除)',
    
    -- 审计字段
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    -- 索引：提高查询效率
    INDEX idx_parent (parent_id),
    INDEX idx_code (code),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业板块表';

-- 【重要】去掉了原有的物理外键约束 (ON DELETE CASCADE)
-- 因为物理外键会在删除父节点时强制物理删除子节点，这与逻辑删除冲突。
-- 父子关系现在完全通过代码逻辑和 parent_id 字段来维护。