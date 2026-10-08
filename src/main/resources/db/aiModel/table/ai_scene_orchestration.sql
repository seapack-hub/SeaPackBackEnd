-- ============================================================
-- 1. ai_scene_orchestration 编排主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_scene_orchestration` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scene_id`            BIGINT       NOT NULL                COMMENT '关联场景ID',
  `name`                VARCHAR(128) NOT NULL                COMMENT '编排名称',
  `code`                VARCHAR(64)  NOT NULL                COMMENT '编排编码（场景内唯一）',
  `description`         VARCHAR(512) NULL DEFAULT NULL       COMMENT '编排描述',
  `strategy`            VARCHAR(20)  NOT NULL DEFAULT 'sequential' COMMENT '执行策略：sequential-顺序 | parallel-并行 | supervisor-总控调度 | crew-角色协作 | dynamic-动态规划',
  `supervisor_agent_id` BIGINT       NULL DEFAULT NULL       COMMENT 'Supervisor Agent ID，有值时由该Agent动态调度，无值时按步骤执行',
  `max_rounds`          INT          NOT NULL DEFAULT 5      COMMENT 'Agent间最大协作轮次（防止死循环）',
  `context_strategy`    VARCHAR(20)  NOT NULL DEFAULT 'structured' COMMENT '上下文传递策略：text_only-纯文本 | structured-结构化JSON | shared_state-共享状态对象',
  `status`              TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
  `sort_order`          INT          NOT NULL DEFAULT 0      COMMENT '排序号',
  `created_by`          BIGINT       NULL DEFAULT NULL       COMMENT '创建人',
  `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_scene_code`(`scene_id` ASC, `code` ASC) USING BTREE,
  INDEX `idx_scene_id`(`scene_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '场景编排主表' ROW_FORMAT = Dynamic;
