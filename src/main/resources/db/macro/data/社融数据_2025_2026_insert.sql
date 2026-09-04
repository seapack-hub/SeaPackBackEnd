-- ============================================================
-- 社会融资规模数据插入脚本 (2025-2026)
-- 数据来源: 中国人民银行
-- ============================================================

-- -----------------------------------------------------------
-- 1. 插入社融存量指标元数据
-- -----------------------------------------------------------
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_STOCK', '社会融资规模存量', 'monthly', '万亿元', 'line', '#409EFF', 1);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_RMB_LOAN_STOCK', '人民币贷款(存量)', 'monthly', '万亿元', 'line', '#67C23A', 2);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_FOREIGN_LOAN_STOCK', '外币贷款(折合人民币)(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 3);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ENTRUDED_LOAN_STOCK', '委托贷款(存量)', 'monthly', '万亿元', 'line', '#909399', 4);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_TRUST_LOAN_STOCK', '信托贷款(存量)', 'monthly', '万亿元', 'line', '#F56C6C', 5);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ACCEPTANCE_STOCK', '未贴现银行承兑汇票(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 6);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_CORP_BOND_STOCK', '企业债券(存量)', 'monthly', '万亿元', 'line', '#409EFF', 7);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_GOVT_BOND_STOCK', '政府债券(存量)', 'monthly', '万亿元', 'line', '#67C23A', 8);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_EQUITY_STOCK', '非金融企业境内股票(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 9);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ABS_STOCK', '存款类金融机构资产支持证券(存量)', 'monthly', '万亿元', 'line', '#909399', 10);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_LOAN_WRITEOFF_STOCK', '贷款核销(存量)', 'monthly', '万亿元', 'line', '#F56C6C', 11);

-- -----------------------------------------------------------
-- 2. 插入社融增量指标元数据
-- -----------------------------------------------------------
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_NEW', '社会融资规模增量', 'monthly', '万亿元', 'line', '#409EFF', 1);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_RMB_LOAN', '人民币贷款(增量)', 'monthly', '万亿元', 'line', '#67C23A', 2);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_FOREIGN_LOAN', '外币贷款(折合人民币)(增量)', 'monthly', '万亿元', 'line', '#E6A23C', 3);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ENTRUDED_LOAN', '委托贷款(增量)', 'monthly', '万亿元', 'line', '#909399', 4);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_TRUST_LOAN', '信托贷款(增量)', 'monthly', '万亿元', 'line', '#F56C6C', 5);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ACCEPTANCE', '未贴现银行承兑汇票(增量)', 'monthly', '万亿元', 'line', '#E6A23C', 6);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_CORP_BOND', '企业债券(增量)', 'monthly', '万亿元', 'line', '#409EFF', 7);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_GOVT_BOND', '政府债券(增量)', 'monthly', '万亿元', 'line', '#67C23A', 8);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_EQUITY', '非金融企业境内股票融资(增量)', 'monthly', '万亿元', 'line', '#E6A23C', 9);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ABS', '存款类金融机构资产支持证券(增量)', 'monthly', '万亿元', 'line', '#909399', 10);
INSERT IGNORE INTO sea_pack.`sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_LOAN_WRITEOFF', '贷款核销(增量)', 'monthly', '万亿元', 'line', '#F56C6C', 11);

-- -----------------------------------------------------------
-- 3. 插入社融存量数据 (2026年1-7月)
-- 单位: 万亿元, metric_value2存储同比增速(%)
-- -----------------------------------------------------------
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_STOCK', 449.1100, 8.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_STOCK', 451.4000, 8.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_STOCK', 456.4600, 7.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_STOCK', 456.8800, 7.8000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_STOCK', 458.8100, 7.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_STOCK', 462.0600, 7.4000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_STOCK', 463.2700, 7.4000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_RMB_LOAN_STOCK', 273.3000, 6.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_RMB_LOAN_STOCK', 274.1500, 6.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_RMB_LOAN_STOCK', 277.3000, 5.8000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_RMB_LOAN_STOCK', 276.9000, 5.6000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_RMB_LOAN_STOCK', 277.4000, 5.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_RMB_LOAN_STOCK', 279.1600, 5.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_RMB_LOAN_STOCK', 278.5700, 5.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_FOREIGN_LOAN_STOCK', 1.0900, -12.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_FOREIGN_LOAN_STOCK', 1.0800, -11.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_FOREIGN_LOAN_STOCK', 1.1200, -5.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_FOREIGN_LOAN_STOCK', 1.1300, -3.8000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_FOREIGN_LOAN_STOCK', 1.1400, -4.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_FOREIGN_LOAN_STOCK', 1.1800, -2.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_FOREIGN_LOAN_STOCK', 1.1900, -1.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3000, 0.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2800, 0.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2500, 0.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2300, -0.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2200, 0.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2400, 0.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2400, 0.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_TRUST_LOAN_STOCK', 4.6700, 7.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_TRUST_LOAN_STOCK', 4.7000, 8.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_TRUST_LOAN_STOCK', 4.6800, 7.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_TRUST_LOAN_STOCK', 4.6700, 7.4000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_TRUST_LOAN_STOCK', 4.6700, 7.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_TRUST_LOAN_STOCK', 4.6200, 4.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_TRUST_LOAN_STOCK', 4.6000, 3.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_ACCEPTANCE_STOCK', 2.7800, 6.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_ACCEPTANCE_STOCK', 2.6000, 12.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_ACCEPTANCE_STOCK', 2.7300, 2.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_ACCEPTANCE_STOCK', 2.2000, -7.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_ACCEPTANCE_STOCK', 2.1300, -6.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_ACCEPTANCE_STOCK', 2.0200, -2.8000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_ACCEPTANCE_STOCK', 1.9700, 2.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_CORP_BOND_STOCK', 34.6900, 6.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_CORP_BOND_STOCK', 34.8400, 6.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_CORP_BOND_STOCK', 35.1600, 7.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_CORP_BOND_STOCK', 35.5200, 8.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_CORP_BOND_STOCK', 35.6900, 8.4000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_CORP_BOND_STOCK', 36.0800, 8.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_CORP_BOND_STOCK', 36.4700, 9.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_GOVT_BOND_STOCK', 95.9000, 17.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_GOVT_BOND_STOCK', 97.3000, 16.6000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_GOVT_BOND_STOCK', 98.4700, 15.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_GOVT_BOND_STOCK', 99.3700, 15.6000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_GOVT_BOND_STOCK', 100.6000, 15.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_GOVT_BOND_STOCK', 101.3600, 14.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_GOVT_BOND_STOCK', 102.6800, 14.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_EQUITY_STOCK', 12.2300, 3.9000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_EQUITY_STOCK', 12.2700, 4.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_EQUITY_STOCK', 12.3100, 4.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_EQUITY_STOCK', 12.4000, 4.6000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_EQUITY_STOCK', 12.4300, 4.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_EQUITY_STOCK', 12.4900, 5.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_EQUITY_STOCK', 12.6000, 5.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_ABS_STOCK', 0.6900, -11.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_ABS_STOCK', 0.6800, -8.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_ABS_STOCK', 0.6700, -9.3000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_ABS_STOCK', 0.6600, -7.1000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_ABS_STOCK', 0.6300, -9.2000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_ABS_STOCK', 0.6600, -3.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_ABS_STOCK', 0.6500, -5.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-01-01', 'SF_LOAN_WRITEOFF_STOCK', 11.4500, 14.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-02-01', 'SF_LOAN_WRITEOFF_STOCK', 11.4800, 14.5000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-03-01', 'SF_LOAN_WRITEOFF_STOCK', 11.7400, 14.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-04-01', 'SF_LOAN_WRITEOFF_STOCK', 11.7900, 14.6000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-05-01', 'SF_LOAN_WRITEOFF_STOCK', 11.9000, 14.7000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-06-01', 'SF_LOAN_WRITEOFF_STOCK', 12.2200, 15.0000, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2026-07-01', 'SF_LOAN_WRITEOFF_STOCK', 12.2900, 15.2000, '中国人民银行');

-- -----------------------------------------------------------
-- 4. 插入社融增量数据 (2025-2026)
-- 单位: 万亿元 (原始数据为亿元人民币, 已转换)
-- -----------------------------------------------------------
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_ABS', -0.0227, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_ACCEPTANCE', 0.4654, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_CORP_BOND', 0.4454, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_ENTRUDED_LOAN', 0.0449, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_EQUITY', 0.0473, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_FOREIGN_LOAN', -0.0392, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_GOVT_BOND', 0.6933, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_LOAN_WRITEOFF', 0.0397, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_NEW', 7.0546, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_RMB_LOAN', 5.2194, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-01-01', 'SF_TRUST_LOAN', 0.0623, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_ABS', -0.0269, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_ACCEPTANCE', -0.2987, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_CORP_BOND', 0.1702, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_ENTRUDED_LOAN', -0.0228, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_EQUITY', 0.0076, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_FOREIGN_LOAN', -0.0281, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_GOVT_BOND', 1.6939, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_LOAN_WRITEOFF', 0.0542, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_NEW', 2.2331, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_RMB_LOAN', 0.6528, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-02-01', 'SF_TRUST_LOAN', -0.0330, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_ABS', -0.0076, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_ACCEPTANCE', 0.3632, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_CORP_BOND', -0.0905, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_ENTRUDED_LOAN', -0.0165, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_EQUITY', 0.0412, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_FOREIGN_LOAN', -0.0295, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_GOVT_BOND', 1.4866, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_LOAN_WRITEOFF', 0.1967, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_NEW', 5.8961, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_RMB_LOAN', 3.8234, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-03-01', 'SF_TRUST_LOAN', 0.0238, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_ABS', -0.0316, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_ACCEPTANCE', -0.2794, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_CORP_BOND', 0.2340, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_ENTRUDED_LOAN', -0.0002, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_EQUITY', 0.0391, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_FOREIGN_LOAN', -0.0130, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_GOVT_BOND', 0.9729, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_LOAN_WRITEOFF', 0.0631, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_NEW', 1.1599, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_RMB_LOAN', 0.0884, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-04-01', 'SF_TRUST_LOAN', -0.0077, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_ABS', -0.0102, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_ACCEPTANCE', -0.1164, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_CORP_BOND', 0.1496, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_ENTRUDED_LOAN', -0.0166, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_EQUITY', 0.0152, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_FOREIGN_LOAN', 0.0134, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_GOVT_BOND', 1.4585, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_LOAN_WRITEOFF', 0.0830, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_NEW', 2.2900, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_RMB_LOAN', 0.5923, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-05-01', 'SF_TRUST_LOAN', 0.0173, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_ABS', -0.0111, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_ACCEPTANCE', -0.1900, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_CORP_BOND', 0.2422, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_ENTRUDED_LOAN', -0.0400, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_EQUITY', 0.0203, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_FOREIGN_LOAN', 0.0325, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_GOVT_BOND', 1.3508, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_LOAN_WRITEOFF', 0.2478, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_NEW', 4.2251, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_RMB_LOAN', 2.3600, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-06-01', 'SF_TRUST_LOAN', 0.0816, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_ABS', 0.0011, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_ACCEPTANCE', -0.1638, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_CORP_BOND', 0.2748, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_ENTRUDED_LOAN', -0.0177, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_EQUITY', 0.0505, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_FOREIGN_LOAN', -0.0087, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_GOVT_BOND', 1.2482, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_LOAN_WRITEOFF', 0.0548, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_NEW', 1.1307, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_RMB_LOAN', -0.4296, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-07-01', 'SF_TRUST_LOAN', 0.0149, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_ABS', -0.0048, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_ACCEPTANCE', 0.1973, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_CORP_BOND', 0.1338, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_ENTRUDED_LOAN', -0.0165, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_EQUITY', 0.0456, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_FOREIGN_LOAN', -0.0091, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_GOVT_BOND', 1.3672, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_LOAN_WRITEOFF', 0.0875, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_NEW', 2.5660, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_RMB_LOAN', 0.6253, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-08-01', 'SF_TRUST_LOAN', 0.0350, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_ABS', 0.0073, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_ACCEPTANCE', 0.3234, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_CORP_BOND', 0.0136, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_ENTRUDED_LOAN', 0.0283, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_EQUITY', 0.0499, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_FOREIGN_LOAN', -0.0129, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_GOVT_BOND', 1.1893, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_LOAN_WRITEOFF', 0.2030, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_NEW', 3.5299, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_RMB_LOAN', 1.6081, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-09-01', 'SF_TRUST_LOAN', 0.0062, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_ABS', 0.0098, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_ACCEPTANCE', -0.2895, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_CORP_BOND', 0.2500, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_ENTRUDED_LOAN', 0.1654, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_EQUITY', 0.0695, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_FOREIGN_LOAN', -0.0200, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_GOVT_BOND', 0.4852, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_LOAN_WRITEOFF', 0.0452, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_NEW', 0.8178, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_RMB_LOAN', -0.0154, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-10-01', 'SF_TRUST_LOAN', 0.0155, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_ABS', -0.0013, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_ACCEPTANCE', 0.1488, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_CORP_BOND', 0.4145, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_ENTRUDED_LOAN', -0.0187, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_EQUITY', 0.0341, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_FOREIGN_LOAN', -0.0222, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_GOVT_BOND', 1.2077, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_LOAN_WRITEOFF', 0.1122, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_NEW', 2.4926, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_RMB_LOAN', 0.4096, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-11-01', 'SF_TRUST_LOAN', 0.0843, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_ABS', 0.0007, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_ACCEPTANCE', -0.1492, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_CORP_BOND', 0.1541, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_ENTRUDED_LOAN', 0.0308, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_EQUITY', 0.0559, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_FOREIGN_LOAN', -0.0675, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_GOVT_BOND', 0.6833, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_LOAN_WRITEOFF', 0.2845, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_NEW', 2.2132, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_RMB_LOAN', 0.9804, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2025-12-01', 'SF_TRUST_LOAN', 0.0679, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_ABS', -0.0099, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_ACCEPTANCE', 0.6293, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_CORP_BOND', 0.5033, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_ENTRUDED_LOAN', -0.0192, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_EQUITY', 0.0291, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_FOREIGN_LOAN', 0.0468, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_GOVT_BOND', 0.9764, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_LOAN_WRITEOFF', 0.0355, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_NEW', 7.2185, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_RMB_LOAN', 4.9016, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-01-01', 'SF_TRUST_LOAN', -0.0004, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_ABS', -0.0056, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_ACCEPTANCE', -0.1755, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_CORP_BOND', 0.1522, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_ENTRUDED_LOAN', -0.0181, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_EQUITY', 0.0454, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_FOREIGN_LOAN', -0.0036, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_GOVT_BOND', 1.4014, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_LOAN_WRITEOFF', 0.0379, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_NEW', 2.3837, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_RMB_LOAN', 0.8458, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-02-01', 'SF_TRUST_LOAN', 0.0310, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_ABS', -0.0132, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_ACCEPTANCE', 0.1258, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_CORP_BOND', 0.3910, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_ENTRUDED_LOAN', -0.0284, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_EQUITY', 0.0428, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_FOREIGN_LOAN', 0.0420, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_GOVT_BOND', 1.1658, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_LOAN_WRITEOFF', 0.2538, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_NEW', 5.2240, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_RMB_LOAN', 3.1522, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-03-01', 'SF_TRUST_LOAN', -0.0173, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_ABS', -0.0128, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_ACCEPTANCE', -0.5284, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_CORP_BOND', 0.4520, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_ENTRUDED_LOAN', -0.0283, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_EQUITY', 0.0835, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_FOREIGN_LOAN', 0.0184, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_GOVT_BOND', 0.9041, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_LOAN_WRITEOFF', 0.0580, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_NEW', 0.6238, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_RMB_LOAN', -0.4006, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-04-01', 'SF_TRUST_LOAN', -0.0129, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_ABS', -0.0241, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_ACCEPTANCE', -0.0685, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_CORP_BOND', 0.1680, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_ENTRUDED_LOAN', -0.0090, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_EQUITY', 0.0298, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_FOREIGN_LOAN', 0.0117, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_GOVT_BOND', 1.2236, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_LOAN_WRITEOFF', 0.1016, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_NEW', 2.0293, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_RMB_LOAN', 0.4965, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-05-01', 'SF_TRUST_LOAN', 0.0053, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_ABS', 0.0322, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_ACCEPTANCE', -0.1084, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_CORP_BOND', 0.4012, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_ENTRUDED_LOAN', 0.0243, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_EQUITY', 0.0628, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_FOREIGN_LOAN', 0.0455, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_GOVT_BOND', 0.7683, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_LOAN_WRITEOFF', 0.3188, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_NEW', 3.3671, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_RMB_LOAN', 1.7650, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-06-01', 'SF_TRUST_LOAN', -0.0503, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_ABS', -0.0159, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_ACCEPTANCE', -0.0530, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_CORP_BOND', 0.4536, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_ENTRUDED_LOAN', -0.0022, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_EQUITY', 0.1128, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_FOREIGN_LOAN', 0.0085, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_GOVT_BOND', 1.3176, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_LOAN_WRITEOFF', 0.0798, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_NEW', 1.4017, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_RMB_LOAN', -0.5896, '中国人民银行');
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2026-07-01', 'SF_TRUST_LOAN', -0.0226, '中国人民银行');

-- -----------------------------------------------------------
-- 3. 插入社融存量数据 (2025年全年)
-- 单位: 万亿元, metric_value2存储同比增速(%)
-- -----------------------------------------------------------
INSERT IGNORE INTO sea_pack.`macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2025-01-01', 'SF_STOCK', 415.1900, 8.000, '中国人民银行'),
('2025-02-01', 'SF_STOCK', 417.2900, 8.200, '中国人民银行'),
('2025-03-01', 'SF_STOCK', 422.9700, 8.400, '中国人民银行'),
('2025-04-01', 'SF_STOCK', 424.0000, 8.700, '中国人民银行'),
('2025-05-01', 'SF_STOCK', 426.1600, 8.700, '中国人民银行'),
('2025-06-01', 'SF_STOCK', 430.2400, 8.900, '中国人民银行'),
('2025-07-01', 'SF_STOCK', 431.2500, 9.000, '中国人民银行'),
('2025-08-01', 'SF_STOCK', 433.6500, 8.800, '中国人民银行'),
('2025-09-01', 'SF_STOCK', 437.0800, 8.700, '中国人民银行'),
('2025-10-01', 'SF_STOCK', 437.7100, 8.500, '中国人民银行'),
('2025-11-01', 'SF_STOCK', 440.0700, 8.500, '中国人民银行'),
('2025-12-01', 'SF_STOCK', 442.1200, 8.300, '中国人民银行'),
('2025-01-01', 'SF_RMB_LOAN_STOCK', 257.7100, 7.200, '中国人民银行'),
('2025-02-01', 'SF_RMB_LOAN_STOCK', 258.3600, 7.100, '中国人民银行'),
('2025-03-01', 'SF_RMB_LOAN_STOCK', 262.1800, 7.200, '中国人民银行'),
('2025-04-01', 'SF_RMB_LOAN_STOCK', 262.2700, 7.100, '中国人民银行'),
('2025-05-01', 'SF_RMB_LOAN_STOCK', 262.8600, 7.000, '中国人民银行'),
('2025-06-01', 'SF_RMB_LOAN_STOCK', 265.2200, 7.000, '中国人民银行'),
('2025-07-01', 'SF_RMB_LOAN_STOCK', 264.7900, 6.800, '中国人民银行'),
('2025-08-01', 'SF_RMB_LOAN_STOCK', 265.4200, 6.600, '中国人民银行'),
('2025-09-01', 'SF_RMB_LOAN_STOCK', 267.0300, 6.400, '中国人民银行'),
('2025-10-01', 'SF_RMB_LOAN_STOCK', 267.0100, 6.300, '中国人民银行'),
('2025-11-01', 'SF_RMB_LOAN_STOCK', 267.4200, 6.300, '中国人民银行'),
('2025-12-01', 'SF_RMB_LOAN_STOCK', 268.4000, 6.300, '中国人民银行'),
('2025-01-01', 'SF_FOREIGN_LOAN_STOCK', 1.2400, -29.400, '中国人民银行'),
('2025-02-01', 'SF_FOREIGN_LOAN_STOCK', 1.2200, -30.900, '中国人民银行'),
('2025-03-01', 'SF_FOREIGN_LOAN_STOCK', 1.1900, -34.500, '中国人民银行'),
('2025-04-01', 'SF_FOREIGN_LOAN_STOCK', 1.1800, -33.900, '中国人民银行'),
('2025-05-01', 'SF_FOREIGN_LOAN_STOCK', 1.1900, -31.500, '中国人民银行'),
('2025-06-01', 'SF_FOREIGN_LOAN_STOCK', 1.2200, -26.600, '中国人民银行'),
('2025-07-01', 'SF_FOREIGN_LOAN_STOCK', 1.2100, -23.200, '中国人民银行'),
('2025-08-01', 'SF_FOREIGN_LOAN_STOCK', 1.1900, -21.000, '中国人民银行'),
('2025-09-01', 'SF_FOREIGN_LOAN_STOCK', 1.1800, -18.000, '中国人民银行'),
('2025-10-01', 'SF_FOREIGN_LOAN_STOCK', 1.1500, -16.900, '中国人民银行'),
('2025-11-01', 'SF_FOREIGN_LOAN_STOCK', 1.1300, -16.500, '中国人民银行'),
('2025-12-01', 'SF_FOREIGN_LOAN_STOCK', 1.0500, -18.000, '中国人民银行'),
('2025-01-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2800, 0.400, '中国人民银行'),
('2025-02-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2500, 0.300, '中国人民银行'),
('2025-03-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2400, 0.600, '中国人民银行'),
('2025-04-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2400, 0.500, '中国人民银行'),
('2025-05-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2200, 0.400, '中国人民银行'),
('2025-06-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1800, 0.000, '中国人民银行'),
('2025-07-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1600, -0.400, '中国人民银行'),
('2025-08-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1500, -0.600, '中国人民银行'),
('2025-09-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1700, -0.700, '中国人民银行'),
('2025-10-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3400, 1.000, '中国人民银行'),
('2025-11-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3200, 1.000, '中国人民银行'),
('2025-12-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3500, 1.300, '中国人民银行'),
('2025-01-01', 'SF_TRUST_LOAN_STOCK', 4.3600, 9.700, '中国人民银行'),
('2025-02-01', 'SF_TRUST_LOAN_STOCK', 4.3300, 7.400, '中国人民银行'),
('2025-03-01', 'SF_TRUST_LOAN_STOCK', 4.3500, 6.200, '中国人民银行'),
('2025-04-01', 'SF_TRUST_LOAN_STOCK', 4.3500, 5.600, '中国人民银行'),
('2025-05-01', 'SF_TRUST_LOAN_STOCK', 4.3600, 5.400, '中国人民银行'),
('2025-06-01', 'SF_TRUST_LOAN_STOCK', 4.4500, 5.500, '中国人民银行'),
('2025-07-01', 'SF_TRUST_LOAN_STOCK', 4.4600, 5.900, '中国人民银行'),
('2025-08-01', 'SF_TRUST_LOAN_STOCK', 4.4900, 5.500, '中国人民银行'),
('2025-09-01', 'SF_TRUST_LOAN_STOCK', 4.5000, 5.700, '中国人民银行'),
('2025-10-01', 'SF_TRUST_LOAN_STOCK', 4.5200, 5.600, '中国人民银行'),
('2025-11-01', 'SF_TRUST_LOAN_STOCK', 4.6000, 7.400, '中国人民银行'),
('2025-12-01', 'SF_TRUST_LOAN_STOCK', 4.6700, 8.600, '中国人民银行'),
('2025-01-01', 'SF_ACCEPTANCE_STOCK', 2.6000, -14.600, '中国人民银行'),
('2025-02-01', 'SF_ACCEPTANCE_STOCK', 2.3000, -14.000, '中国人民银行'),
('2025-03-01', 'SF_ACCEPTANCE_STOCK', 2.6700, -12.100, '中国人民银行'),
('2025-04-01', 'SF_ACCEPTANCE_STOCK', 2.3900, -7.600, '中国人民银行'),
('2025-05-01', 'SF_ACCEPTANCE_STOCK', 2.2700, -7.400, '中国人民银行'),
('2025-06-01', 'SF_ACCEPTANCE_STOCK', 2.0800, -7.400, '中国人民银行'),
('2025-07-01', 'SF_ACCEPTANCE_STOCK', 1.9200, -10.400, '中国人民银行'),
('2025-08-01', 'SF_ACCEPTANCE_STOCK', 2.1200, -4.100, '中国人民银行'),
('2025-09-01', 'SF_ACCEPTANCE_STOCK', 2.4400, 4.400, '中国人民银行'),
('2025-10-01', 'SF_ACCEPTANCE_STOCK', 2.1500, -2.200, '中国人民银行'),
('2025-11-01', 'SF_ACCEPTANCE_STOCK', 2.3000, 0.400, '中国人民银行'),
('2025-12-01', 'SF_ACCEPTANCE_STOCK', 2.1500, -0.300, '中国人民银行'),
('2025-01-01', 'SF_CORP_BOND_STOCK', 32.6900, 4.100, '中国人民银行'),
('2025-02-01', 'SF_CORP_BOND_STOCK', 32.8000, 4.000, '中国人民银行'),
('2025-03-01', 'SF_CORP_BOND_STOCK', 32.5900, 2.400, '中国人民银行'),
('2025-04-01', 'SF_CORP_BOND_STOCK', 32.8000, 3.200, '中国人民银行'),
('2025-05-01', 'SF_CORP_BOND_STOCK', 32.9100, 3.400, '中国人民银行'),
('2025-06-01', 'SF_CORP_BOND_STOCK', 33.1300, 3.500, '中国人民银行'),
('2025-07-01', 'SF_CORP_BOND_STOCK', 33.3900, 3.800, '中国人民银行'),
('2025-08-01', 'SF_CORP_BOND_STOCK', 33.4700, 3.700, '中国人民银行'),
('2025-09-01', 'SF_CORP_BOND_STOCK', 33.5000, 4.500, '中国人民银行'),
('2025-10-01', 'SF_CORP_BOND_STOCK', 33.6800, 4.900, '中国人民银行'),
('2025-11-01', 'SF_CORP_BOND_STOCK', 34.0800, 5.600, '中国人民银行'),
('2025-12-01', 'SF_CORP_BOND_STOCK', 34.2400, 6.000, '中国人民银行'),
('2025-01-01', 'SF_GOVT_BOND_STOCK', 81.7800, 16.700, '中国人民银行'),
('2025-02-01', 'SF_GOVT_BOND_STOCK', 83.4700, 18.100, '中国人民银行'),
('2025-03-01', 'SF_GOVT_BOND_STOCK', 84.9600, 19.400, '中国人民银行'),
('2025-04-01', 'SF_GOVT_BOND_STOCK', 85.9300, 20.900, '中国人民银行'),
('2025-05-01', 'SF_GOVT_BOND_STOCK', 87.3900, 20.900, '中国人民银行'),
('2025-06-01', 'SF_GOVT_BOND_STOCK', 88.7400, 21.300, '中国人民银行'),
('2025-07-01', 'SF_GOVT_BOND_STOCK', 89.9900, 21.900, '中国人民银行'),
('2025-08-01', 'SF_GOVT_BOND_STOCK', 91.3600, 21.100, '中国人民银行'),
('2025-09-01', 'SF_GOVT_BOND_STOCK', 92.5500, 20.200, '中国人民银行'),
('2025-10-01', 'SF_GOVT_BOND_STOCK', 93.0300, 19.200, '中国人民银行'),
('2025-11-01', 'SF_GOVT_BOND_STOCK', 94.2400, 18.800, '中国人民银行'),
('2025-12-01', 'SF_GOVT_BOND_STOCK', 94.9200, 17.100, '中国人民银行'),
('2025-01-01', 'SF_EQUITY_STOCK', 11.7700, 2.600, '中国人民银行'),
('2025-02-01', 'SF_EQUITY_STOCK', 11.7800, 2.500, '中国人民银行'),
('2025-03-01', 'SF_EQUITY_STOCK', 11.8200, 2.700, '中国人民银行'),
('2025-04-01', 'SF_EQUITY_STOCK', 11.8600, 2.900, '中国人民银行'),
('2025-05-01', 'SF_EQUITY_STOCK', 11.8700, 2.900, '中国人民银行'),
('2025-06-01', 'SF_EQUITY_STOCK', 11.8900, 2.900, '中国人民银行'),
('2025-07-01', 'SF_EQUITY_STOCK', 11.9400, 3.200, '中国人民银行'),
('2025-08-01', 'SF_EQUITY_STOCK', 11.9900, 3.400, '中国人民银行'),
('2025-09-01', 'SF_EQUITY_STOCK', 12.0400, 3.800, '中国人民银行'),
('2025-10-01', 'SF_EQUITY_STOCK', 12.1100, 4.100, '中国人民银行'),
('2025-11-01', 'SF_EQUITY_STOCK', 12.1400, 4.000, '中国人民银行'),
('2025-12-01', 'SF_EQUITY_STOCK', 12.2000, 4.100, '中国人民银行'),
('2025-01-01', 'SF_ABS_STOCK', 0.7700, -42.300, '中国人民银行'),
('2025-02-01', 'SF_ABS_STOCK', 0.7400, -43.500, '中国人民银行'),
('2025-03-01', 'SF_ABS_STOCK', 0.7400, -41.400, '中国人民银行'),
('2025-04-01', 'SF_ABS_STOCK', 0.7100, -33.500, '中国人民银行'),
('2025-05-01', 'SF_ABS_STOCK', 0.7000, -31.800, '中国人民银行'),
('2025-06-01', 'SF_ABS_STOCK', 0.6800, -27.900, '中国人民银行'),
('2025-07-01', 'SF_ABS_STOCK', 0.6900, -23.900, '中国人民银行'),
('2025-08-01', 'SF_ABS_STOCK', 0.6800, -20.300, '中国人民银行'),
('2025-09-01', 'SF_ABS_STOCK', 0.6900, -17.900, '中国人民银行'),
('2025-10-01', 'SF_ABS_STOCK', 0.7000, -14.600, '中国人民银行'),
('2025-11-01', 'SF_ABS_STOCK', 0.7000, -13.600, '中国人民银行'),
('2025-12-01', 'SF_ABS_STOCK', 0.7000, -12.300, '中国人民银行'),
('2025-01-01', 'SF_LOAN_WRITEOFF_STOCK', 9.9800, 15.300, '中国人民银行'),
('2025-02-01', 'SF_LOAN_WRITEOFF_STOCK', 10.0300, 15.200, '中国人民银行'),
('2025-03-01', 'SF_LOAN_WRITEOFF_STOCK', 10.2300, 15.400, '中国人民银行'),
('2025-04-01', 'SF_LOAN_WRITEOFF_STOCK', 10.2900, 15.400, '中国人民银行'),
('2025-05-01', 'SF_LOAN_WRITEOFF_STOCK', 10.3700, 15.400, '中国人民银行'),
('2025-06-01', 'SF_LOAN_WRITEOFF_STOCK', 10.6200, 15.500, '中国人民银行'),
('2025-07-01', 'SF_LOAN_WRITEOFF_STOCK', 10.6800, 15.500, '中国人民银行'),
('2025-08-01', 'SF_LOAN_WRITEOFF_STOCK', 10.7600, 15.500, '中国人民银行'),
('2025-09-01', 'SF_LOAN_WRITEOFF_STOCK', 10.9700, 15.000, '中国人民银行'),
('2025-10-01', 'SF_LOAN_WRITEOFF_STOCK', 11.0100, 14.800, '中国人民银行'),
('2025-11-01', 'SF_LOAN_WRITEOFF_STOCK', 11.1300, 15.000, '中国人民银行'),
('2025-12-01', 'SF_LOAN_WRITEOFF_STOCK', 11.4100, 14.800, '中国人民银行');

-- -----------------------------------------------------------
-- 5. 验证查询
-- -----------------------------------------------------------
SELECT indicator_code, COUNT(*) as record_count FROM macro_monthly WHERE indicator_code LIKE 'SF_%' GROUP BY indicator_code ORDER BY indicator_code;

-- 预期结果: 存量 77 条 + 增量 209 条 = 合计 286 条