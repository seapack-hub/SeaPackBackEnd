-- ============================================================
-- 3. Agent关联提示词模板（多对多，一个Agent可组合多个模板）
-- ============================================================
CREATE TABLE `ai_agent_prompt` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `agent_id`      BIGINT NOT NULL                 COMMENT '助手ID',
  `template_id`   BIGINT NOT NULL                 COMMENT '提示词模板ID',
  `is_primary`    TINYINT DEFAULT 0               COMMENT '1=主模板 0=辅助模板',
  `enabled`       TINYINT DEFAULT 1               COMMENT '1启用 0禁用',
  `sort_order`    INT    DEFAULT 0                COMMENT '排序号',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_template` (`agent_id`, `template_id`),
  KEY `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='助手关联提示词模板';