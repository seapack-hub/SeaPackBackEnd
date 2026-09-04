-- ============================================================
-- 2. 提示词模板变量表（替代 ai_skill_param 中的提示词变量）
-- ============================================================
CREATE TABLE `ai_template_variable` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `template_id`   BIGINT       NOT NULL                 COMMENT '所属模板ID',
  `var_name`      VARCHAR(50)  NOT NULL                 COMMENT '变量名，对应 content 中的 {{var_name}}',
  `label`         VARCHAR(100) NOT NULL                 COMMENT '显示标签，如"股票代码"',
  `var_type`      VARCHAR(30)  NOT NULL DEFAULT 'string' COMMENT '变量类型：string/number/boolean/select/date',
  `required`      TINYINT      DEFAULT 1                COMMENT '1必填 0选填',
  `default_value` VARCHAR(500) DEFAULT NULL             COMMENT '默认值',
  `options`       JSON         DEFAULT NULL             COMMENT 'select类型选项 [{label,value}]',
  `placeholder`   VARCHAR(200) DEFAULT NULL             COMMENT '输入提示',
  `sort_order`    INT          DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板变量定义表';