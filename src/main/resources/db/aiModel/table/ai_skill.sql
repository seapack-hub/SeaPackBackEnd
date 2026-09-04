-- ============================================================
-- 2. 技能定义表（核心表，最终合并版本）
-- ============================================================
CREATE TABLE `ai_skill` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `category_id`     BIGINT       DEFAULT NULL             COMMENT '所属分类ID',
  `name`            VARCHAR(100) NOT NULL                 COMMENT '技能名称，如"文章AI写作助手"',
  `code`            VARCHAR(50)  NOT NULL                 COMMENT '技能编码，唯一标识，用于前端路由匹配',
  `icon`            VARCHAR(100) DEFAULT NULL             COMMENT '技能图标SVG文件名',
  `description`     VARCHAR(500) DEFAULT NULL             COMMENT '技能描述',
  `skill_type`      VARCHAR(30)  DEFAULT 'tool'           COMMENT '技能类型：tool=工具调用 / rag=知识检索 / hybrid=混合',
  `endpoint`        VARCHAR(200) DEFAULT NULL             COMMENT '工具调用端点（后端API路径或外部服务URL）',
  `timeout_ms`      INT          DEFAULT 30000            COMMENT '调用超时（毫秒）',
  `input_schema`    JSON         DEFAULT NULL             COMMENT '输入参数JSON Schema定义',
  `status`          TINYINT      DEFAULT 1                COMMENT '状态：1启用 0禁用',
  `sort_order`      INT          DEFAULT 0                COMMENT '排序号',
  `use_count`       INT          DEFAULT 0                COMMENT '使用次数（统计）',
  `version`         VARCHAR(20)  DEFAULT 'v1.0.0'         COMMENT '当前版本号',
  `created_by`      BIGINT       DEFAULT NULL             COMMENT '创建人ID',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI技能定义表';