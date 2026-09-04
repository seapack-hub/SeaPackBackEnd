-- 3. 技能输入参数表（input_schema 的扁平化存储，可选）
-- ============================================================
CREATE TABLE `ai_skill_param` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `skill_id`    BIGINT       NOT NULL                 COMMENT '所属技能ID',
  `param_name`  VARCHAR(50)  NOT NULL                 COMMENT '参数名，对应 prompt_template 中的变量名',
  `label`       VARCHAR(100) NOT NULL                 COMMENT '参数标签，如"股票代码"',
  `param_type`  VARCHAR(30)  NOT NULL DEFAULT 'string' COMMENT '参数类型：string / number / boolean / select',
  `required`    TINYINT      DEFAULT 1                COMMENT '是否必填：1是 0否',
  `default_value` VARCHAR(500) DEFAULT NULL           COMMENT '默认值',
  `options`     JSON         DEFAULT NULL             COMMENT 'select类型的选项列表 [{label, value}]',
  `placeholder` VARCHAR(200) DEFAULT NULL             COMMENT '输入提示',
  `sort_order`  INT          DEFAULT 0                COMMENT '排序号',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_skill` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能输入参数表';

-- ============================================================
-- 修改2：ai_skill_param 表（保留，用于工具的输入参数定义）
-- ============================================================
-- 这张表保持不变，但语义从"提示词变量"变为"工具输入参数"
-- 用于 LLM Function Calling 的参数声明