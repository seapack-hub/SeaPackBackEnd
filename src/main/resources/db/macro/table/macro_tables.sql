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
-- 初始化指标字典数据（全量：含 parent_code 分组，供管理页按分类树导航）
-- 幂等：ON DUPLICATE KEY UPDATE 会回填已有记录的分组/排序，可对存量库直接重跑
-- 分组编码: MONEY_SUPPLY-货币供应 / SOCIAL_FINANCE-社会融资 / PMI-景气指数 /
--           PRICE-物价指数 / RATES-利率 / RESERVE-储备资产 /
--           CREDIT-信贷投放 / INVESTOR-投资者 / MARGIN-两融
-- -----------------------------------------------------------
INSERT INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `parent_code`, `sort_order`) VALUES
-- 货币供应
('M0',           'M0 余额',            'monthly', '万亿元', 'line', '#409EFF', 'MONEY_SUPPLY', 10),
('M1',           'M1 余额',            'monthly', '万亿元', 'line', '#67C23A', 'MONEY_SUPPLY', 11),
('M2',           'M2 余额',            'monthly', '万亿元', 'line', '#E6A23C', 'MONEY_SUPPLY', 12),
('M0_YOY',       'M0 同比增速',        'monthly', '%',     'line', '#409EFF', 'MONEY_SUPPLY', 13),
('M1_YOY',       'M1 同比增速',        'monthly', '%',     'line', '#67C23A', 'MONEY_SUPPLY', 14),
('M2_YOY',       'M2 同比增速',        'monthly', '%',     'line', '#E6A23C', 'MONEY_SUPPLY', 15),
-- 社会融资（增量 + 存量）
('SF_NEW',              '社会融资规模增量',            'monthly', '万亿元', 'line', '#409EFF', 'SOCIAL_FINANCE', 20),
('SF_STOCK',            '社会融资规模存量',            'monthly', '万亿元', 'line', '#409EFF', 'SOCIAL_FINANCE', 21),
('SF_YOY',              '社融存量同比增速',            'monthly', '%',     'line', '#F56C6C', 'SOCIAL_FINANCE', 22),
('SF_RMB_LOAN',         '人民币贷款(增量)',            'monthly', '万亿元', 'line', '#67C23A', 'SOCIAL_FINANCE', 23),
('SF_FOREIGN_LOAN',     '外币贷款(折合人民币)(增量)',  'monthly', '万亿元', 'line', '#E6A23C', 'SOCIAL_FINANCE', 24),
('SF_ENTRUDED_LOAN',    '委托贷款(增量)',              'monthly', '万亿元', 'line', '#909399', 'SOCIAL_FINANCE', 25),
('SF_TRUST_LOAN',       '信托贷款(增量)',              'monthly', '万亿元', 'line', '#F56C6C', 'SOCIAL_FINANCE', 26),
('SF_ACCEPTANCE',       '未贴现银行承兑汇票(增量)',    'monthly', '万亿元', 'line', '#E6A23C', 'SOCIAL_FINANCE', 27),
('SF_CORP_BOND',        '企业债券(增量)',              'monthly', '万亿元', 'line', '#409EFF', 'SOCIAL_FINANCE', 28),
('SF_GOVT_BOND',        '政府债券(增量)',              'monthly', '万亿元', 'line', '#67C23A', 'SOCIAL_FINANCE', 29),
('SF_EQUITY',           '非金融企业境内股票融资(增量)','monthly', '万亿元', 'line', '#E6A23C', 'SOCIAL_FINANCE', 30),
('SF_ABS',              '存款类金融机构资产支持证券(增量)', 'monthly', '万亿元', 'line', '#909399', 'SOCIAL_FINANCE', 31),
('SF_LOAN_WRITEOFF',    '贷款核销(增量)',              'monthly', '万亿元', 'line', '#F56C6C', 'SOCIAL_FINANCE', 32),
('SF_RMB_LOAN_STOCK',   '人民币贷款(存量)',            'monthly', '万亿元', 'line', '#67C23A', 'SOCIAL_FINANCE', 33),
('SF_FOREIGN_LOAN_STOCK','外币贷款(折合人民币)(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 'SOCIAL_FINANCE', 34),
('SF_ENTRUDED_LOAN_STOCK','委托贷款(存量)',            'monthly', '万亿元', 'line', '#909399', 'SOCIAL_FINANCE', 35),
('SF_TRUST_LOAN_STOCK', '信托贷款(存量)',              'monthly', '万亿元', 'line', '#F56C6C', 'SOCIAL_FINANCE', 36),
('SF_ACCEPTANCE_STOCK', '未贴现银行承兑汇票(存量)',    'monthly', '万亿元', 'line', '#E6A23C', 'SOCIAL_FINANCE', 37),
('SF_CORP_BOND_STOCK',  '企业债券(存量)',              'monthly', '万亿元', 'line', '#409EFF', 'SOCIAL_FINANCE', 38),
('SF_GOVT_BOND_STOCK',  '政府债券(存量)',              'monthly', '万亿元', 'line', '#67C23A', 'SOCIAL_FINANCE', 39),
('SF_EQUITY_STOCK',     '非金融企业境内股票(存量)',    'monthly', '万亿元', 'line', '#E6A23C', 'SOCIAL_FINANCE', 40),
('SF_ABS_STOCK',        '存款类金融机构资产支持证券(存量)', 'monthly', '万亿元', 'line', '#909399', 'SOCIAL_FINANCE', 41),
('SF_LOAN_WRITEOFF_STOCK','贷款核销(存量)',            'monthly', '万亿元', 'line', '#F56C6C', 'SOCIAL_FINANCE', 42),
-- 景气指数
('PMI_MFG',      '制造业 PMI',         'monthly', '—',     'line', '#409EFF', 'PMI', 10),
('PMI_NONMFG',   '非制造业 PMI',       'monthly', '—',     'line', '#67C23A', 'PMI', 11),
('PMI_COMP',     '综合 PMI',           'monthly', '—',     'line', '#E6A23C', 'PMI', 12),
-- 物价指数
('CPI_YOY',      'CPI 同比',           'monthly', '%',     'line', '#E6A23C', 'PRICE', 10),
('CPI_MOM',      'CPI 环比',           'monthly', '%',     'line', '#E6A23C', 'PRICE', 11),
('PPI_YOY',      'PPI 同比',           'monthly', '%',     'line', '#F56C6C', 'PRICE', 12),
('PPI_MOM',      'PPI 环比',           'monthly', '%',     'line', '#F56C6C', 'PRICE', 13),
-- 利率（LPR 月频 + SHIBOR 日频）
('LPR_1Y',       '1年期 LPR',          'monthly', '%',     'step', '#409EFF', 'RATES', 10),
('LPR_5Y',       '5年期 LPR',          'monthly', '%',     'step', '#F56C6C', 'RATES', 11),
('SHIBOR_ON',    'SHIBOR 隔夜',        'daily',   '%',     'line', '#409EFF', 'RATES', 20),
('SHIBOR_1W',    'SHIBOR 1周',         'daily',   '%',     'line', '#67C23A', 'RATES', 21),
('SHIBOR_2W',    'SHIBOR 2周',         'daily',   '%',     'line', '#E6A23C', 'RATES', 22),
('SHIBOR_1M',    'SHIBOR 1月',         'daily',   '%',     'line', '#F56C6C', 'RATES', 23),
('SHIBOR_3M',    'SHIBOR 3月',         'daily',   '%',     'line', '#409EFF', 'RATES', 24),
('SHIBOR_6M',    'SHIBOR 6月',         'daily',   '%',     'line', '#67C23A', 'RATES', 25),
('SHIBOR_9M',    'SHIBOR 9月',         'daily',   '%',     'line', '#E6A23C', 'RATES', 26),
('SHIBOR_1Y',    'SHIBOR 1年',         'daily',   '%',     'line', '#F56C6C', 'RATES', 27),
-- 储备资产
('FOREX_USD',    '外汇储备（亿美元）',  'monthly', '亿美元', 'bar',  '#409EFF', 'RESERVE', 10),
('FOREX_SDR',    '外汇储备（亿SDR）',   'monthly', '亿SDR', 'bar',  '#67C23A', 'RESERVE', 11),
('FOREX_CHG',    '外汇储备环比变动',    'monthly', '亿美元', 'line', '#909399', 'RESERVE', 12),
('IMF_USD',      '基金组织储备头寸',    'monthly', '亿美元', 'bar',  '#67C23A', 'RESERVE', 13),
('SDR_USD',      '特别提款权',          'monthly', '亿美元', 'bar',  '#E6A23C', 'RESERVE', 14),
('GOLD_USD',     '黄金价值（亿美元）',  'monthly', '亿美元', 'line', '#E6A23C', 'RESERVE', 20),
('GOLD_SDR',     '黄金价值（亿SDR）',   'monthly', '亿SDR', 'line', '#E6A23C', 'RESERVE', 21),
('GOLD_OZ',      '黄金储备（万盎司）',  'monthly', '万盎司', 'line', '#E6A23C', 'RESERVE', 22),
('GOLD_CHG',     '黄金储备环比变动',    'monthly', '万盎司', 'line', '#909399', 'RESERVE', 23),
('TOTAL_USD',    '储备合计（亿美元）',  'monthly', '亿美元', 'bar',  '#909399', 'RESERVE', 30),
('TOTAL_SDR',    '储备合计（亿SDR）',   'monthly', '亿SDR', 'bar',  '#909399', 'RESERVE', 31),
-- 信贷投放
('LOAN_NEW',     '当月新增贷款',        'monthly', '亿元',   'bar',  '#409EFF', 'CREDIT', 10),
('LOAN_YOY',     '同比多增',            'monthly', '亿元',   'bar',  '#67C23A', 'CREDIT', 11),
-- 投资者（周频）
('NEW_INVEST',   '新增投资者',          'weekly',  '万人',   'bar',  '#9C27B0', 'INVESTOR', 10),
('TOTAL_INVEST', '累计投资者',          'weekly',  '万人',   'line', '#E6A23C', 'INVESTOR', 11),
-- 两融（日频）
('MARGIN_BUY',   '融资余额',            'daily',   '亿元',   'line', '#409EFF', 'MARGIN', 10),
('SHORT_SELL',   '融券余额',            'daily',   '亿元',   'line', '#F56C6C', 'MARGIN', 11),
('MARGIN_TOTAL', '两融余额合计',        'daily',   '亿元',   'line', '#67C23A', 'MARGIN', 12)
ON DUPLICATE KEY UPDATE
  `indicator_name` = VALUES(`indicator_name`),
  `frequency`      = VALUES(`frequency`),
  `unit`           = VALUES(`unit`),
  `chart_type`     = VALUES(`chart_type`),
  `chart_color`    = VALUES(`chart_color`),
  `parent_code`    = VALUES(`parent_code`),
  `sort_order`     = VALUES(`sort_order`);
