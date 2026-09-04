-- ============================================================
-- 7. 场景关联助手（多对多）
-- ============================================================
CREATE TABLE `ai_scene_agent` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `scene_id`    BIGINT NOT NULL                 COMMENT '场景ID',
  `agent_id`    BIGINT NOT NULL                 COMMENT '助手ID',
  `is_default`  TINYINT DEFAULT 0               COMMENT '1=默认助手 0=普通',
  `sort_order`  INT    DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_agent` (`scene_id`, `agent_id`),
  KEY `idx_scene` (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景关联助手';