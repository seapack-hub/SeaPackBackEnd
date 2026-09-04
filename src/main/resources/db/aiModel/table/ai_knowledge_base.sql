CREATE TABLE `ai_knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
  `code` VARCHAR(64) NOT NULL COMMENT '知识库编码（唯一标识，用于API调用）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '知识库描述',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  -- 向量化配置
  `embedding_model` VARCHAR(50) DEFAULT 'text-embedding-v3' COMMENT '向量化模型编码',
  `chunk_size` INT DEFAULT 512 COMMENT '分片大小（字符数）',
  `chunk_overlap` INT DEFAULT 50 COMMENT '分片重叠字符数',
  `separator` VARCHAR(20) DEFAULT '\\n\\n' COMMENT '分片分隔符',
  -- 统计字段
  `document_count` INT DEFAULT 0 COMMENT '文档总数',
  `chunk_count` INT DEFAULT 0 COMMENT '分片总数',
  `total_tokens` BIGINT DEFAULT 0 COMMENT '总 Token 消耗',
  -- 状态
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 知识库主表';