-- ============================================================
-- 1. 场景部署表（替代原 ai_scene.module_key + position）
--    一个场景可部署到多个模块的多个位置
-- ============================================================
CREATE TABLE `ai_scene_deployment` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `scene_id`     BIGINT       NOT NULL                 COMMENT '场景ID，关联 ai_scene.id',
  `module_key`   VARCHAR(50)  NOT NULL                 COMMENT '前端模块标识，与 aiPositions.ts / config/modules.ts 对应',
  `position_key` VARCHAR(50)  NOT NULL                 COMMENT '位置标识，如 editor-toolbar / detail-toolbar',
  `config`       JSON         DEFAULT NULL             COMMENT '部署配置，如 {"button_text":"AI 写作","icon":"Edit","tooltip":"使用 AI 辅助写作"}',
  `is_default`   TINYINT      DEFAULT 0                COMMENT '是否该位置的默认场景：1-是 0-否',
  `sort_order`   INT          DEFAULT 0                COMMENT '排序号',
  `status`       TINYINT      DEFAULT 1                COMMENT '状态：1-启用 0-禁用',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_deployment` (`scene_id`, `module_key`, `position_key`),
  KEY `idx_module_position` (`module_key`, `position_key`),
  KEY `idx_scene` (`scene_id`),
  CONSTRAINT `fk_deployment_scene` FOREIGN KEY (`scene_id`) REFERENCES `ai_scene` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景部署配置表';