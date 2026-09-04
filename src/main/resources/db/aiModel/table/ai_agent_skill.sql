-- ============================================================
-- 4. Agent关联技能（多对多）
-- ============================================================
CREATE TABLE `ai_agent_skill` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `agent_id`    BIGINT NOT NULL                 COMMENT '助手ID',
  `skill_id`    BIGINT NOT NULL                 COMMENT '技能ID',
  `enabled`     TINYINT DEFAULT 1               COMMENT '1启用 0禁用',
  `is_primary`  TINYINT DEFAULT 0               COMMENT '1=主技能 0=辅助技能',
  `sort_order`  INT    DEFAULT 0                COMMENT '排序号',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_skill` (`agent_id`, `skill_id`),
  KEY `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='助手关联技能';