CREATE TABLE `ai_knowledge_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `knowledge_id` BIGINT NOT NULL COMMENT '所属知识库ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) DEFAULT NULL COMMENT '存储路径',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` VARCHAR(20) DEFAULT NULL COMMENT '文件类型：txt/pdf/docx/md',
  `content_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME 类型',
  -- 解析与向量化状态
  `parse_status` TINYINT DEFAULT 0 COMMENT '解析状态：0待解析 1解析中 2成功 3失败',
  `vector_status` TINYINT DEFAULT 0 COMMENT '向量化状态：0待处理 1处理中 2成功 3失败',
  `chunk_count` INT DEFAULT 0 COMMENT '生成分片数',
  `token_count` BIGINT DEFAULT 0 COMMENT '文档总 Token 数',
  `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
  -- 元数据
  `extra_metadata` JSON DEFAULT NULL COMMENT '扩展元数据（作者、标签等）',
  `created_by` BIGINT DEFAULT NULL COMMENT '上传人',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_id` (`knowledge_id`),
  KEY `idx_parse_status` (`parse_status`),
  KEY `idx_vector_status` (`vector_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档管理';