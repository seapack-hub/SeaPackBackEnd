-- ============================================================
-- 6. 场景表
-- ============================================================
CREATE TABLE `ai_scene` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `name`          VARCHAR(100) NOT NULL                 COMMENT '场景名称',
  `code`          VARCHAR(100) NOT NULL                 COMMENT '场景编码',
  `icon`          VARCHAR(200) DEFAULT NULL             COMMENT '图标',
  `cover_color`   VARCHAR(20)  DEFAULT NULL             COMMENT '卡片渐变色',
  `description`   VARCHAR(500) DEFAULT NULL             COMMENT '场景描述',
  `is_public`     TINYINT      DEFAULT 1                COMMENT '1公开 0私有',
  `status`        TINYINT      DEFAULT 1                COMMENT '1启用 0禁用',
  `sort_order`    INT          DEFAULT 0,
  `use_count`     INT          DEFAULT 0,
  `created_by`    BIGINT       DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI业务场景表';