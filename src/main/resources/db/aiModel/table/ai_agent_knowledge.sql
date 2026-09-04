-- ============================================================
-- 5. Agent关联知识库（多对多）
-- ============================================================
CREATE TABLE `ai_agent_knowledge` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `agent_id`        BIGINT NOT NULL                 COMMENT '助手ID',
  `knowledge_id`    BIGINT NOT NULL                 COMMENT '知识库ID',
  `enabled`         TINYINT DEFAULT 1               COMMENT '1启用 0禁用',
  `retrieval_count` INT    DEFAULT 5                COMMENT '每次检索返回片段数',
  `sort_order`      INT    DEFAULT 0,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_knowledge` (`agent_id`, `knowledge_id`),
  KEY `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='助手关联知识库';