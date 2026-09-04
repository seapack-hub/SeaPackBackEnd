-- ============================================================
-- 1. ai_scene_orchestration 编排主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_scene_orchestration` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scene_id`      BIGINT       NOT NULL                COMMENT '关联场景ID',
  `name`          VARCHAR(128) NOT NULL                COMMENT '编排名称',
  `code`          VARCHAR(64)  NOT NULL                COMMENT '编排编码（场景内唯一）',
  `description`   VARCHAR(512) DEFAULT NULL            COMMENT '编排描述',
  `strategy`      VARCHAR(20)  NOT NULL DEFAULT 'sequential' COMMENT '执行策略：sequential-顺序执行 | parallel-并行执行 | llm_tool-LLM决策工具调用 | auto-自动（单Agent顺序/多Agent并行）',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
  `sort_order`    INT          NOT NULL DEFAULT 0      COMMENT '排序号',
  `created_by`    BIGINT       DEFAULT NULL            COMMENT '创建人',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scene_id` (`scene_id`),
  UNIQUE KEY `uk_scene_code` (`scene_id`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='场景编排主表';
