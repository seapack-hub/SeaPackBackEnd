-- 1. 技能分类表
-- ============================================================
CREATE TABLE `ai_skill_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL                 COMMENT '分类名称，如"内容生成"、"数据分析"',
  `code`        VARCHAR(50)  NOT NULL                 COMMENT '分类编码，唯一标识，如 content_gen',
  `icon`        VARCHAR(100) DEFAULT NULL             COMMENT '分类图标',
  `description` VARCHAR(255) DEFAULT NULL             COMMENT '分类描述',
  `sort_order`  INT          DEFAULT 0                COMMENT '排序号，越小越靠前',
  `status`      TINYINT      DEFAULT 1                COMMENT '状态：1启用 0禁用',
  `created_by`  BIGINT       DEFAULT NULL             COMMENT '创建人ID',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI技能分类表';