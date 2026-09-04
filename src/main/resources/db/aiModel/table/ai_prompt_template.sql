-- ============================================================
-- 1. 提示词模板表（独立出来，可跨技能/助手复用）
-- ============================================================
CREATE TABLE `ai_prompt_template` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `name`          VARCHAR(100) NOT NULL                 COMMENT '模板名称，如"股票技术分析模板"',
  `code`          VARCHAR(100) NOT NULL                 COMMENT '模板编码，唯一标识',
  `category`      VARCHAR(50)  DEFAULT 'general'        COMMENT '分类：stock_analysis / content_gen / data_qa / general',
  `content`       TEXT         NOT NULL                 COMMENT '模板正文，支持 {{变量名}} 占位符',
  `description`   VARCHAR(500) DEFAULT NULL             COMMENT '模板用途说明',
  `output_format` VARCHAR(20)  DEFAULT 'markdown'       COMMENT '期望输出格式：markdown/json/text/html',
  `version`       VARCHAR(20)  DEFAULT 'v1.0.0'         COMMENT '版本号',
  `use_count`     INT          DEFAULT 0                COMMENT '被引用次数',
  `status`        TINYINT      DEFAULT 1                COMMENT '1启用 0禁用',
  `created_by`    BIGINT       DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词模板表';