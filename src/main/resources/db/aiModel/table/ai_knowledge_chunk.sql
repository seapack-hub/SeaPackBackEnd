CREATE TABLE `ai_knowledge_chunk` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `knowledge_id` BIGINT NOT NULL COMMENT '所属知识库ID',
  `document_id` BIGINT NOT NULL COMMENT '来源文档ID',
  `vector_id` VARCHAR(100) DEFAULT NULL COMMENT '向量数据库中的Record ID（用于关联删除/查询）',
  `chunk_index` INT NOT NULL COMMENT '分片序号（从0开始）',
  `content` TEXT NOT NULL COMMENT '分片文本内容',
  `token_count` INT DEFAULT 0 COMMENT '分片 Token 数',
  `source_page` INT DEFAULT NULL COMMENT '来源页码（PDF适用）',
  `source_section` VARCHAR(200) DEFAULT NULL COMMENT '来源章节标题',
  `extra_metadata` JSON DEFAULT NULL COMMENT '扩展元数据',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_id` (`knowledge_id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_knowledge_doc` (`knowledge_id`, `document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库向量分片';