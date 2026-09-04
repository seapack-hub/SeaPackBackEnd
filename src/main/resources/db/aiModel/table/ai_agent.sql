-- ============================================================
-- 6. Agent/助手表（新建）
-- ============================================================
CREATE TABLE `ai_agent` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `name`            VARCHAR(100) NOT NULL                 COMMENT '助手名称，如"股票分析师"',
  `code`            VARCHAR(100) NOT NULL                 COMMENT '助手编码，唯一标识',
  `avatar`          VARCHAR(200) DEFAULT NULL             COMMENT '助手头像（emoji或图片URL）',
  `description`     VARCHAR(500) DEFAULT NULL             COMMENT '助手描述，展示给用户看',
  `system_prompt`   TEXT         NOT NULL                 COMMENT '系统提示词，定义助手角色和行为规则',
  `greeting`        VARCHAR(500) DEFAULT NULL             COMMENT '开场白，首次对话时自动发送',
  `model_code`      VARCHAR(50)  DEFAULT 'deepseek-chat' COMMENT '默认模型编码',
  `temperature`     DECIMAL(3,2) DEFAULT 0.70            COMMENT '模型温度参数 0-2',
  `max_tokens`      INT          DEFAULT 2048             COMMENT '最大输出token数',
  `output_format`   VARCHAR(50)  DEFAULT 'markdown'       COMMENT '输出格式：markdown/json/text/html',
  `memory_enabled`  TINYINT      DEFAULT 0                COMMENT '是否开启对话记忆：1是 0否',
  `memory_window`   INT          DEFAULT 20               COMMENT '记忆窗口大小（最近N轮对话）',
  `version`         VARCHAR(20)  DEFAULT 'v1.0.0'         COMMENT '配置版本号',
  `status`          TINYINT      DEFAULT 1                COMMENT '状态：1启用 0禁用',
  `sort_order`      INT          DEFAULT 0                COMMENT '排序号',
  `use_count`       INT          DEFAULT 0                COMMENT '使用次数统计',
  `created_by`      BIGINT       DEFAULT NULL             COMMENT '创建人ID',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手/Agent定义表';