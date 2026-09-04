-- ============================================================
-- 2. 场景级助手运行配置表
--    同一 Agent 在不同场景可用不同模型/参数
-- ============================================================
CREATE TABLE `ai_scene_agent_config` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `scene_id`       BIGINT        NOT NULL                 COMMENT '场景ID，关联 ai_scene.id',
  `agent_id`       BIGINT        NOT NULL                 COMMENT '助手ID，关联 ai_agent.id',
  `model`          VARCHAR(50)   DEFAULT NULL             COMMENT '覆盖模型编码，NULL 表示使用 Agent 默认',
  `temperature`    DECIMAL(3,2)  DEFAULT NULL             COMMENT '覆盖温度 0.00~2.00，NULL 使用 Agent 默认',
  `max_tokens`     INT           DEFAULT NULL             COMMENT '最大输出 Token，NULL 使用 Agent 默认',
  `system_prompt`  TEXT          DEFAULT NULL             COMMENT '场景级 System Prompt 追加内容',
  `output_format`  VARCHAR(20)   DEFAULT 'markdown'       COMMENT '输出格式：markdown / json / text / html',
  `context_limit`  INT           DEFAULT 8000             COMMENT '上下文窗口上限 Token 数',
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_agent` (`scene_id`, `agent_id`),
  KEY `idx_scene` (`scene_id`),
  KEY `idx_agent` (`agent_id`),
  CONSTRAINT `fk_sac_scene` FOREIGN KEY (`scene_id`) REFERENCES `ai_scene` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sac_agent` FOREIGN KEY (`agent_id`) REFERENCES `ai_agent` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景级助手运行配置表';
