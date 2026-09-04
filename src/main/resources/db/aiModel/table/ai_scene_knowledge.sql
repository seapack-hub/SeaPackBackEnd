-- ============================================================
-- 8. 场景关联知识库（多对多）
-- ============================================================
CREATE TABLE `ai_scene_knowledge` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `scene_id`      BIGINT NOT NULL                 COMMENT '场景ID',
  `knowledge_id`  BIGINT NOT NULL                 COMMENT '知识库ID',
  `enabled`       TINYINT DEFAULT 1,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_knowledge` (`scene_id`, `knowledge_id`),
  KEY `idx_scene` (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景关联知识库';