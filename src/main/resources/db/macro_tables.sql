-- ============================================================
-- 宏观数据模块 — 数据库建表脚本（MySQL 8.0）
-- 3张数据表 + 1张指标字典表
-- ============================================================

-- -----------------------------------------------------------
-- 0. 指标元数据字典
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_macro_indicator_meta` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `indicator_code` VARCHAR(64) NOT NULL COMMENT '指标编码: M0, CPI_YOY, FOREX_USD 等',
  `indicator_name` VARCHAR(128) NOT NULL COMMENT '指标中文名',
  `frequency` VARCHAR(16) NOT NULL COMMENT '频率: monthly/daily/weekly',
  `unit` VARCHAR(16) NOT NULL DEFAULT '' COMMENT '单位: 万亿元, %, 亿美元',
  `chart_type` VARCHAR(16) NOT NULL DEFAULT 'line' COMMENT '默认图表: line/bar',
  `chart_color` VARCHAR(16) DEFAULT NULL COMMENT '图表颜色',
  `parent_code` VARCHAR(64) DEFAULT NULL COMMENT '父指标编码（分组展示）',
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_indicator_code` (`indicator_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宏观指标元数据字典';

-- -----------------------------------------------------------
-- 1. 月频指标（M0/M1/M2、社融、PMI、CPI/PPI、LPR、贷款、储备）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `macro_monthly` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL COMMENT '统计月份（每月1号）',
  `indicator_code` VARCHAR(64) NOT NULL COMMENT '指标编码',
  `metric_value` DECIMAL(20,4) NOT NULL COMMENT '指标值',
  `metric_value2` DECIMAL(20,4) DEFAULT NULL COMMENT '第二数值（如FOREX_SDR，成对存储）',
  `mom_change` DECIMAL(14,4) DEFAULT NULL COMMENT '环比变化',
  `data_version` TINYINT NOT NULL DEFAULT 1 COMMENT '口径版本: 1-旧 2-新',
  `source` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '数据来源',
  `extra` JSON DEFAULT NULL COMMENT '扩展字段',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_indicator_ver` (`stat_date`, `indicator_code`, `data_version`),
  KEY `idx_indicator_date` (`indicator_code`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月频宏观指标';

-- -----------------------------------------------------------
-- 2. 日频指标（SHIBOR、两融）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `macro_daily` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL COMMENT '交易日期',
  `indicator_code` VARCHAR(64) NOT NULL COMMENT '指标编码',
  `metric_value` DECIMAL(20,4) NOT NULL COMMENT '指标值',
  `metric_value2` DECIMAL(20,4) DEFAULT NULL COMMENT '第二数值',
  `mom_change` DECIMAL(14,4) DEFAULT NULL COMMENT '日环比变化',
  `source` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '数据来源',
  `extra` JSON DEFAULT NULL COMMENT '扩展字段',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_indicator` (`stat_date`, `indicator_code`),
  KEY `idx_indicator_date` (`indicator_code`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日频宏观指标';

-- -----------------------------------------------------------
-- 3. 周频指标（新开户数）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `macro_weekly` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL COMMENT '统计日期（周末）',
  `indicator_code` VARCHAR(64) NOT NULL COMMENT '指标编码',
  `metric_value` DECIMAL(20,4) NOT NULL COMMENT '指标值',
  `mom_change` DECIMAL(14,4) DEFAULT NULL COMMENT '周环比变化',
  `source` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '数据来源',
  `extra` JSON DEFAULT NULL COMMENT '扩展字段',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_indicator` (`stat_date`, `indicator_code`),
  KEY `idx_indicator_date` (`indicator_code`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周频宏观指标';

-- -----------------------------------------------------------
-- 初始化指标字典数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('M0',           'M0 余额',            'monthly', '万亿元', 'line', '#409EFF', 1),
('M1',           'M1 余额',            'monthly', '万亿元', 'line', '#67C23A', 2),
('M2',           'M2 余额',            'monthly', '万亿元', 'line', '#E6A23C', 3),
('M0_YOY',       'M0 同比增速',        'monthly', '%',     'line', '#409EFF', 4),
('M1_YOY',       'M1 同比增速',        'monthly', '%',     'line', '#67C23A', 5),
('M2_YOY',       'M2 同比增速',        'monthly', '%',     'line', '#E6A23C', 6),
('SF_NEW',       '社融当月新增',        'monthly', '万亿元', 'bar',  '#409EFF', 7),
('SF_STOCK',     '社融存量',            'monthly', '万亿元', 'bar',  '#67C23A', 8),
('SF_YOY',       '社融存量同比',        'monthly', '%',     'line', '#F56C6C', 9),
('PMI_MFG',      '制造业 PMI',         'monthly', '—',     'line', '#409EFF', 10),
('PMI_NONMFG',   '非制造业 PMI',       'monthly', '—',     'line', '#67C23A', 11),
('PMI_COMP',     '综合 PMI',           'monthly', '—',     'line', '#E6A23C', 12),
('CPI_YOY',      'CPI 同比',           'monthly', '%',     'line', '#E6A23C', 13),
('PPI_YOY',      'PPI 同比',           'monthly', '%',     'line', '#F56C6C', 14),
('CPI_MOM',      'CPI 环比',           'monthly', '%',     'line', '#E6A23C', 15),
('PPI_MOM',      'PPI 环比',           'monthly', '%',     'line', '#F56C6C', 16),
('LPR_1Y',       '1年期 LPR',          'monthly', '%',     'step', '#409EFF', 17),
('LPR_5Y',       '5年期 LPR',          'monthly', '%',     'step', '#F56C6C', 18),
('FOREX_USD',    '外汇储备（亿美元）',  'monthly', '亿美元', 'bar',  '#409EFF', 19),
('FOREX_SDR',    '外汇储备（亿SDR）',   'monthly', '亿SDR', 'bar',  '#67C23A', 20),
('FOREX_CHG',    '外汇储备环比变化',    'monthly', '亿美元', 'bar',  '#909399', 21),
('IMF_USD',      '基金组织头寸',        'monthly', '亿美元', 'bar',  '#67C23A', 22),
('SDR_USD',      '特别提款权',          'monthly', '亿美元', 'bar',  '#E6A23C', 23),
('GOLD_USD',     '黄金价值（亿美元）',  'monthly', '亿美元', 'line', '#E6A23C', 24),
('GOLD_OZ',      '黄金储备（万盎司）',  'monthly', '万盎司', 'line', '#E6A23C', 25),
('GOLD_CHG',     '黄金储备环比变化',    'monthly', '万盎司', 'bar',  '#909399', 26),
('TOTAL_USD',    '合计（亿美元）',      'monthly', '亿美元', 'bar',  '#909399', 27),
('LOAN_NEW',     '当月新增贷款',        'monthly', '亿元',   'bar',  '#409EFF', 28),
('LOAN_YOY',     '同比多增',            'monthly', '亿元',   'bar',  '#67C23A', 29),
('SHIBOR_ON',    'SHIBOR 隔夜',        'daily',   '%',     'line', '#409EFF', 1),
('SHIBOR_1W',    'SHIBOR 1周',         'daily',   '%',     'line', '#67C23A', 2),
('SHIBOR_1M',    'SHIBOR 1月',         'daily',   '%',     'line', '#E6A23C', 3),
('SHIBOR_1Y',    'SHIBOR 1年',         'daily',   '%',     'line', '#F56C6C', 4),
('MARGIN_BUY',   '融资余额',            'daily',   '亿元',   'line', '#409EFF', 5),
('SHORT_SELL',   '融券余额',            'daily',   '亿元',   'line', '#F56C6C', 6),
('MARGIN_TOTAL', '两融合计',            'daily',   '亿元',   'bar',  '#67C23A', 7),
('NEW_INVEST',   '新增投资者',          'weekly',  '万人',   'bar',  '#9C27B0', 1),
('TOTAL_INVEST', '累计投资者',          'weekly',  '万人',   'line', '#E6A23C', 2);
