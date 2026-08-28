-- ============================================================
-- 社会融资规模数据插入脚本 (2021-2024)
-- 数据来源: 中国人民银行
-- ============================================================

-- -----------------------------------------------------------
-- 1. 插入社融存量指标元数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_STOCK', '社会融资规模存量', 'monthly', '万亿元', 'line', '#409EFF', 1);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_RMB_LOAN_STOCK', '人民币贷款(存量)', 'monthly', '万亿元', 'line', '#67C23A', 2);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_FOREIGN_LOAN_STOCK', '外币贷款(折合人民币)(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 3);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ENTRUDED_LOAN_STOCK', '委托贷款(存量)', 'monthly', '万亿元', 'line', '#909399', 4);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_TRUST_LOAN_STOCK', '信托贷款(存量)', 'monthly', '万亿元', 'line', '#F56C6C', 5);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ACCEPTANCE_STOCK', '未贴现银行承兑汇票(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 6);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_CORP_BOND_STOCK', '企业债券(存量)', 'monthly', '万亿元', 'line', '#409EFF', 7);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_GOVT_BOND_STOCK', '政府债券(存量)', 'monthly', '万亿元', 'line', '#67C23A', 8);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_EQUITY_STOCK', '非金融企业境内股票(存量)', 'monthly', '万亿元', 'line', '#E6A23C', 9);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ABS_STOCK', '存款类金融机构资产支持证券(存量)', 'monthly', '万亿元', 'line', '#909399', 10);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_LOAN_WRITEOFF_STOCK', '贷款核销(存量)', 'monthly', '万亿元', 'line', '#F56C6C', 11);

-- -----------------------------------------------------------
-- 2. 插入社融增量指标元数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_NEW', '社会融资规模增量', 'monthly', '万亿元', 'line', '#409EFF', 1);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_RMB_LOAN', '人民币贷款(增量)', 'monthly', '万亿元', 'line', '#67C23A', 2);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_FOREIGN_LOAN', '外币贷款(折合人民币)(增量)', 'monthly', '万亿元', 'line', '#E6A23C', 3);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ENTRUDED_LOAN', '委托贷款(增量)', 'monthly', '万亿元', 'line', '#909399', 4);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_TRUST_LOAN', '信托贷款(增量)', 'monthly', '万亿元', 'line', '#F56C6C', 5);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ACCEPTANCE', '未贴现银行承兑汇票(增量)', 'monthly', '万亿元', 'line', '#E6A23C', 6);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_CORP_BOND', '企业债券(增量)', 'monthly', '万亿元', 'line', '#409EFF', 7);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_GOVT_BOND', '政府债券(增量)', 'monthly', '万亿元', 'line', '#67C23A', 8);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_EQUITY', '非金融企业境内股票融资(增量)', 'monthly', '万亿元', 'line', '#E6A23C', 9);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_ABS', '存款类金融机构资产支持证券(增量)', 'monthly', '万亿元', 'line', '#909399', 10);
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('SF_LOAN_WRITEOFF', '贷款核销(增量)', 'monthly', '万亿元', 'line', '#F56C6C', 11);

-- -----------------------------------------------------------
-- 3. 插入社融存量数据 (2021-2024)
-- 单位: 万亿元, metric_value2存储同比增速(%)
-- -----------------------------------------------------------
INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_STOCK', 289.7400, 13.0000, '中国人民银行'),
('2021-02-01', 'SF_STOCK', 291.3600, 13.3000, '中国人民银行'),
('2021-03-01', 'SF_STOCK', 294.5600, 12.3000, '中国人民银行'),
('2021-04-01', 'SF_STOCK', 296.1500, 11.7000, '中国人民银行'),
('2021-05-01', 'SF_STOCK', 297.9800, 11.0000, '中国人民银行'),
('2021-06-01', 'SF_STOCK', 301.5600, 11.0000, '中国人民银行'),
('2021-07-01', 'SF_STOCK', 302.4700, 10.7000, '中国人民银行'),
('2021-08-01', 'SF_STOCK', 305.2900, 10.3000, '中国人民银行'),
('2021-09-01', 'SF_STOCK', 308.0500, 10.0000, '中国人民银行'),
('2021-10-01', 'SF_STOCK', 309.4500, 10.0000, '中国人民银行'),
('2021-11-01', 'SF_STOCK', 311.9000, 10.1000, '中国人民银行'),
('2021-12-01', 'SF_STOCK', 314.1200, 10.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_STOCK', 320.0300, 10.5000, '中国人民银行'),
('2022-02-01', 'SF_STOCK', 321.1200, 10.2000, '中国人民银行'),
('2022-03-01', 'SF_STOCK', 325.6300, 10.5000, '中国人民银行'),
('2022-04-01', 'SF_STOCK', 326.4700, 10.2000, '中国人民银行'),
('2022-05-01', 'SF_STOCK', 329.2000, 10.5000, '中国人民银行'),
('2022-06-01', 'SF_STOCK', 334.2800, 10.8000, '中国人民银行'),
('2022-07-01', 'SF_STOCK', 334.9000, 10.7000, '中国人民银行'),
('2022-08-01', 'SF_STOCK', 337.2200, 10.5000, '中国人民银行'),
('2022-09-01', 'SF_STOCK', 340.6500, 10.6000, '中国人民银行'),
('2022-10-01', 'SF_STOCK', 341.4200, 10.3000, '中国人民银行'),
('2022-11-01', 'SF_STOCK', 343.1900, 10.0000, '中国人民银行'),
('2022-12-01', 'SF_STOCK', 344.2200, 9.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_STOCK', 350.9300, 9.4000, '中国人民银行'),
('2023-02-01', 'SF_STOCK', 353.9700, 9.9000, '中国人民银行'),
('2023-03-01', 'SF_STOCK', 359.0200, 10.0000, '中国人民银行'),
('2023-04-01', 'SF_STOCK', 359.9500, 10.0000, '中国人民银行'),
('2023-05-01', 'SF_STOCK', 361.4200, 9.5000, '中国人民银行'),
('2023-06-01', 'SF_STOCK', 365.4500, 9.0000, '中国人民银行'),
('2023-07-01', 'SF_STOCK', 365.7700, 8.9000, '中国人民银行'),
('2023-08-01', 'SF_STOCK', 368.6100, 9.0000, '中国人民银行'),
('2023-09-01', 'SF_STOCK', 372.5000, 9.0000, '中国人民银行'),
('2023-10-01', 'SF_STOCK', 374.1700, 9.3000, '中国人民银行'),
('2023-11-01', 'SF_STOCK', 376.3900, 9.4000, '中国人民银行'),
('2023-12-01', 'SF_STOCK', 378.0800, 9.5000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_STOCK', 384.3200, 9.5000, '中国人民银行'),
('2024-02-01', 'SF_STOCK', 385.7100, 9.0000, '中国人民银行'),
('2024-03-01', 'SF_STOCK', 390.3200, 8.7000, '中国人民银行'),
('2024-04-01', 'SF_STOCK', 389.9300, 8.3000, '中国人民银行'),
('2024-05-01', 'SF_STOCK', 391.9300, 8.4000, '中国人民银行'),
('2024-06-01', 'SF_STOCK', 395.1000, 8.1000, '中国人民银行'),
('2024-07-01', 'SF_STOCK', 395.7200, 8.2000, '中国人民银行'),
('2024-08-01', 'SF_STOCK', 398.5600, 8.1000, '中国人民银行'),
('2024-09-01', 'SF_STOCK', 402.1900, 8.0000, '中国人民银行'),
('2024-10-01', 'SF_STOCK', 403.4500, 7.8000, '中国人民银行'),
('2024-11-01', 'SF_STOCK', 405.6000, 7.8000, '中国人民银行'),
('2024-12-01', 'SF_STOCK', 408.3400, 8.0000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_RMB_LOAN_STOCK', 175.4100, 13.1000, '中国人民银行'),
('2021-02-01', 'SF_RMB_LOAN_STOCK', 176.7600, 13.5000, '中国人民银行'),
('2021-03-01', 'SF_RMB_LOAN_STOCK', 179.5100, 13.0000, '中国人民银行'),
('2021-04-01', 'SF_RMB_LOAN_STOCK', 180.7900, 12.7000, '中国人民银行'),
('2021-05-01', 'SF_RMB_LOAN_STOCK', 182.2200, 12.5000, '中国人民银行'),
('2021-06-01', 'SF_RMB_LOAN_STOCK', 184.5400, 12.6000, '中国人民银行'),
('2021-07-01', 'SF_RMB_LOAN_STOCK', 185.3800, 12.4000, '中国人民银行'),
('2021-08-01', 'SF_RMB_LOAN_STOCK', 186.6500, 12.2000, '中国人民银行'),
('2021-09-01', 'SF_RMB_LOAN_STOCK', 188.4200, 12.0000, '中国人民银行'),
('2021-10-01', 'SF_RMB_LOAN_STOCK', 189.2000, 12.0000, '中国人民银行'),
('2021-11-01', 'SF_RMB_LOAN_STOCK', 190.5000, 11.8000, '中国人民银行'),
('2021-12-01', 'SF_RMB_LOAN_STOCK', 191.5400, 11.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_RMB_LOAN_STOCK', 195.7100, 11.6000, '中国人民银行'),
('2022-02-01', 'SF_RMB_LOAN_STOCK', 196.6200, 11.2000, '中国人民银行'),
('2022-03-01', 'SF_RMB_LOAN_STOCK', 199.8500, 11.3000, '中国人民银行'),
('2022-04-01', 'SF_RMB_LOAN_STOCK', 200.2100, 10.7000, '中国人民银行'),
('2022-05-01', 'SF_RMB_LOAN_STOCK', 202.0300, 10.9000, '中国人民银行'),
('2022-06-01', 'SF_RMB_LOAN_STOCK', 205.0900, 11.1000, '中国人民银行'),
('2022-07-01', 'SF_RMB_LOAN_STOCK', 205.5000, 10.9000, '中国人民银行'),
('2022-08-01', 'SF_RMB_LOAN_STOCK', 206.8300, 10.8000, '中国人民银行'),
('2022-09-01', 'SF_RMB_LOAN_STOCK', 209.4000, 11.1000, '中国人民银行'),
('2022-10-01', 'SF_RMB_LOAN_STOCK', 209.8400, 10.9000, '中国人民银行'),
('2022-11-01', 'SF_RMB_LOAN_STOCK', 210.9900, 10.8000, '中国人民银行'),
('2022-12-01', 'SF_RMB_LOAN_STOCK', 212.4300, 10.9000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_RMB_LOAN_STOCK', 218.1900, 11.1000, '中国人民银行'),
('2023-02-01', 'SF_RMB_LOAN_STOCK', 220.0100, 11.5000, '中国人民银行'),
('2023-03-01', 'SF_RMB_LOAN_STOCK', 223.9600, 11.7000, '中国人民银行'),
('2023-04-01', 'SF_RMB_LOAN_STOCK', 224.4000, 11.7000, '中国人民银行'),
('2023-05-01', 'SF_RMB_LOAN_STOCK', 225.6200, 11.3000, '中国人民银行'),
('2023-06-01', 'SF_RMB_LOAN_STOCK', 228.8600, 11.2000, '中国人民银行'),
('2023-07-01', 'SF_RMB_LOAN_STOCK', 228.9000, 11.0000, '中国人民银行'),
('2023-08-01', 'SF_RMB_LOAN_STOCK', 230.2400, 10.9000, '中国人民银行'),
('2023-09-01', 'SF_RMB_LOAN_STOCK', 232.7800, 10.7000, '中国人民银行'),
('2023-10-01', 'SF_RMB_LOAN_STOCK', 233.2600, 10.7000, '中国人民银行'),
('2023-11-01', 'SF_RMB_LOAN_STOCK', 234.3700, 10.7000, '中国人民银行'),
('2023-12-01', 'SF_RMB_LOAN_STOCK', 235.4800, 10.4000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_RMB_LOAN_STOCK', 240.3200, 10.1000, '中国人民银行'),
('2024-02-01', 'SF_RMB_LOAN_STOCK', 241.2900, 9.7000, '中国人民银行'),
('2024-03-01', 'SF_RMB_LOAN_STOCK', 244.5900, 9.2000, '中国人民银行'),
('2024-04-01', 'SF_RMB_LOAN_STOCK', 244.9200, 9.1000, '中国人民银行'),
('2024-05-01', 'SF_RMB_LOAN_STOCK', 245.7400, 8.9000, '中国人民银行'),
('2024-06-01', 'SF_RMB_LOAN_STOCK', 247.9300, 8.3000, '中国人民银行'),
('2024-07-01', 'SF_RMB_LOAN_STOCK', 247.8500, 8.3000, '中国人民银行'),
('2024-08-01', 'SF_RMB_LOAN_STOCK', 248.8900, 8.1000, '中国人民银行'),
('2024-09-01', 'SF_RMB_LOAN_STOCK', 250.8700, 7.8000, '中国人民银行'),
('2024-10-01', 'SF_RMB_LOAN_STOCK', 251.1600, 7.7000, '中国人民银行'),
('2024-11-01', 'SF_RMB_LOAN_STOCK', 251.6900, 7.4000, '中国人民银行'),
('2024-12-01', 'SF_RMB_LOAN_STOCK', 252.5300, 7.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_FOREIGN_LOAN_STOCK', 2.2000, 3.2000, '中国人民银行'),
('2021-02-01', 'SF_FOREIGN_LOAN_STOCK', 2.2500, 2.4000, '中国人民银行'),
('2021-03-01', 'SF_FOREIGN_LOAN_STOCK', 2.3100, -1.1000, '中国人民银行'),
('2021-04-01', 'SF_FOREIGN_LOAN_STOCK', 2.2500, -7.0000, '中国人民银行'),
('2021-05-01', 'SF_FOREIGN_LOAN_STOCK', 2.2100, -11.1000, '中国人民银行'),
('2021-06-01', 'SF_FOREIGN_LOAN_STOCK', 2.3200, -7.0000, '中国人民银行'),
('2021-07-01', 'SF_FOREIGN_LOAN_STOCK', 2.3100, -4.0000, '中国人民银行'),
('2021-08-01', 'SF_FOREIGN_LOAN_STOCK', 2.3500, -2.3000, '中国人民银行'),
('2021-09-01', 'SF_FOREIGN_LOAN_STOCK', 2.3500, -0.2000, '中国人民银行'),
('2021-10-01', 'SF_FOREIGN_LOAN_STOCK', 2.3100, 0.3000, '中国人民银行'),
('2021-11-01', 'SF_FOREIGN_LOAN_STOCK', 2.3000, 3.7000, '中国人民银行'),
('2021-12-01', 'SF_FOREIGN_LOAN_STOCK', 2.2300, 6.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_FOREIGN_LOAN_STOCK', 2.2600, 2.9000, '中国人民银行'),
('2022-02-01', 'SF_FOREIGN_LOAN_STOCK', 2.2900, 2.0000, '中国人民银行'),
('2022-03-01', 'SF_FOREIGN_LOAN_STOCK', 2.3300, 0.7000, '中国人民银行'),
('2022-04-01', 'SF_FOREIGN_LOAN_STOCK', 2.3500, 4.4000, '中国人民银行'),
('2022-05-01', 'SF_FOREIGN_LOAN_STOCK', 2.3400, 5.6000, '中国人民银行'),
('2022-06-01', 'SF_FOREIGN_LOAN_STOCK', 2.3300, 0.5000, '中国人民银行'),
('2022-07-01', 'SF_FOREIGN_LOAN_STOCK', 2.2300, -3.6000, '中国人民银行'),
('2022-08-01', 'SF_FOREIGN_LOAN_STOCK', 2.1900, -6.7000, '中国人民银行'),
('2022-09-01', 'SF_FOREIGN_LOAN_STOCK', 2.1800, -7.1000, '中国人民银行'),
('2022-10-01', 'SF_FOREIGN_LOAN_STOCK', 2.1300, -7.7000, '中国人民银行'),
('2022-11-01', 'SF_FOREIGN_LOAN_STOCK', 2.0700, -9.9000, '中国人民银行'),
('2022-12-01', 'SF_FOREIGN_LOAN_STOCK', 1.8400, -17.4000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_FOREIGN_LOAN_STOCK', 1.7800, -21.6000, '中国人民银行'),
('2023-02-01', 'SF_FOREIGN_LOAN_STOCK', 1.8600, -19.0000, '中国人民银行'),
('2023-03-01', 'SF_FOREIGN_LOAN_STOCK', 1.8800, -19.3000, '中国人民银行'),
('2023-04-01', 'SF_FOREIGN_LOAN_STOCK', 1.8600, -20.8000, '中国人民银行'),
('2023-05-01', 'SF_FOREIGN_LOAN_STOCK', 1.8700, -20.1000, '中国人民银行'),
('2023-06-01', 'SF_FOREIGN_LOAN_STOCK', 1.8900, -18.9000, '中国人民银行'),
('2023-07-01', 'SF_FOREIGN_LOAN_STOCK', 1.8300, -17.8000, '中国人民银行'),
('2023-08-01', 'SF_FOREIGN_LOAN_STOCK', 1.8200, -16.8000, '中国人民银行'),
('2023-09-01', 'SF_FOREIGN_LOAN_STOCK', 1.7600, -19.3000, '中国人民银行'),
('2023-10-01', 'SF_FOREIGN_LOAN_STOCK', 1.7800, -16.7000, '中国人民银行'),
('2023-11-01', 'SF_FOREIGN_LOAN_STOCK', 1.7200, -16.7000, '中国人民银行'),
('2023-12-01', 'SF_FOREIGN_LOAN_STOCK', 1.6600, -10.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_FOREIGN_LOAN_STOCK', 1.7600, -0.9000, '中国人民银行'),
('2024-02-01', 'SF_FOREIGN_LOAN_STOCK', 1.7600, -5.3000, '中国人民银行'),
('2024-03-01', 'SF_FOREIGN_LOAN_STOCK', 1.8100, -3.6000, '中国人民银行'),
('2024-04-01', 'SF_FOREIGN_LOAN_STOCK', 1.7800, -4.2000, '中国人民银行'),
('2024-05-01', 'SF_FOREIGN_LOAN_STOCK', 1.7300, -7.2000, '中国人民银行'),
('2024-06-01', 'SF_FOREIGN_LOAN_STOCK', 1.6600, -12.2000, '中国人民银行'),
('2024-07-01', 'SF_FOREIGN_LOAN_STOCK', 1.5700, -14.1000, '中国人民银行'),
('2024-08-01', 'SF_FOREIGN_LOAN_STOCK', 1.5000, -17.4000, '中国人民银行'),
('2024-09-01', 'SF_FOREIGN_LOAN_STOCK', 1.4300, -18.6000, '中国人民银行'),
('2024-10-01', 'SF_FOREIGN_LOAN_STOCK', 1.3900, -21.9000, '中国人民银行'),
('2024-11-01', 'SF_FOREIGN_LOAN_STOCK', 1.3500, -21.5000, '中国人民银行'),
('2024-12-01', 'SF_FOREIGN_LOAN_STOCK', 1.2900, -22.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_ENTRUDED_LOAN_STOCK', 11.0500, -3.5000, '中国人民银行'),
('2021-02-01', 'SF_ENTRUDED_LOAN_STOCK', 11.0400, -3.2000, '中国人民银行'),
('2021-03-01', 'SF_ENTRUDED_LOAN_STOCK', 11.0400, -2.8000, '中国人民银行'),
('2021-04-01', 'SF_ENTRUDED_LOAN_STOCK', 11.0200, -2.5000, '中国人民银行'),
('2021-05-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9800, -2.6000, '中国人民银行'),
('2021-06-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9300, -2.6000, '中国人民银行'),
('2021-07-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9100, -2.6000, '中国人民银行'),
('2021-08-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9300, -2.1000, '中国人民银行'),
('2021-09-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9300, -1.8000, '中国人民银行'),
('2021-10-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9100, -1.8000, '中国人民银行'),
('2021-11-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9200, -1.8000, '中国人民银行'),
('2021-12-01', 'SF_ENTRUDED_LOAN_STOCK', 10.8700, -1.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9300, -1.1000, '中国人民银行'),
('2022-02-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9200, -1.1000, '中国人民银行'),
('2022-03-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9300, -1.0000, '中国人民银行'),
('2022-04-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9300, -0.8000, '中国人民银行'),
('2022-05-01', 'SF_ENTRUDED_LOAN_STOCK', 10.9200, -0.5000, '中国人民银行'),
('2022-06-01', 'SF_ENTRUDED_LOAN_STOCK', 10.8800, -0.5000, '中国人民银行'),
('2022-07-01', 'SF_ENTRUDED_LOAN_STOCK', 10.8900, -0.2000, '中国人民银行'),
('2022-08-01', 'SF_ENTRUDED_LOAN_STOCK', 11.0600, 1.2000, '中国人民银行'),
('2022-09-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2100, 2.6000, '中国人民银行'),
('2022-10-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2600, 3.2000, '中国人民银行'),
('2022-11-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2500, 3.1000, '中国人民银行'),
('2022-12-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2400, 3.4000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3100, 3.5000, '中国人民银行'),
('2023-02-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3000, 3.5000, '中国人民银行'),
('2023-03-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3200, 3.5000, '中国人民银行'),
('2023-04-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3300, 3.6000, '中国人民银行'),
('2023-05-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3300, 3.8000, '中国人民银行'),
('2023-06-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3200, 4.1000, '中国人民银行'),
('2023-07-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3200, 4.0000, '中国人民银行'),
('2023-08-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3300, 2.4000, '中国人民银行'),
('2023-09-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3600, 1.3000, '中国人民银行'),
('2023-10-01', 'SF_ENTRUDED_LOAN_STOCK', 11.3100, 0.5000, '中国人民银行'),
('2023-11-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2700, 0.2000, '中国人民银行'),
('2023-12-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2700, 0.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2300, -0.7000, '中国人民银行'),
('2024-02-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2200, -0.7000, '中国人民银行'),
('2024-03-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1700, -1.3000, '中国人民银行'),
('2024-04-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1800, -1.3000, '中国人民银行'),
('2024-05-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1800, -1.3000, '中国人民银行'),
('2024-06-01', 'SF_ENTRUDED_LOAN_STOCK', 11.1800, -1.3000, '中国人民银行'),
('2024-07-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2100, -1.0000, '中国人民银行'),
('2024-08-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2100, -1.1000, '中国人民银行'),
('2024-09-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2500, -0.9000, '中国人民银行'),
('2024-10-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2300, -0.7000, '中国人民银行'),
('2024-11-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2100, -0.5000, '中国人民银行'),
('2024-12-01', 'SF_ENTRUDED_LOAN_STOCK', 11.2100, -0.5000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_TRUST_LOAN_STOCK', 6.2800, -16.2000, '中国人民银行'),
('2021-02-01', 'SF_TRUST_LOAN_STOCK', 6.1900, -16.8000, '中国人民银行'),
('2021-03-01', 'SF_TRUST_LOAN_STOCK', 6.0100, -19.2000, '中国人民银行'),
('2021-04-01', 'SF_TRUST_LOAN_STOCK', 5.8700, -21.0000, '中国人民银行'),
('2021-05-01', 'SF_TRUST_LOAN_STOCK', 5.7400, -22.4000, '中国人民银行'),
('2021-06-01', 'SF_TRUST_LOAN_STOCK', 5.6400, -22.9000, '中国人民银行'),
('2021-07-01', 'SF_TRUST_LOAN_STOCK', 5.4800, -23.6000, '中国人民银行'),
('2021-08-01', 'SF_TRUST_LOAN_STOCK', 5.3500, -25.2000, '中国人民银行'),
('2021-09-01', 'SF_TRUST_LOAN_STOCK', 5.1400, -27.0000, '中国人民银行'),
('2021-10-01', 'SF_TRUST_LOAN_STOCK', 5.0300, -27.6000, '中国人民银行'),
('2021-11-01', 'SF_TRUST_LOAN_STOCK', 4.8100, -29.3000, '中国人民银行'),
('2021-12-01', 'SF_TRUST_LOAN_STOCK', 4.3600, -31.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_TRUST_LOAN_STOCK', 4.2800, -31.9000, '中国人民银行'),
('2022-02-01', 'SF_TRUST_LOAN_STOCK', 4.2000, -32.1000, '中国人民银行'),
('2022-03-01', 'SF_TRUST_LOAN_STOCK', 4.1800, -30.5000, '中国人民银行'),
('2022-04-01', 'SF_TRUST_LOAN_STOCK', 4.1200, -29.9000, '中国人民银行'),
('2022-05-01', 'SF_TRUST_LOAN_STOCK', 4.0500, -29.4000, '中国人民银行'),
('2022-06-01', 'SF_TRUST_LOAN_STOCK', 3.9700, -29.6000, '中国人民银行'),
('2022-07-01', 'SF_TRUST_LOAN_STOCK', 3.9300, -28.3000, '中国人民银行'),
('2022-08-01', 'SF_TRUST_LOAN_STOCK', 3.8800, -27.4000, '中国人民银行'),
('2022-09-01', 'SF_TRUST_LOAN_STOCK', 3.8600, -24.8000, '中国人民银行'),
('2022-10-01', 'SF_TRUST_LOAN_STOCK', 3.8600, -23.3000, '中国人民银行'),
('2022-11-01', 'SF_TRUST_LOAN_STOCK', 3.8200, -20.6000, '中国人民银行'),
('2022-12-01', 'SF_TRUST_LOAN_STOCK', 3.7500, -14.0000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_TRUST_LOAN_STOCK', 3.7400, -12.6000, '中国人民银行'),
('2023-02-01', 'SF_TRUST_LOAN_STOCK', 3.7500, -10.9000, '中国人民银行'),
('2023-03-01', 'SF_TRUST_LOAN_STOCK', 3.7400, -10.4000, '中国人民银行'),
('2023-04-01', 'SF_TRUST_LOAN_STOCK', 3.7500, -8.8000, '中国人民银行'),
('2023-05-01', 'SF_TRUST_LOAN_STOCK', 3.7800, -6.7000, '中国人民银行'),
('2023-06-01', 'SF_TRUST_LOAN_STOCK', 3.7700, -5.1000, '中国人民银行'),
('2023-07-01', 'SF_TRUST_LOAN_STOCK', 3.7900, -3.6000, '中国人民银行'),
('2023-08-01', 'SF_TRUST_LOAN_STOCK', 3.7700, -2.9000, '中国人民银行'),
('2023-09-01', 'SF_TRUST_LOAN_STOCK', 3.8100, -1.4000, '中国人民银行'),
('2023-10-01', 'SF_TRUST_LOAN_STOCK', 3.8500, -0.3000, '中国人民银行'),
('2023-11-01', 'SF_TRUST_LOAN_STOCK', 3.8700, 1.2000, '中国人民银行'),
('2023-12-01', 'SF_TRUST_LOAN_STOCK', 3.9000, 4.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_TRUST_LOAN_STOCK', 3.9800, 6.3000, '中国人民银行'),
('2024-02-01', 'SF_TRUST_LOAN_STOCK', 4.0300, 7.7000, '中国人民银行'),
('2024-03-01', 'SF_TRUST_LOAN_STOCK', 4.1000, 9.6000, '中国人民银行'),
('2024-04-01', 'SF_TRUST_LOAN_STOCK', 4.1200, 9.7000, '中国人民银行'),
('2024-05-01', 'SF_TRUST_LOAN_STOCK', 4.1400, 9.4000, '中国人民银行'),
('2024-06-01', 'SF_TRUST_LOAN_STOCK', 4.2100, 11.8000, '中国人民银行'),
('2024-07-01', 'SF_TRUST_LOAN_STOCK', 4.2100, 11.1000, '中国人民银行'),
('2024-08-01', 'SF_TRUST_LOAN_STOCK', 4.2600, 13.0000, '中国人民银行'),
('2024-09-01', 'SF_TRUST_LOAN_STOCK', 4.2600, 11.8000, '中国人民银行'),
('2024-10-01', 'SF_TRUST_LOAN_STOCK', 4.2800, 11.1000, '中国人民银行'),
('2024-11-01', 'SF_TRUST_LOAN_STOCK', 4.2900, 10.8000, '中国人民银行'),
('2024-12-01', 'SF_TRUST_LOAN_STOCK', 4.3000, 10.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_ACCEPTANCE_STOCK', 4.0000, 15.1000, '中国人民银行'),
('2021-02-01', 'SF_ACCEPTANCE_STOCK', 4.0600, 32.0000, '中国人民银行'),
('2021-03-01', 'SF_ACCEPTANCE_STOCK', 3.8300, 14.1000, '中国人民银行'),
('2021-04-01', 'SF_ACCEPTANCE_STOCK', 3.6100, 5.9000, '中国人民银行'),
('2021-05-01', 'SF_ACCEPTANCE_STOCK', 3.5200, 0.7000, '中国人民银行'),
('2021-06-01', 'SF_ACCEPTANCE_STOCK', 3.5000, -5.8000, '中国人民银行'),
('2021-07-01', 'SF_ACCEPTANCE_STOCK', 3.2700, -9.3000, '中国人民银行'),
('2021-08-01', 'SF_ACCEPTANCE_STOCK', 3.2800, -12.5000, '中国人民银行'),
('2021-09-01', 'SF_ACCEPTANCE_STOCK', 3.2800, -15.8000, '中国人民银行'),
('2021-10-01', 'SF_ACCEPTANCE_STOCK', 3.1900, -15.7000, '中国人民银行'),
('2021-11-01', 'SF_ACCEPTANCE_STOCK', 3.1600, -15.3000, '中国人民银行'),
('2021-12-01', 'SF_ACCEPTANCE_STOCK', 3.0100, -14.0000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_ACCEPTANCE_STOCK', 3.4800, -12.9000, '中国人民银行'),
('2022-02-01', 'SF_ACCEPTANCE_STOCK', 3.0600, -24.7000, '中国人民银行'),
('2022-03-01', 'SF_ACCEPTANCE_STOCK', 3.0800, -19.5000, '中国人民银行'),
('2022-04-01', 'SF_ACCEPTANCE_STOCK', 2.8300, -21.8000, '中国人民银行'),
('2022-05-01', 'SF_ACCEPTANCE_STOCK', 2.7200, -22.7000, '中国人民银行'),
('2022-06-01', 'SF_ACCEPTANCE_STOCK', 2.8300, -19.2000, '中国人民银行'),
('2022-07-01', 'SF_ACCEPTANCE_STOCK', 2.5500, -21.9000, '中国人民银行'),
('2022-08-01', 'SF_ACCEPTANCE_STOCK', 2.9000, -11.5000, '中国人民银行'),
('2022-09-01', 'SF_ACCEPTANCE_STOCK', 2.9200, -11.2000, '中国人民银行'),
('2022-10-01', 'SF_ACCEPTANCE_STOCK', 2.7000, -15.5000, '中国人民银行'),
('2022-11-01', 'SF_ACCEPTANCE_STOCK', 2.7200, -13.8000, '中国人民银行'),
('2022-12-01', 'SF_ACCEPTANCE_STOCK', 2.6600, -11.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_ACCEPTANCE_STOCK', 2.9600, -14.9000, '中国人民银行'),
('2023-02-01', 'SF_ACCEPTANCE_STOCK', 2.9500, -3.3000, '中国人民银行'),
('2023-03-01', 'SF_ACCEPTANCE_STOCK', 3.1300, 1.6000, '中国人民银行'),
('2023-04-01', 'SF_ACCEPTANCE_STOCK', 3.0000, 6.0000, '中国人民银行'),
('2023-05-01', 'SF_ACCEPTANCE_STOCK', 2.8200, 3.6000, '中国人民银行'),
('2023-06-01', 'SF_ACCEPTANCE_STOCK', 2.7500, -2.8000, '中国人民银行'),
('2023-07-01', 'SF_ACCEPTANCE_STOCK', 2.5500, 0.0000, '中国人民银行'),
('2023-08-01', 'SF_ACCEPTANCE_STOCK', 2.6700, -8.2000, '中国人民银行'),
('2023-09-01', 'SF_ACCEPTANCE_STOCK', 2.9100, -0.3000, '中国人民银行'),
('2023-10-01', 'SF_ACCEPTANCE_STOCK', 2.6500, -1.8000, '中国人民银行'),
('2023-11-01', 'SF_ACCEPTANCE_STOCK', 2.6700, -1.7000, '中国人民银行'),
('2023-12-01', 'SF_ACCEPTANCE_STOCK', 2.4900, -6.7000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_ACCEPTANCE_STOCK', 3.0500, 3.0000, '中国人民银行'),
('2024-02-01', 'SF_ACCEPTANCE_STOCK', 2.6800, -9.3000, '中国人民银行'),
('2024-03-01', 'SF_ACCEPTANCE_STOCK', 3.0300, -3.1000, '中国人民银行'),
('2024-04-01', 'SF_ACCEPTANCE_STOCK', 2.5900, -13.8000, '中国人民银行'),
('2024-05-01', 'SF_ACCEPTANCE_STOCK', 2.4500, -13.0000, '中国人民银行'),
('2024-06-01', 'SF_ACCEPTANCE_STOCK', 2.2500, -18.2000, '中国人民银行'),
('2024-07-01', 'SF_ACCEPTANCE_STOCK', 2.1400, -16.2000, '中国人民银行'),
('2024-08-01', 'SF_ACCEPTANCE_STOCK', 2.2100, -17.3000, '中国人民银行'),
('2024-09-01', 'SF_ACCEPTANCE_STOCK', 2.3400, -19.6000, '中国人民银行'),
('2024-10-01', 'SF_ACCEPTANCE_STOCK', 2.2000, -17.2000, '中国人民银行'),
('2024-11-01', 'SF_ACCEPTANCE_STOCK', 2.2900, -14.4000, '中国人民银行'),
('2024-12-01', 'SF_ACCEPTANCE_STOCK', 2.1600, -13.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_CORP_BOND_STOCK', 27.8300, 16.3000, '中国人民银行'),
('2021-02-01', 'SF_CORP_BOND_STOCK', 27.9300, 15.1000, '中国人民银行'),
('2021-03-01', 'SF_CORP_BOND_STOCK', 28.1700, 11.7000, '中国人民银行'),
('2021-04-01', 'SF_CORP_BOND_STOCK', 28.3900, 8.9000, '中国人民银行'),
('2021-05-01', 'SF_CORP_BOND_STOCK', 28.2600, 7.1000, '中国人民银行'),
('2021-06-01', 'SF_CORP_BOND_STOCK', 28.5800, 6.7000, '中国人民银行'),
('2021-07-01', 'SF_CORP_BOND_STOCK', 28.8100, 6.8000, '中国人民银行'),
('2021-08-01', 'SF_CORP_BOND_STOCK', 29.1900, 7.0000, '中国人民银行'),
('2021-09-01', 'SF_CORP_BOND_STOCK', 29.2500, 6.9000, '中国人民银行'),
('2021-10-01', 'SF_CORP_BOND_STOCK', 29.3600, 6.7000, '中国人民银行'),
('2021-11-01', 'SF_CORP_BOND_STOCK', 29.7200, 7.7000, '中国人民银行'),
('2021-12-01', 'SF_CORP_BOND_STOCK', 29.9300, 8.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_CORP_BOND_STOCK', 30.4500, 9.4000, '中国人民银行'),
('2022-02-01', 'SF_CORP_BOND_STOCK', 30.7600, 10.1000, '中国人民银行'),
('2022-03-01', 'SF_CORP_BOND_STOCK', 31.0600, 10.2000, '中国人民银行'),
('2022-04-01', 'SF_CORP_BOND_STOCK', 31.3000, 10.3000, '中国人民银行'),
('2022-05-01', 'SF_CORP_BOND_STOCK', 31.2900, 10.7000, '中国人民银行'),
('2022-06-01', 'SF_CORP_BOND_STOCK', 31.4800, 10.1000, '中国人民银行'),
('2022-07-01', 'SF_CORP_BOND_STOCK', 31.5000, 9.3000, '中国人民银行'),
('2022-08-01', 'SF_CORP_BOND_STOCK', 31.5400, 8.0000, '中国人民银行'),
('2022-09-01', 'SF_CORP_BOND_STOCK', 31.4900, 7.7000, '中国人民银行'),
('2022-10-01', 'SF_CORP_BOND_STOCK', 31.6500, 7.8000, '中国人民银行'),
('2022-11-01', 'SF_CORP_BOND_STOCK', 31.6000, 6.3000, '中国人民银行'),
('2022-12-01', 'SF_CORP_BOND_STOCK', 31.0100, 3.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_CORP_BOND_STOCK', 31.0200, 1.9000, '中国人民银行'),
('2023-02-01', 'SF_CORP_BOND_STOCK', 31.3000, 1.8000, '中国人民银行'),
('2023-03-01', 'SF_CORP_BOND_STOCK', 31.4200, 1.2000, '中国人民银行'),
('2023-04-01', 'SF_CORP_BOND_STOCK', 31.4900, 0.6000, '中国人民银行'),
('2023-05-01', 'SF_CORP_BOND_STOCK', 31.2400, -0.1000, '中国人民银行'),
('2023-06-01', 'SF_CORP_BOND_STOCK', 31.3400, -0.4000, '中国人民银行'),
('2023-07-01', 'SF_CORP_BOND_STOCK', 31.3700, -0.4000, '中国人民银行'),
('2023-08-01', 'SF_CORP_BOND_STOCK', 31.4600, -0.2000, '中国人民银行'),
('2023-09-01', 'SF_CORP_BOND_STOCK', 31.3900, -0.3000, '中国人民银行'),
('2023-10-01', 'SF_CORP_BOND_STOCK', 31.4400, -0.7000, '中国人民银行'),
('2023-11-01', 'SF_CORP_BOND_STOCK', 31.4800, -0.4000, '中国人民银行'),
('2023-12-01', 'SF_CORP_BOND_STOCK', 31.1100, 0.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_CORP_BOND_STOCK', 31.4100, 1.3000, '中国人民银行'),
('2024-02-01', 'SF_CORP_BOND_STOCK', 31.5300, 0.7000, '中国人民银行'),
('2024-03-01', 'SF_CORP_BOND_STOCK', 31.8300, 1.3000, '中国人民银行'),
('2024-04-01', 'SF_CORP_BOND_STOCK', 31.7800, 0.9000, '中国人民银行'),
('2024-05-01', 'SF_CORP_BOND_STOCK', 31.8400, 1.9000, '中国人民银行'),
('2024-06-01', 'SF_CORP_BOND_STOCK', 32.0200, 2.2000, '中国人民银行'),
('2024-07-01', 'SF_CORP_BOND_STOCK', 32.1800, 2.6000, '中国人民银行'),
('2024-08-01', 'SF_CORP_BOND_STOCK', 32.2700, 2.6000, '中国人民银行'),
('2024-09-01', 'SF_CORP_BOND_STOCK', 32.0700, 2.2000, '中国人民银行'),
('2024-10-01', 'SF_CORP_BOND_STOCK', 32.1100, 2.2000, '中国人民银行'),
('2024-11-01', 'SF_CORP_BOND_STOCK', 32.2800, 2.5000, '中国人民银行'),
('2024-12-01', 'SF_CORP_BOND_STOCK', 32.3000, 3.8000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_GOVT_BOND_STOCK', 46.2900, 20.3000, '中国人民银行'),
('2021-02-01', 'SF_GOVT_BOND_STOCK', 46.3900, 20.0000, '中国人民银行'),
('2021-03-01', 'SF_GOVT_BOND_STOCK', 46.7100, 18.8000, '中国人民银行'),
('2021-04-01', 'SF_GOVT_BOND_STOCK', 47.0800, 18.8000, '中国人民银行'),
('2021-05-01', 'SF_GOVT_BOND_STOCK', 47.7500, 17.1000, '中国人民银行'),
('2021-06-01', 'SF_GOVT_BOND_STOCK', 48.5000, 16.8000, '中国人民银行'),
('2021-07-01', 'SF_GOVT_BOND_STOCK', 48.6800, 15.7000, '中国人民银行'),
('2021-08-01', 'SF_GOVT_BOND_STOCK', 49.6600, 14.3000, '中国人民银行'),
('2021-09-01', 'SF_GOVT_BOND_STOCK', 50.4600, 13.5000, '中国人民银行'),
('2021-10-01', 'SF_GOVT_BOND_STOCK', 51.0800, 13.6000, '中国人民银行'),
('2021-11-01', 'SF_GOVT_BOND_STOCK', 51.9000, 14.4000, '中国人民银行'),
('2021-12-01', 'SF_GOVT_BOND_STOCK', 53.0600, 15.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_GOVT_BOND_STOCK', 53.6700, 15.9000, '中国人民银行'),
('2022-02-01', 'SF_GOVT_BOND_STOCK', 53.9400, 16.3000, '中国人民银行'),
('2022-03-01', 'SF_GOVT_BOND_STOCK', 54.6500, 17.0000, '中国人民银行'),
('2022-04-01', 'SF_GOVT_BOND_STOCK', 55.0400, 16.9000, '中国人民银行'),
('2022-05-01', 'SF_GOVT_BOND_STOCK', 56.1000, 17.5000, '中国人民银行'),
('2022-06-01', 'SF_GOVT_BOND_STOCK', 57.7200, 19.0000, '中国人民银行'),
('2022-07-01', 'SF_GOVT_BOND_STOCK', 58.1200, 19.4000, '中国人民银行'),
('2022-08-01', 'SF_GOVT_BOND_STOCK', 58.4200, 17.6000, '中国人民银行'),
('2022-09-01', 'SF_GOVT_BOND_STOCK', 58.9800, 16.9000, '中国人民银行'),
('2022-10-01', 'SF_GOVT_BOND_STOCK', 59.2500, 16.0000, '中国人民银行'),
('2022-11-01', 'SF_GOVT_BOND_STOCK', 59.9100, 15.4000, '中国人民银行'),
('2022-12-01', 'SF_GOVT_BOND_STOCK', 60.1900, 13.4000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_GOVT_BOND_STOCK', 60.6000, 12.9000, '中国人民银行'),
('2023-02-01', 'SF_GOVT_BOND_STOCK', 61.4100, 13.9000, '中国人民银行'),
('2023-03-01', 'SF_GOVT_BOND_STOCK', 62.0200, 13.5000, '中国人民银行'),
('2023-04-01', 'SF_GOVT_BOND_STOCK', 62.4700, 13.5000, '中国人民银行'),
('2023-05-01', 'SF_GOVT_BOND_STOCK', 63.0300, 12.4000, '中国人民银行'),
('2023-06-01', 'SF_GOVT_BOND_STOCK', 63.5700, 10.1000, '中国人民银行'),
('2023-07-01', 'SF_GOVT_BOND_STOCK', 63.9800, 10.1000, '中国人民银行'),
('2023-08-01', 'SF_GOVT_BOND_STOCK', 65.1500, 11.5000, '中国人民银行'),
('2023-09-01', 'SF_GOVT_BOND_STOCK', 66.1400, 12.2000, '中国人民银行'),
('2023-10-01', 'SF_GOVT_BOND_STOCK', 67.7100, 14.3000, '中国人民银行'),
('2023-11-01', 'SF_GOVT_BOND_STOCK', 68.8600, 14.9000, '中国人民银行'),
('2023-12-01', 'SF_GOVT_BOND_STOCK', 69.7900, 16.0000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_GOVT_BOND_STOCK', 70.0900, 15.7000, '中国人民银行'),
('2024-02-01', 'SF_GOVT_BOND_STOCK', 70.6900, 15.1000, '中国人民银行'),
('2024-03-01', 'SF_GOVT_BOND_STOCK', 71.1500, 14.7000, '中国人民银行'),
('2024-04-01', 'SF_GOVT_BOND_STOCK', 71.0600, 13.7000, '中国人民银行'),
('2024-05-01', 'SF_GOVT_BOND_STOCK', 72.2800, 14.7000, '中国人民银行'),
('2024-06-01', 'SF_GOVT_BOND_STOCK', 73.1300, 15.0000, '中国人民银行'),
('2024-07-01', 'SF_GOVT_BOND_STOCK', 73.8200, 15.4000, '中国人民银行'),
('2024-08-01', 'SF_GOVT_BOND_STOCK', 75.4400, 15.8000, '中国人民银行'),
('2024-09-01', 'SF_GOVT_BOND_STOCK', 76.9700, 16.4000, '中国人民银行'),
('2024-10-01', 'SF_GOVT_BOND_STOCK', 78.0200, 15.2000, '中国人民银行'),
('2024-11-01', 'SF_GOVT_BOND_STOCK', 79.3300, 15.2000, '中国人民银行'),
('2024-12-01', 'SF_GOVT_BOND_STOCK', 81.0900, 16.2000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_EQUITY_STOCK', 8.3500, 12.5000, '中国人民银行'),
('2021-02-01', 'SF_EQUITY_STOCK', 8.4200, 12.8000, '中国人民银行'),
('2021-03-01', 'SF_EQUITY_STOCK', 8.5000, 13.5000, '中国人民银行'),
('2021-04-01', 'SF_EQUITY_STOCK', 8.5800, 14.2000, '中国人民银行'),
('2021-05-01', 'SF_EQUITY_STOCK', 8.6500, 14.6000, '中国人民银行'),
('2021-06-01', 'SF_EQUITY_STOCK', 8.7400, 15.0000, '中国人民银行'),
('2021-07-01', 'SF_EQUITY_STOCK', 8.8400, 14.4000, '中国人民银行'),
('2021-08-01', 'SF_EQUITY_STOCK', 8.9900, 14.4000, '中国人民银行'),
('2021-09-01', 'SF_EQUITY_STOCK', 9.0600, 13.8000, '中国人民银行'),
('2021-10-01', 'SF_EQUITY_STOCK', 9.1500, 13.5000, '中国人民银行'),
('2021-11-01', 'SF_EQUITY_STOCK', 9.2800, 14.0000, '中国人民银行'),
('2021-12-01', 'SF_EQUITY_STOCK', 9.4600, 14.7000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_EQUITY_STOCK', 9.6100, 15.1000, '中国人民银行'),
('2022-02-01', 'SF_EQUITY_STOCK', 9.6600, 14.8000, '中国人民银行'),
('2022-03-01', 'SF_EQUITY_STOCK', 9.7600, 14.9000, '中国人民银行'),
('2022-04-01', 'SF_EQUITY_STOCK', 9.8800, 15.2000, '中国人民银行'),
('2022-05-01', 'SF_EQUITY_STOCK', 9.9100, 14.5000, '中国人民银行'),
('2022-06-01', 'SF_EQUITY_STOCK', 9.9600, 14.0000, '中国人民银行'),
('2022-07-01', 'SF_EQUITY_STOCK', 10.1100, 14.4000, '中国人民银行'),
('2022-08-01', 'SF_EQUITY_STOCK', 10.2300, 13.9000, '中国人民银行'),
('2022-09-01', 'SF_EQUITY_STOCK', 10.3400, 14.0000, '中国人民银行'),
('2022-10-01', 'SF_EQUITY_STOCK', 10.4100, 13.9000, '中国人民银行'),
('2022-11-01', 'SF_EQUITY_STOCK', 10.4900, 13.1000, '中国人民银行'),
('2022-12-01', 'SF_EQUITY_STOCK', 10.6400, 12.4000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_EQUITY_STOCK', 10.7300, 11.7000, '中国人民银行'),
('2023-02-01', 'SF_EQUITY_STOCK', 10.7900, 11.7000, '中国人民银行'),
('2023-03-01', 'SF_EQUITY_STOCK', 10.8500, 11.2000, '中国人民银行'),
('2023-04-01', 'SF_EQUITY_STOCK', 10.9500, 10.9000, '中国人民银行'),
('2023-05-01', 'SF_EQUITY_STOCK', 11.0300, 11.3000, '中国人民银行'),
('2023-06-01', 'SF_EQUITY_STOCK', 11.1000, 11.4000, '中国人民银行'),
('2023-07-01', 'SF_EQUITY_STOCK', 11.1800, 10.6000, '中国人民银行'),
('2023-08-01', 'SF_EQUITY_STOCK', 11.2800, 10.2000, '中国人民银行'),
('2023-09-01', 'SF_EQUITY_STOCK', 11.3100, 9.4000, '中国人民银行'),
('2023-10-01', 'SF_EQUITY_STOCK', 11.3400, 8.9000, '中国人民银行'),
('2023-11-01', 'SF_EQUITY_STOCK', 11.3800, 8.4000, '中国人民银行'),
('2023-12-01', 'SF_EQUITY_STOCK', 11.4300, 7.5000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_EQUITY_STOCK', 11.4700, 6.9000, '中国人民银行'),
('2024-02-01', 'SF_EQUITY_STOCK', 11.4800, 6.4000, '中国人民银行'),
('2024-03-01', 'SF_EQUITY_STOCK', 11.5100, 6.0000, '中国人民银行'),
('2024-04-01', 'SF_EQUITY_STOCK', 11.5300, 5.2000, '中国人民银行'),
('2024-05-01', 'SF_EQUITY_STOCK', 11.5400, 4.6000, '中国人民银行'),
('2024-06-01', 'SF_EQUITY_STOCK', 11.5500, 4.1000, '中国人民银行'),
('2024-07-01', 'SF_EQUITY_STOCK', 11.5800, 3.6000, '中国人民银行'),
('2024-08-01', 'SF_EQUITY_STOCK', 11.5900, 2.7000, '中国人民银行'),
('2024-09-01', 'SF_EQUITY_STOCK', 11.6000, 2.6000, '中国人民银行'),
('2024-10-01', 'SF_EQUITY_STOCK', 11.6300, 2.5000, '中国人民银行'),
('2024-11-01', 'SF_EQUITY_STOCK', 11.6700, 2.6000, '中国人民银行'),
('2024-12-01', 'SF_EQUITY_STOCK', 11.7200, 2.5000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_ABS_STOCK', 1.8900, 9.3000, '中国人民银行'),
('2021-02-01', 'SF_ABS_STOCK', 1.8700, 11.8000, '中国人民银行'),
('2021-03-01', 'SF_ABS_STOCK', 1.9200, 15.7000, '中国人民银行'),
('2021-04-01', 'SF_ABS_STOCK', 1.9400, 18.2000, '中国人民银行'),
('2021-05-01', 'SF_ABS_STOCK', 1.9600, 20.4000, '中国人民银行'),
('2021-06-01', 'SF_ABS_STOCK', 1.9600, 20.4000, '中国人民银行'),
('2021-07-01', 'SF_ABS_STOCK', 1.9600, 26.0000, '中国人民银行'),
('2021-08-01', 'SF_ABS_STOCK', 1.9900, 25.2000, '中国人民银行'),
('2021-09-01', 'SF_ABS_STOCK', 2.0500, 25.8000, '中国人民银行'),
('2021-10-01', 'SF_ABS_STOCK', 2.0400, 23.5000, '中国人民银行'),
('2021-11-01', 'SF_ABS_STOCK', 2.1100, 18.8000, '中国人民银行'),
('2021-12-01', 'SF_ABS_STOCK', 2.1700, 14.7000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_ABS_STOCK', 2.1500, 13.7000, '中国人民银行'),
('2022-02-01', 'SF_ABS_STOCK', 2.1500, 14.5000, '中国人民银行'),
('2022-03-01', 'SF_ABS_STOCK', 2.1400, 11.1000, '中国人民银行'),
('2022-04-01', 'SF_ABS_STOCK', 2.1100, 9.0000, '中国人民银行'),
('2022-05-01', 'SF_ABS_STOCK', 2.0900, 6.7000, '中国人民银行'),
('2022-06-01', 'SF_ABS_STOCK', 2.0900, 6.7000, '中国人民银行'),
('2022-07-01', 'SF_ABS_STOCK', 2.0600, 5.0000, '中国人民银行'),
('2022-08-01', 'SF_ABS_STOCK', 2.0600, 3.6000, '中国人民银行'),
('2022-09-01', 'SF_ABS_STOCK', 2.0400, -0.6000, '中国人民银行'),
('2022-10-01', 'SF_ABS_STOCK', 2.0200, -0.6000, '中国人民银行'),
('2022-11-01', 'SF_ABS_STOCK', 2.0000, -5.2000, '中国人民银行'),
('2022-12-01', 'SF_ABS_STOCK', 1.9900, -8.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_ABS_STOCK', 1.9500, -9.2000, '中国人民银行'),
('2023-02-01', 'SF_ABS_STOCK', 1.9200, -10.3000, '中国人民银行'),
('2023-03-01', 'SF_ABS_STOCK', 1.9100, -10.6000, '中国人民银行'),
('2023-04-01', 'SF_ABS_STOCK', 1.8700, -11.4000, '中国人民银行'),
('2023-05-01', 'SF_ABS_STOCK', 1.8400, -12.3000, '中国人民银行'),
('2023-06-01', 'SF_ABS_STOCK', 1.8300, -12.3000, '中国人民银行'),
('2023-07-01', 'SF_ABS_STOCK', 1.8000, -12.2000, '中国人民银行'),
('2023-08-01', 'SF_ABS_STOCK', 1.7900, -12.9000, '中国人民银行'),
('2023-09-01', 'SF_ABS_STOCK', 1.7700, -12.9000, '中国人民银行'),
('2023-10-01', 'SF_ABS_STOCK', 1.5200, -24.8000, '中国人民银行'),
('2023-11-01', 'SF_ABS_STOCK', 1.3900, -30.7000, '中国人民银行'),
('2023-12-01', 'SF_ABS_STOCK', 1.3600, -31.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_ABS_STOCK', 1.3400, -31.5000, '中国人民银行'),
('2024-02-01', 'SF_ABS_STOCK', 1.3200, -31.6000, '中国人民银行'),
('2024-03-01', 'SF_ABS_STOCK', 1.2600, -34.1000, '中国人民银行'),
('2024-04-01', 'SF_ABS_STOCK', 1.0600, -43.3000, '中国人民银行'),
('2024-05-01', 'SF_ABS_STOCK', 1.0200, -44.6000, '中国人民银行'),
('2024-06-01', 'SF_ABS_STOCK', 0.9500, -48.3000, '中国人民银行'),
('2024-07-01', 'SF_ABS_STOCK', 0.9000, -50.1000, '中国人民银行'),
('2024-08-01', 'SF_ABS_STOCK', 0.8500, -52.4000, '中国人民银行'),
('2024-09-01', 'SF_ABS_STOCK', 0.8400, -52.8000, '中国人民银行'),
('2024-10-01', 'SF_ABS_STOCK', 0.8200, -46.3000, '中国人民银行'),
('2024-11-01', 'SF_ABS_STOCK', 0.8100, -41.9000, '中国人民银行'),
('2024-12-01', 'SF_ABS_STOCK', 0.7900, -41.5000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2021-01-01', 'SF_LOAN_WRITEOFF_STOCK', 5.3200, 30.2000, '中国人民银行'),
('2021-02-01', 'SF_LOAN_WRITEOFF_STOCK', 5.3500, 30.3000, '中国人民银行'),
('2021-03-01', 'SF_LOAN_WRITEOFF_STOCK', 5.4600, 29.4000, '中国人民银行'),
('2021-04-01', 'SF_LOAN_WRITEOFF_STOCK', 5.5100, 29.1000, '中国人民银行'),
('2021-05-01', 'SF_LOAN_WRITEOFF_STOCK', 5.5400, 28.7000, '中国人民银行'),
('2021-06-01', 'SF_LOAN_WRITEOFF_STOCK', 5.7200, 26.8000, '中国人民银行'),
('2021-07-01', 'SF_LOAN_WRITEOFF_STOCK', 5.7500, 25.3000, '中国人民银行'),
('2021-08-01', 'SF_LOAN_WRITEOFF_STOCK', 5.8000, 24.7000, '中国人民银行'),
('2021-09-01', 'SF_LOAN_WRITEOFF_STOCK', 5.9500, 23.2000, '中国人民银行'),
('2021-10-01', 'SF_LOAN_WRITEOFF_STOCK', 5.9900, 22.6000, '中国人民银行'),
('2021-11-01', 'SF_LOAN_WRITEOFF_STOCK', 6.0400, 22.1000, '中国人民银行'),
('2021-12-01', 'SF_LOAN_WRITEOFF_STOCK', 6.3200, 19.5000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2022-01-01', 'SF_LOAN_WRITEOFF_STOCK', 6.3400, 19.2000, '中国人民银行'),
('2022-02-01', 'SF_LOAN_WRITEOFF_STOCK', 6.3600, 18.9000, '中国人民银行'),
('2022-03-01', 'SF_LOAN_WRITEOFF_STOCK', 6.5200, 19.4000, '中国人民银行'),
('2022-04-01', 'SF_LOAN_WRITEOFF_STOCK', 6.5700, 19.3000, '中国人民银行'),
('2022-05-01', 'SF_LOAN_WRITEOFF_STOCK', 6.6200, 19.4000, '中国人民银行'),
('2022-06-01', 'SF_LOAN_WRITEOFF_STOCK', 6.8000, 18.8000, '中国人民银行'),
('2022-07-01', 'SF_LOAN_WRITEOFF_STOCK', 6.9000, 20.0000, '中国人民银行'),
('2022-08-01', 'SF_LOAN_WRITEOFF_STOCK', 6.9600, 20.0000, '中国人民银行'),
('2022-09-01', 'SF_LOAN_WRITEOFF_STOCK', 7.1100, 19.5000, '中国人民银行'),
('2022-10-01', 'SF_LOAN_WRITEOFF_STOCK', 7.1400, 19.3000, '中国人民银行'),
('2022-11-01', 'SF_LOAN_WRITEOFF_STOCK', 7.2100, 19.2000, '中国人民银行'),
('2022-12-01', 'SF_LOAN_WRITEOFF_STOCK', 7.3400, 16.3000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2023-01-01', 'SF_LOAN_WRITEOFF_STOCK', 7.5400, 16.6000, '中国人民银行'),
('2023-02-01', 'SF_LOAN_WRITEOFF_STOCK', 7.5800, 16.7000, '中国人民银行'),
('2023-03-01', 'SF_LOAN_WRITEOFF_STOCK', 7.7000, 15.7000, '中国人民银行'),
('2023-04-01', 'SF_LOAN_WRITEOFF_STOCK', 7.7500, 15.5000, '中国人民银行'),
('2023-05-01', 'SF_LOAN_WRITEOFF_STOCK', 7.8000, 15.4000, '中国人民银行'),
('2023-06-01', 'SF_LOAN_WRITEOFF_STOCK', 7.9800, 14.9000, '中国人民银行'),
('2023-07-01', 'SF_LOAN_WRITEOFF_STOCK', 8.0200, 13.8000, '中国人民银行'),
('2023-08-01', 'SF_LOAN_WRITEOFF_STOCK', 8.0800, 13.6000, '中国人民银行'),
('2023-09-01', 'SF_LOAN_WRITEOFF_STOCK', 8.2600, 13.6000, '中国人民银行'),
('2023-10-01', 'SF_LOAN_WRITEOFF_STOCK', 8.3000, 13.6000, '中国人民银行'),
('2023-11-01', 'SF_LOAN_WRITEOFF_STOCK', 8.3700, 13.6000, '中国人民银行'),
('2023-12-01', 'SF_LOAN_WRITEOFF_STOCK', 8.6100, 14.6000, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `metric_value2`, `source`) VALUES
('2024-01-01', 'SF_LOAN_WRITEOFF_STOCK', 8.6600, 14.8000, '中国人民银行'),
('2024-02-01', 'SF_LOAN_WRITEOFF_STOCK', 8.7000, 14.8000, '中国人民银行'),
('2024-03-01', 'SF_LOAN_WRITEOFF_STOCK', 8.8600, 15.1000, '中国人民银行'),
('2024-04-01', 'SF_LOAN_WRITEOFF_STOCK', 8.9200, 15.1000, '中国人民银行'),
('2024-05-01', 'SF_LOAN_WRITEOFF_STOCK', 8.9900, 15.2000, '中国人民银行'),
('2024-06-01', 'SF_LOAN_WRITEOFF_STOCK', 9.2000, 15.3000, '中国人民银行'),
('2024-07-01', 'SF_LOAN_WRITEOFF_STOCK', 9.2400, 15.3000, '中国人民银行'),
('2024-08-01', 'SF_LOAN_WRITEOFF_STOCK', 9.3200, 15.4000, '中国人民银行'),
('2024-09-01', 'SF_LOAN_WRITEOFF_STOCK', 9.5300, 15.5000, '中国人民银行'),
('2024-10-01', 'SF_LOAN_WRITEOFF_STOCK', 9.5900, 15.6000, '中国人民银行'),
('2024-11-01', 'SF_LOAN_WRITEOFF_STOCK', 9.6700, 15.5000, '中国人民银行'),
('2024-12-01', 'SF_LOAN_WRITEOFF_STOCK', 9.9400, 15.4000, '中国人民银行');

-- -----------------------------------------------------------
-- 4. 插入社融增量数据 (2021-2024)
-- 单位: 万亿元 (原始数据为亿元人民币, 已转换)
-- -----------------------------------------------------------
INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-01-01', 'SF_NEW', 5.1884, '中国人民银行'),
('2021-01-01', 'SF_RMB_LOAN', 3.8182, '中国人民银行'),
('2021-01-01', 'SF_FOREIGN_LOAN', 0.1098, '中国人民银行'),
('2021-01-01', 'SF_ENTRUDED_LOAN', 0.0091, '中国人民银行'),
('2021-01-01', 'SF_TRUST_LOAN', -0.0842, '中国人民银行'),
('2021-01-01', 'SF_ACCEPTANCE', 0.4902, '中国人民银行'),
('2021-01-01', 'SF_CORP_BOND', 0.3917, '中国人民银行'),
('2021-01-01', 'SF_GOVT_BOND', 0.2437, '中国人民银行'),
('2021-01-01', 'SF_EQUITY', 0.0991, '中国人民银行'),
('2021-01-01', 'SF_ABS', -0.0028, '中国人民银行'),
('2021-01-01', 'SF_LOAN_WRITEOFF', 0.0280, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-02-01', 'SF_NEW', 1.7243, '中国人民银行'),
('2021-02-01', 'SF_RMB_LOAN', 1.3413, '中国人民银行'),
('2021-02-01', 'SF_FOREIGN_LOAN', 0.0464, '中国人民银行'),
('2021-02-01', 'SF_ENTRUDED_LOAN', -0.0100, '中国人民银行'),
('2021-02-01', 'SF_TRUST_LOAN', -0.0936, '中国人民银行'),
('2021-02-01', 'SF_ACCEPTANCE', 0.0639, '中国人民银行'),
('2021-02-01', 'SF_CORP_BOND', 0.1356, '中国人民银行'),
('2021-02-01', 'SF_GOVT_BOND', 0.1017, '中国人民银行'),
('2021-02-01', 'SF_EQUITY', 0.0693, '中国人民银行'),
('2021-02-01', 'SF_ABS', -0.0177, '中国人民银行'),
('2021-02-01', 'SF_LOAN_WRITEOFF', 0.0370, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-03-01', 'SF_NEW', 3.3762, '中国人民银行'),
('2021-03-01', 'SF_RMB_LOAN', 2.7511, '中国人民银行'),
('2021-03-01', 'SF_FOREIGN_LOAN', 0.0282, '中国人民银行'),
('2021-03-01', 'SF_ENTRUDED_LOAN', -0.0042, '中国人民银行'),
('2021-03-01', 'SF_TRUST_LOAN', -0.1791, '中国人民银行'),
('2021-03-01', 'SF_ACCEPTANCE', -0.2296, '中国人民银行'),
('2021-03-01', 'SF_CORP_BOND', 0.3807, '中国人民银行'),
('2021-03-01', 'SF_GOVT_BOND', 0.3131, '中国人民银行'),
('2021-03-01', 'SF_EQUITY', 0.0783, '中国人民银行'),
('2021-03-01', 'SF_ABS', 0.0477, '中国人民银行'),
('2021-03-01', 'SF_LOAN_WRITEOFF', 0.1080, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-04-01', 'SF_NEW', 1.8570, '中国人民银行'),
('2021-04-01', 'SF_RMB_LOAN', 1.2840, '中国人民银行'),
('2021-04-01', 'SF_FOREIGN_LOAN', -0.0272, '中国人民银行'),
('2021-04-01', 'SF_ENTRUDED_LOAN', -0.0213, '中国人民银行'),
('2021-04-01', 'SF_TRUST_LOAN', -0.1328, '中国人民银行'),
('2021-04-01', 'SF_ACCEPTANCE', -0.2152, '中国人民银行'),
('2021-04-01', 'SF_CORP_BOND', 0.3624, '中国人民银行'),
('2021-04-01', 'SF_GOVT_BOND', 0.3739, '中国人民银行'),
('2021-04-01', 'SF_EQUITY', 0.0814, '中国人民银行'),
('2021-04-01', 'SF_ABS', 0.0174, '中国人民银行'),
('2021-04-01', 'SF_LOAN_WRITEOFF', 0.0456, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-05-01', 'SF_NEW', 1.9522, '中国人民银行'),
('2021-05-01', 'SF_RMB_LOAN', 1.4294, '中国人民银行'),
('2021-05-01', 'SF_FOREIGN_LOAN', 0.0007, '中国人民银行'),
('2021-05-01', 'SF_ENTRUDED_LOAN', -0.0408, '中国人民银行'),
('2021-05-01', 'SF_TRUST_LOAN', -0.1295, '中国人民银行'),
('2021-05-01', 'SF_ACCEPTANCE', -0.0926, '中国人民银行'),
('2021-05-01', 'SF_CORP_BOND', -0.1077, '中国人民银行'),
('2021-05-01', 'SF_GOVT_BOND', 0.6701, '中国人民银行'),
('2021-05-01', 'SF_EQUITY', 0.0717, '中国人民银行'),
('2021-05-01', 'SF_ABS', 0.0247, '中国人民银行'),
('2021-05-01', 'SF_LOAN_WRITEOFF', 0.0378, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-06-01', 'SF_NEW', 3.7017, '中国人民银行'),
('2021-06-01', 'SF_RMB_LOAN', 2.3182, '中国人民银行'),
('2021-06-01', 'SF_FOREIGN_LOAN', 0.0701, '中国人民银行'),
('2021-06-01', 'SF_ENTRUDED_LOAN', -0.0474, '中国人民银行'),
('2021-06-01', 'SF_TRUST_LOAN', -0.1046, '中国人民银行'),
('2021-06-01', 'SF_ACCEPTANCE', -0.0221, '中国人民银行'),
('2021-06-01', 'SF_CORP_BOND', 0.3927, '中国人民银行'),
('2021-06-01', 'SF_GOVT_BOND', 0.7508, '中国人民银行'),
('2021-06-01', 'SF_EQUITY', 0.0956, '中国人民银行'),
('2021-06-01', 'SF_ABS', -0.0023, '中国人民银行'),
('2021-06-01', 'SF_LOAN_WRITEOFF', 0.1779, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-07-01', 'SF_NEW', 1.0752, '中国人民银行'),
('2021-07-01', 'SF_RMB_LOAN', 0.8391, '中国人民银行'),
('2021-07-01', 'SF_FOREIGN_LOAN', -0.0078, '中国人民银行'),
('2021-07-01', 'SF_ENTRUDED_LOAN', -0.0151, '中国人民银行'),
('2021-07-01', 'SF_TRUST_LOAN', -0.1571, '中国人民银行'),
('2021-07-01', 'SF_ACCEPTANCE', -0.2316, '中国人民银行'),
('2021-07-01', 'SF_CORP_BOND', 0.3091, '中国人民银行'),
('2021-07-01', 'SF_GOVT_BOND', 0.1820, '中国人民银行'),
('2021-07-01', 'SF_EQUITY', 0.0938, '中国人民银行'),
('2021-07-01', 'SF_ABS', -0.0019, '中国人民银行'),
('2021-07-01', 'SF_LOAN_WRITEOFF', 0.0269, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-08-01', 'SF_NEW', 2.9893, '中国人民银行'),
('2021-08-01', 'SF_RMB_LOAN', 1.2713, '中国人民银行'),
('2021-08-01', 'SF_FOREIGN_LOAN', 0.0347, '中国人民银行'),
('2021-08-01', 'SF_ENTRUDED_LOAN', 0.0177, '中国人民银行'),
('2021-08-01', 'SF_TRUST_LOAN', -0.1362, '中国人民银行'),
('2021-08-01', 'SF_ACCEPTANCE', 0.0127, '中国人民银行'),
('2021-08-01', 'SF_CORP_BOND', 0.4649, '中国人民银行'),
('2021-08-01', 'SF_GOVT_BOND', 0.9738, '中国人民银行'),
('2021-08-01', 'SF_EQUITY', 0.1478, '中国人民银行'),
('2021-08-01', 'SF_ABS', 0.0268, '中国人民银行'),
('2021-08-01', 'SF_LOAN_WRITEOFF', 0.0526, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-09-01', 'SF_NEW', 2.9026, '中国人民银行'),
('2021-09-01', 'SF_RMB_LOAN', 1.7755, '中国人民银行'),
('2021-09-01', 'SF_FOREIGN_LOAN', -0.0019, '中国人民银行'),
('2021-09-01', 'SF_ENTRUDED_LOAN', -0.0022, '中国人民银行'),
('2021-09-01', 'SF_TRUST_LOAN', -0.2098, '中国人民银行'),
('2021-09-01', 'SF_ACCEPTANCE', 0.0014, '中国人民银行'),
('2021-09-01', 'SF_CORP_BOND', 0.1137, '中国人民银行'),
('2021-09-01', 'SF_GOVT_BOND', 0.8066, '中国人民银行'),
('2021-09-01', 'SF_EQUITY', 0.0772, '中国人民银行'),
('2021-09-01', 'SF_ABS', 0.0642, '中国人民银行'),
('2021-09-01', 'SF_LOAN_WRITEOFF', 0.1505, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-10-01', 'SF_NEW', 1.6176, '中国人民银行'),
('2021-10-01', 'SF_RMB_LOAN', 0.7752, '中国人民银行'),
('2021-10-01', 'SF_FOREIGN_LOAN', -0.0033, '中国人民银行'),
('2021-10-01', 'SF_ENTRUDED_LOAN', -0.0173, '中国人民银行'),
('2021-10-01', 'SF_TRUST_LOAN', -0.1061, '中国人民银行'),
('2021-10-01', 'SF_ACCEPTANCE', -0.0886, '中国人民银行'),
('2021-10-01', 'SF_CORP_BOND', 0.2261, '中国人民银行'),
('2021-10-01', 'SF_GOVT_BOND', 0.6167, '中国人民银行'),
('2021-10-01', 'SF_EQUITY', 0.0846, '中国人民银行'),
('2021-10-01', 'SF_ABS', -0.0139, '中国人民银行'),
('2021-10-01', 'SF_LOAN_WRITEOFF', 0.0348, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-11-01', 'SF_NEW', 2.5983, '中国人民银行'),
('2021-11-01', 'SF_RMB_LOAN', 1.3021, '中国人民银行'),
('2021-11-01', 'SF_FOREIGN_LOAN', -0.0134, '中国人民银行'),
('2021-11-01', 'SF_ENTRUDED_LOAN', 0.0035, '中国人民银行'),
('2021-11-01', 'SF_TRUST_LOAN', -0.2190, '中国人民银行'),
('2021-11-01', 'SF_ACCEPTANCE', -0.0383, '中国人民银行'),
('2021-11-01', 'SF_CORP_BOND', 0.4006, '中国人民银行'),
('2021-11-01', 'SF_GOVT_BOND', 0.8158, '中国人民银行'),
('2021-11-01', 'SF_EQUITY', 0.1294, '中国人民银行'),
('2021-11-01', 'SF_ABS', 0.0742, '中国人民银行'),
('2021-11-01', 'SF_LOAN_WRITEOFF', 0.0582, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2021-12-01', 'SF_NEW', 2.3580, '中国人民银行'),
('2021-12-01', 'SF_RMB_LOAN', 1.0350, '中国人民银行'),
('2021-12-01', 'SF_FOREIGN_LOAN', -0.0649, '中国人民银行'),
('2021-12-01', 'SF_ENTRUDED_LOAN', -0.0416, '中国人民银行'),
('2021-12-01', 'SF_TRUST_LOAN', -0.4553, '中国人民银行'),
('2021-12-01', 'SF_ACCEPTANCE', -0.1419, '中国人民银行'),
('2021-12-01', 'SF_CORP_BOND', 0.2167, '中国人民银行'),
('2021-12-01', 'SF_GOVT_BOND', 1.1674, '中国人民银行'),
('2021-12-01', 'SF_EQUITY', 0.1851, '中国人民银行'),
('2021-12-01', 'SF_ABS', 0.0618, '中国人民银行'),
('2021-12-01', 'SF_LOAN_WRITEOFF', 0.2726, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-01-01', 'SF_NEW', 6.1759, '中国人民银行'),
('2022-01-01', 'SF_RMB_LOAN', 4.1988, '中国人民银行'),
('2022-01-01', 'SF_FOREIGN_LOAN', 0.1031, '中国人民银行'),
('2022-01-01', 'SF_ENTRUDED_LOAN', 0.0428, '中国人民银行'),
('2022-01-01', 'SF_TRUST_LOAN', -0.0680, '中国人民银行'),
('2022-01-01', 'SF_ACCEPTANCE', 0.4733, '中国人民银行'),
('2022-01-01', 'SF_CORP_BOND', 0.5838, '中国人民银行'),
('2022-01-01', 'SF_GOVT_BOND', 0.6026, '中国人民银行'),
('2022-01-01', 'SF_EQUITY', 0.1439, '中国人民银行'),
('2022-01-01', 'SF_ABS', -0.0214, '中国人民银行'),
('2022-01-01', 'SF_LOAN_WRITEOFF', 0.0204, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-02-01', 'SF_NEW', 1.2170, '中国人民银行'),
('2022-02-01', 'SF_RMB_LOAN', 0.9084, '中国人民银行'),
('2022-02-01', 'SF_FOREIGN_LOAN', 0.0480, '中国人民银行'),
('2022-02-01', 'SF_ENTRUDED_LOAN', -0.0074, '中国人民银行'),
('2022-02-01', 'SF_TRUST_LOAN', -0.0751, '中国人民银行'),
('2022-02-01', 'SF_ACCEPTANCE', -0.4228, '中国人民银行'),
('2022-02-01', 'SF_CORP_BOND', 0.3610, '中国人民银行'),
('2022-02-01', 'SF_GOVT_BOND', 0.2722, '中国人民银行'),
('2022-02-01', 'SF_EQUITY', 0.0585, '中国人民银行'),
('2022-02-01', 'SF_ABS', -0.0053, '中国人民银行'),
('2022-02-01', 'SF_LOAN_WRITEOFF', 0.0257, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-03-01', 'SF_NEW', 4.6565, '中国人民银行'),
('2022-03-01', 'SF_RMB_LOAN', 3.2291, '中国人民银行'),
('2022-03-01', 'SF_FOREIGN_LOAN', 0.0239, '中国人民银行'),
('2022-03-01', 'SF_ENTRUDED_LOAN', 0.0107, '中国人民银行'),
('2022-03-01', 'SF_TRUST_LOAN', -0.0259, '中国人民银行'),
('2022-03-01', 'SF_ACCEPTANCE', 0.0287, '中国人民银行'),
('2022-03-01', 'SF_CORP_BOND', 0.3750, '中国人民银行'),
('2022-03-01', 'SF_GOVT_BOND', 0.7074, '中国人民银行'),
('2022-03-01', 'SF_EQUITY', 0.0958, '中国人民银行'),
('2022-03-01', 'SF_ABS', -0.0102, '中国人民银行'),
('2022-03-01', 'SF_LOAN_WRITEOFF', 0.1584, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-04-01', 'SF_NEW', 0.9327, '中国人民银行'),
('2022-04-01', 'SF_RMB_LOAN', 0.3616, '中国人民银行'),
('2022-04-01', 'SF_FOREIGN_LOAN', -0.0760, '中国人民银行'),
('2022-04-01', 'SF_ENTRUDED_LOAN', -0.0002, '中国人民银行'),
('2022-04-01', 'SF_TRUST_LOAN', -0.0615, '中国人民银行'),
('2022-04-01', 'SF_ACCEPTANCE', -0.2557, '中国人民银行'),
('2022-04-01', 'SF_CORP_BOND', 0.3652, '中国人民银行'),
('2022-04-01', 'SF_GOVT_BOND', 0.3912, '中国人民银行'),
('2022-04-01', 'SF_EQUITY', 0.1166, '中国人民银行'),
('2022-04-01', 'SF_ABS', -0.0216, '中国人民银行'),
('2022-04-01', 'SF_LOAN_WRITEOFF', 0.0484, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-05-01', 'SF_NEW', 2.8415, '中国人民银行'),
('2022-05-01', 'SF_RMB_LOAN', 1.8230, '中国人民银行'),
('2022-05-01', 'SF_FOREIGN_LOAN', -0.0240, '中国人民银行'),
('2022-05-01', 'SF_ENTRUDED_LOAN', -0.0132, '中国人民银行'),
('2022-05-01', 'SF_TRUST_LOAN', -0.0619, '中国人民银行'),
('2022-05-01', 'SF_ACCEPTANCE', -0.1068, '中国人民银行'),
('2022-05-01', 'SF_CORP_BOND', 0.0366, '中国人民银行'),
('2022-05-01', 'SF_GOVT_BOND', 1.0582, '中国人民银行'),
('2022-05-01', 'SF_EQUITY', 0.0292, '中国人民银行'),
('2022-05-01', 'SF_ABS', -0.0191, '中国人民银行'),
('2022-05-01', 'SF_LOAN_WRITEOFF', 0.0487, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-06-01', 'SF_NEW', 5.1926, '中国人民银行'),
('2022-06-01', 'SF_RMB_LOAN', 3.0540, '中国人民银行'),
('2022-06-01', 'SF_FOREIGN_LOAN', -0.0291, '中国人民银行'),
('2022-06-01', 'SF_ENTRUDED_LOAN', -0.0380, '中国人民银行'),
('2022-06-01', 'SF_TRUST_LOAN', -0.0828, '中国人民银行'),
('2022-06-01', 'SF_ACCEPTANCE', 0.1066, '中国人民银行'),
('2022-06-01', 'SF_CORP_BOND', 0.2346, '中国人民银行'),
('2022-06-01', 'SF_GOVT_BOND', 1.6216, '中国人民银行'),
('2022-06-01', 'SF_EQUITY', 0.0589, '中国人民银行'),
('2022-06-01', 'SF_ABS', -0.0023, '中国人民银行'),
('2022-06-01', 'SF_LOAN_WRITEOFF', 0.1760, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-07-01', 'SF_NEW', 0.7785, '中国人民银行'),
('2022-07-01', 'SF_RMB_LOAN', 0.4088, '中国人民银行'),
('2022-07-01', 'SF_FOREIGN_LOAN', -0.1137, '中国人民银行'),
('2022-07-01', 'SF_ENTRUDED_LOAN', 0.0089, '中国人民银行'),
('2022-07-01', 'SF_TRUST_LOAN', -0.0398, '中国人民银行'),
('2022-07-01', 'SF_ACCEPTANCE', -0.2744, '中国人民银行'),
('2022-07-01', 'SF_CORP_BOND', 0.0960, '中国人民银行'),
('2022-07-01', 'SF_GOVT_BOND', 0.3998, '中国人民银行'),
('2022-07-01', 'SF_EQUITY', 0.1437, '中国人民银行'),
('2022-07-01', 'SF_ABS', -0.0357, '中国人民银行'),
('2022-07-01', 'SF_LOAN_WRITEOFF', 0.1021, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-08-01', 'SF_NEW', 2.4712, '中国人民银行'),
('2022-08-01', 'SF_RMB_LOAN', 1.3344, '中国人民银行'),
('2022-08-01', 'SF_FOREIGN_LOAN', -0.0826, '中国人民银行'),
('2022-08-01', 'SF_ENTRUDED_LOAN', 0.1755, '中国人民银行'),
('2022-08-01', 'SF_TRUST_LOAN', -0.0472, '中国人民银行'),
('2022-08-01', 'SF_ACCEPTANCE', 0.3486, '中国人民银行'),
('2022-08-01', 'SF_CORP_BOND', 0.1512, '中国人民银行'),
('2022-08-01', 'SF_GOVT_BOND', 0.3045, '中国人民银行'),
('2022-08-01', 'SF_EQUITY', 0.1251, '中国人民银行'),
('2022-08-01', 'SF_ABS', 0.0004, '中国人民银行'),
('2022-08-01', 'SF_LOAN_WRITEOFF', 0.0625, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-09-01', 'SF_NEW', 3.5411, '中国人民银行'),
('2022-09-01', 'SF_RMB_LOAN', 2.5686, '中国人民银行'),
('2022-09-01', 'SF_FOREIGN_LOAN', -0.0713, '中国人民银行'),
('2022-09-01', 'SF_ENTRUDED_LOAN', 0.1508, '中国人民银行'),
('2022-09-01', 'SF_TRUST_LOAN', -0.0191, '中国人民银行'),
('2022-09-01', 'SF_ACCEPTANCE', 0.0132, '中国人民银行'),
('2022-09-01', 'SF_CORP_BOND', 0.0345, '中国人民银行'),
('2022-09-01', 'SF_GOVT_BOND', 0.5533, '中国人民银行'),
('2022-09-01', 'SF_EQUITY', 0.1022, '中国人民银行'),
('2022-09-01', 'SF_ABS', -0.0192, '中国人民银行'),
('2022-09-01', 'SF_LOAN_WRITEOFF', 0.1512, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-10-01', 'SF_NEW', 0.9134, '中国人民银行'),
('2022-10-01', 'SF_RMB_LOAN', 0.4431, '中国人民银行'),
('2022-10-01', 'SF_FOREIGN_LOAN', -0.0724, '中国人民银行'),
('2022-10-01', 'SF_ENTRUDED_LOAN', 0.0470, '中国人民银行'),
('2022-10-01', 'SF_TRUST_LOAN', -0.0061, '中国人民银行'),
('2022-10-01', 'SF_ACCEPTANCE', -0.2156, '中国人民银行'),
('2022-10-01', 'SF_CORP_BOND', 0.2413, '中国人民银行'),
('2022-10-01', 'SF_GOVT_BOND', 0.2791, '中国人民银行'),
('2022-10-01', 'SF_EQUITY', 0.0788, '中国人民银行'),
('2022-10-01', 'SF_ABS', -0.0132, '中国人民银行'),
('2022-10-01', 'SF_LOAN_WRITEOFF', 0.0326, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-11-01', 'SF_NEW', 1.9837, '中国人民银行'),
('2022-11-01', 'SF_RMB_LOAN', 1.1448, '中国人民银行'),
('2022-11-01', 'SF_FOREIGN_LOAN', -0.0648, '中国人民银行'),
('2022-11-01', 'SF_ENTRUDED_LOAN', -0.0088, '中国人民银行'),
('2022-11-01', 'SF_TRUST_LOAN', -0.0365, '中国人民银行'),
('2022-11-01', 'SF_ACCEPTANCE', 0.0191, '中国人民银行'),
('2022-11-01', 'SF_CORP_BOND', 0.0604, '中国人民银行'),
('2022-11-01', 'SF_GOVT_BOND', 0.6520, '中国人民银行'),
('2022-11-01', 'SF_EQUITY', 0.0788, '中国人民银行'),
('2022-11-01', 'SF_ABS', -0.0233, '中国人民银行'),
('2022-11-01', 'SF_LOAN_WRITEOFF', 0.0625, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2022-12-01', 'SF_NEW', 1.3063, '中国人民银行'),
('2022-12-01', 'SF_RMB_LOAN', 1.4401, '中国人民银行'),
('2022-12-01', 'SF_FOREIGN_LOAN', -0.1665, '中国人民银行'),
('2022-12-01', 'SF_ENTRUDED_LOAN', -0.0101, '中国人民银行'),
('2022-12-01', 'SF_TRUST_LOAN', -0.0764, '中国人民银行'),
('2022-12-01', 'SF_ACCEPTANCE', -0.0554, '中国人民银行'),
('2022-12-01', 'SF_CORP_BOND', -0.4887, '中国人民银行'),
('2022-12-01', 'SF_GOVT_BOND', 0.2809, '中国人民银行'),
('2022-12-01', 'SF_EQUITY', 0.1443, '中国人民银行'),
('2022-12-01', 'SF_ABS', -0.0152, '中国人民银行'),
('2022-12-01', 'SF_LOAN_WRITEOFF', 0.1384, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-01-01', 'SF_NEW', 5.9956, '中国人民银行'),
('2023-01-01', 'SF_RMB_LOAN', 4.9314, '中国人民银行'),
('2023-01-01', 'SF_FOREIGN_LOAN', -0.0131, '中国人民银行'),
('2023-01-01', 'SF_ENTRUDED_LOAN', 0.0584, '中国人民银行'),
('2023-01-01', 'SF_TRUST_LOAN', -0.0062, '中国人民银行'),
('2023-01-01', 'SF_ACCEPTANCE', 0.2963, '中国人民银行'),
('2023-01-01', 'SF_CORP_BOND', 0.1638, '中国人民银行'),
('2023-01-01', 'SF_GOVT_BOND', 0.4140, '中国人民银行'),
('2023-01-01', 'SF_EQUITY', 0.0964, '中国人民银行'),
('2023-01-01', 'SF_ABS', -0.0333, '中国人民银行'),
('2023-01-01', 'SF_LOAN_WRITEOFF', 0.0312, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-02-01', 'SF_NEW', 3.1610, '中国人民银行'),
('2023-02-01', 'SF_RMB_LOAN', 1.8184, '中国人民银行'),
('2023-02-01', 'SF_FOREIGN_LOAN', 0.0310, '中国人民银行'),
('2023-02-01', 'SF_ENTRUDED_LOAN', -0.0077, '中国人民银行'),
('2023-02-01', 'SF_TRUST_LOAN', 0.0066, '中国人民银行'),
('2023-02-01', 'SF_ACCEPTANCE', -0.0069, '中国人民银行'),
('2023-02-01', 'SF_CORP_BOND', 0.3662, '中国人民银行'),
('2023-02-01', 'SF_GOVT_BOND', 0.8138, '中国人民银行'),
('2023-02-01', 'SF_EQUITY', 0.0571, '中国人民银行'),
('2023-02-01', 'SF_ABS', -0.0279, '中国人民银行'),
('2023-02-01', 'SF_LOAN_WRITEOFF', 0.0367, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-03-01', 'SF_NEW', 5.3867, '中国人民银行'),
('2023-03-01', 'SF_RMB_LOAN', 3.9487, '中国人民银行'),
('2023-03-01', 'SF_FOREIGN_LOAN', 0.0427, '中国人民银行'),
('2023-03-01', 'SF_ENTRUDED_LOAN', 0.0175, '中国人民银行'),
('2023-03-01', 'SF_TRUST_LOAN', -0.0045, '中国人民银行'),
('2023-03-01', 'SF_ACCEPTANCE', 0.1792, '中国人民银行'),
('2023-03-01', 'SF_CORP_BOND', 0.3357, '中国人民银行'),
('2023-03-01', 'SF_GOVT_BOND', 0.6015, '中国人民银行'),
('2023-03-01', 'SF_EQUITY', 0.0614, '中国人民银行'),
('2023-03-01', 'SF_ABS', -0.0150, '中国人民银行'),
('2023-03-01', 'SF_LOAN_WRITEOFF', 0.1235, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-04-01', 'SF_NEW', 1.2249, '中国人民银行'),
('2023-04-01', 'SF_RMB_LOAN', 0.4431, '中国人民银行'),
('2023-04-01', 'SF_FOREIGN_LOAN', -0.0319, '中国人民银行'),
('2023-04-01', 'SF_ENTRUDED_LOAN', 0.0083, '中国人民银行'),
('2023-04-01', 'SF_TRUST_LOAN', 0.0119, '中国人民银行'),
('2023-04-01', 'SF_ACCEPTANCE', -0.1345, '中国人民银行'),
('2023-04-01', 'SF_CORP_BOND', 0.2940, '中国人民银行'),
('2023-04-01', 'SF_GOVT_BOND', 0.4548, '中国人民银行'),
('2023-04-01', 'SF_EQUITY', 0.0993, '中国人民银行'),
('2023-04-01', 'SF_ABS', -0.0376, '中国人民银行'),
('2023-04-01', 'SF_LOAN_WRITEOFF', 0.0448, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-05-01', 'SF_NEW', 1.5560, '中国人民银行'),
('2023-05-01', 'SF_RMB_LOAN', 1.2219, '中国人民银行'),
('2023-05-01', 'SF_FOREIGN_LOAN', -0.0338, '中国人民银行'),
('2023-05-01', 'SF_ENTRUDED_LOAN', 0.0035, '中国人民银行'),
('2023-05-01', 'SF_TRUST_LOAN', 0.0303, '中国人民银行'),
('2023-05-01', 'SF_ACCEPTANCE', -0.1795, '中国人民银行'),
('2023-05-01', 'SF_CORP_BOND', -0.2144, '中国人民银行'),
('2023-05-01', 'SF_GOVT_BOND', 0.5571, '中国人民银行'),
('2023-05-01', 'SF_EQUITY', 0.0753, '中国人民银行'),
('2023-05-01', 'SF_ABS', -0.0349, '中国人民银行'),
('2023-05-01', 'SF_LOAN_WRITEOFF', 0.0516, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-06-01', 'SF_NEW', 4.2265, '中国人民银行'),
('2023-06-01', 'SF_RMB_LOAN', 3.2413, '中国人民银行'),
('2023-06-01', 'SF_FOREIGN_LOAN', -0.0191, '中国人民银行'),
('2023-06-01', 'SF_ENTRUDED_LOAN', -0.0056, '中国人民银行'),
('2023-06-01', 'SF_TRUST_LOAN', -0.0154, '中国人民银行'),
('2023-06-01', 'SF_ACCEPTANCE', -0.0691, '中国人民银行'),
('2023-06-01', 'SF_CORP_BOND', 0.2249, '中国人民银行'),
('2023-06-01', 'SF_GOVT_BOND', 0.5371, '中国人民银行'),
('2023-06-01', 'SF_EQUITY', 0.0700, '中国人民银行'),
('2023-06-01', 'SF_ABS', -0.0026, '中国人民银行'),
('2023-06-01', 'SF_LOAN_WRITEOFF', 0.1776, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-07-01', 'SF_NEW', 0.5366, '中国人民银行'),
('2023-07-01', 'SF_RMB_LOAN', 0.0364, '中国人民银行'),
('2023-07-01', 'SF_FOREIGN_LOAN', -0.0339, '中国人民银行'),
('2023-07-01', 'SF_ENTRUDED_LOAN', 0.0008, '中国人民银行'),
('2023-07-01', 'SF_TRUST_LOAN', 0.0230, '中国人民银行'),
('2023-07-01', 'SF_ACCEPTANCE', -0.1963, '中国人民银行'),
('2023-07-01', 'SF_CORP_BOND', 0.1290, '中国人民银行'),
('2023-07-01', 'SF_GOVT_BOND', 0.4109, '中国人民银行'),
('2023-07-01', 'SF_EQUITY', 0.0786, '中国人民银行'),
('2023-07-01', 'SF_ABS', -0.0296, '中国人民银行'),
('2023-07-01', 'SF_LOAN_WRITEOFF', 0.0402, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-08-01', 'SF_NEW', 3.1279, '中国人民银行'),
('2023-08-01', 'SF_RMB_LOAN', 1.3412, '中国人民银行'),
('2023-08-01', 'SF_FOREIGN_LOAN', -0.0201, '中国人民银行'),
('2023-08-01', 'SF_ENTRUDED_LOAN', 0.0097, '中国人民银行'),
('2023-08-01', 'SF_TRUST_LOAN', -0.0221, '中国人民银行'),
('2023-08-01', 'SF_ACCEPTANCE', 0.1129, '中国人民银行'),
('2023-08-01', 'SF_CORP_BOND', 0.2788, '中国人民银行'),
('2023-08-01', 'SF_GOVT_BOND', 1.1759, '中国人民银行'),
('2023-08-01', 'SF_EQUITY', 0.1036, '中国人民银行'),
('2023-08-01', 'SF_ABS', -0.0135, '中国人民银行'),
('2023-08-01', 'SF_LOAN_WRITEOFF', 0.0596, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-09-01', 'SF_NEW', 4.1326, '中国人民银行'),
('2023-09-01', 'SF_RMB_LOAN', 2.5369, '中国人民银行'),
('2023-09-01', 'SF_FOREIGN_LOAN', -0.0583, '中国人民银行'),
('2023-09-01', 'SF_ENTRUDED_LOAN', 0.0208, '中国人民银行'),
('2023-09-01', 'SF_TRUST_LOAN', 0.0402, '中国人民银行'),
('2023-09-01', 'SF_ACCEPTANCE', 0.2397, '中国人民银行'),
('2023-09-01', 'SF_CORP_BOND', 0.0650, '中国人民银行'),
('2023-09-01', 'SF_GOVT_BOND', 0.9920, '中国人民银行'),
('2023-09-01', 'SF_EQUITY', 0.0326, '中国人民银行'),
('2023-09-01', 'SF_ABS', -0.0172, '中国人民银行'),
('2023-09-01', 'SF_LOAN_WRITEOFF', 0.1799, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-10-01', 'SF_NEW', 1.8441, '中国人民银行'),
('2023-10-01', 'SF_RMB_LOAN', 0.4837, '中国人民银行'),
('2023-10-01', 'SF_FOREIGN_LOAN', 0.0152, '中国人民银行'),
('2023-10-01', 'SF_ENTRUDED_LOAN', -0.0429, '中国人民银行'),
('2023-10-01', 'SF_TRUST_LOAN', 0.0393, '中国人民银行'),
('2023-10-01', 'SF_ACCEPTANCE', -0.2536, '中国人民银行'),
('2023-10-01', 'SF_CORP_BOND', 0.1178, '中国人民银行'),
('2023-10-01', 'SF_GOVT_BOND', 1.5638, '中国人民银行'),
('2023-10-01', 'SF_EQUITY', 0.0321, '中国人民银行'),
('2023-10-01', 'SF_ABS', -0.2530, '中国人民银行'),
('2023-10-01', 'SF_LOAN_WRITEOFF', 0.0427, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-11-01', 'SF_NEW', 2.4554, '中国人民银行'),
('2023-11-01', 'SF_RMB_LOAN', 1.1120, '中国人民银行'),
('2023-11-01', 'SF_FOREIGN_LOAN', -0.0357, '中国人民银行'),
('2023-11-01', 'SF_ENTRUDED_LOAN', -0.0386, '中国人民银行'),
('2023-11-01', 'SF_TRUST_LOAN', 0.0197, '中国人民银行'),
('2023-11-01', 'SF_ACCEPTANCE', 0.0202, '中国人民银行'),
('2023-11-01', 'SF_CORP_BOND', 0.1388, '中国人民银行'),
('2023-11-01', 'SF_GOVT_BOND', 1.1512, '中国人民银行'),
('2023-11-01', 'SF_EQUITY', 0.0359, '中国人民银行'),
('2023-11-01', 'SF_ABS', -0.1355, '中国人民银行'),
('2023-11-01', 'SF_LOAN_WRITEOFF', 0.0742, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2023-12-01', 'SF_NEW', 1.9326, '中国人民银行'),
('2023-12-01', 'SF_RMB_LOAN', 1.1092, '中国人民银行'),
('2023-12-01', 'SF_FOREIGN_LOAN', -0.0635, '中国人民银行'),
('2023-12-01', 'SF_ENTRUDED_LOAN', -0.0043, '中国人民银行'),
('2023-12-01', 'SF_TRUST_LOAN', 0.0347, '中国人民银行'),
('2023-12-01', 'SF_ACCEPTANCE', -0.1865, '中国人民银行'),
('2023-12-01', 'SF_CORP_BOND', -0.2741, '中国人民银行'),
('2023-12-01', 'SF_GOVT_BOND', 0.9324, '中国人民银行'),
('2023-12-01', 'SF_EQUITY', 0.0508, '中国人民银行'),
('2023-12-01', 'SF_ABS', -0.0278, '中国人民银行'),
('2023-12-01', 'SF_LOAN_WRITEOFF', 0.2347, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-01-01', 'SF_NEW', 6.4734, '中国人民银行'),
('2024-01-01', 'SF_RMB_LOAN', 4.8401, '中国人民银行'),
('2024-01-01', 'SF_FOREIGN_LOAN', 0.0989, '中国人民银行'),
('2024-01-01', 'SF_ENTRUDED_LOAN', -0.0359, '中国人民银行'),
('2024-01-01', 'SF_TRUST_LOAN', 0.0732, '中国人民银行'),
('2024-01-01', 'SF_ACCEPTANCE', 0.5636, '中国人民银行'),
('2024-01-01', 'SF_CORP_BOND', 0.4320, '中国人民银行'),
('2024-01-01', 'SF_GOVT_BOND', 0.2947, '中国人民银行'),
('2024-01-01', 'SF_EQUITY', 0.0422, '中国人民银行'),
('2024-01-01', 'SF_ABS', -0.0203, '中国人民银行'),
('2024-01-01', 'SF_LOAN_WRITEOFF', 0.0474, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-02-01', 'SF_NEW', 1.4959, '中国人民银行'),
('2024-02-01', 'SF_RMB_LOAN', 0.9773, '中国人民银行'),
('2024-02-01', 'SF_FOREIGN_LOAN', -0.0009, '中国人民银行'),
('2024-02-01', 'SF_ENTRUDED_LOAN', -0.0172, '中国人民银行'),
('2024-02-01', 'SF_TRUST_LOAN', 0.0571, '中国人民银行'),
('2024-02-01', 'SF_ACCEPTANCE', -0.3686, '中国人民银行'),
('2024-02-01', 'SF_CORP_BOND', 0.1423, '中国人民银行'),
('2024-02-01', 'SF_GOVT_BOND', 0.6011, '中国人民银行'),
('2024-02-01', 'SF_EQUITY', 0.0114, '中国人民银行'),
('2024-02-01', 'SF_ABS', -0.0210, '中国人民银行'),
('2024-02-01', 'SF_LOAN_WRITEOFF', 0.0488, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-03-01', 'SF_NEW', 4.8335, '中国人民银行'),
('2024-03-01', 'SF_RMB_LOAN', 3.2920, '中国人民银行'),
('2024-03-01', 'SF_FOREIGN_LOAN', 0.0543, '中国人民银行'),
('2024-03-01', 'SF_ENTRUDED_LOAN', -0.0465, '中国人民银行'),
('2024-03-01', 'SF_TRUST_LOAN', 0.0681, '中国人民银行'),
('2024-03-01', 'SF_ACCEPTANCE', 0.3552, '中国人民银行'),
('2024-03-01', 'SF_CORP_BOND', 0.4237, '中国人民银行'),
('2024-03-01', 'SF_GOVT_BOND', 0.4626, '中国人民银行'),
('2024-03-01', 'SF_EQUITY', 0.0227, '中国人民银行'),
('2024-03-01', 'SF_ABS', -0.0588, '中国人民银行'),
('2024-03-01', 'SF_LOAN_WRITEOFF', 0.1587, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-04-01', 'SF_NEW', -0.0658, '中国人民银行'),
('2024-04-01', 'SF_RMB_LOAN', 0.3349, '中国人民银行'),
('2024-04-01', 'SF_FOREIGN_LOAN', -0.0310, '中国人民银行'),
('2024-04-01', 'SF_ENTRUDED_LOAN', 0.0089, '中国人民银行'),
('2024-04-01', 'SF_TRUST_LOAN', 0.0142, '中国人民银行'),
('2024-04-01', 'SF_ACCEPTANCE', -0.4490, '中国人民银行'),
('2024-04-01', 'SF_CORP_BOND', 0.1707, '中国人民银行'),
('2024-04-01', 'SF_GOVT_BOND', -0.0937, '中国人民银行'),
('2024-04-01', 'SF_EQUITY', 0.0186, '中国人民银行'),
('2024-04-01', 'SF_ABS', -0.1967, '中国人民银行'),
('2024-04-01', 'SF_LOAN_WRITEOFF', 0.0520, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-05-01', 'SF_NEW', 2.0623, '中国人民银行'),
('2024-05-01', 'SF_RMB_LOAN', 0.8197, '中国人民银行'),
('2024-05-01', 'SF_FOREIGN_LOAN', -0.0487, '中国人民银行'),
('2024-05-01', 'SF_ENTRUDED_LOAN', -0.0009, '中国人民银行'),
('2024-05-01', 'SF_TRUST_LOAN', 0.0224, '中国人民银行'),
('2024-05-01', 'SF_ACCEPTANCE', -0.1331, '中国人民银行'),
('2024-05-01', 'SF_CORP_BOND', 0.0285, '中国人民银行'),
('2024-05-01', 'SF_GOVT_BOND', 1.2266, '中国人民银行'),
('2024-05-01', 'SF_EQUITY', 0.0111, '中国人民银行'),
('2024-05-01', 'SF_ABS', -0.0426, '中国人民银行'),
('2024-05-01', 'SF_LOAN_WRITEOFF', 0.0734, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-06-01', 'SF_NEW', 3.2985, '中国人民银行'),
('2024-06-01', 'SF_RMB_LOAN', 2.1927, '中国人民银行'),
('2024-06-01', 'SF_FOREIGN_LOAN', -0.0807, '中国人民银行'),
('2024-06-01', 'SF_ENTRUDED_LOAN', -0.0003, '中国人民银行'),
('2024-06-01', 'SF_TRUST_LOAN', 0.0748, '中国人民银行'),
('2024-06-01', 'SF_ACCEPTANCE', -0.2045, '中国人民银行'),
('2024-06-01', 'SF_CORP_BOND', 0.2100, '中国人民银行'),
('2024-06-01', 'SF_GOVT_BOND', 0.8476, '中国人民银行'),
('2024-06-01', 'SF_EQUITY', 0.0154, '中国人民银行'),
('2024-06-01', 'SF_ABS', -0.0695, '中国人民银行'),
('2024-06-01', 'SF_LOAN_WRITEOFF', 0.2092, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-07-01', 'SF_NEW', 0.7707, '中国人民银行'),
('2024-07-01', 'SF_RMB_LOAN', -0.0808, '中国人民银行'),
('2024-07-01', 'SF_FOREIGN_LOAN', -0.0890, '中国人民银行'),
('2024-07-01', 'SF_ENTRUDED_LOAN', 0.0345, '中国人民银行'),
('2024-07-01', 'SF_TRUST_LOAN', -0.0026, '中国人民银行'),
('2024-07-01', 'SF_ACCEPTANCE', -0.1075, '中国人民银行'),
('2024-07-01', 'SF_CORP_BOND', 0.2036, '中国人民银行'),
('2024-07-01', 'SF_GOVT_BOND', 0.6881, '中国人民银行'),
('2024-07-01', 'SF_EQUITY', 0.0231, '中国人民银行'),
('2024-07-01', 'SF_ABS', -0.0490, '中国人民银行'),
('2024-07-01', 'SF_LOAN_WRITEOFF', 0.0448, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-08-01', 'SF_NEW', 3.0323, '中国人民银行'),
('2024-08-01', 'SF_RMB_LOAN', 1.0411, '中国人民银行'),
('2024-08-01', 'SF_FOREIGN_LOAN', -0.0612, '中国人民银行'),
('2024-08-01', 'SF_ENTRUDED_LOAN', 0.0025, '中国人民银行'),
('2024-08-01', 'SF_TRUST_LOAN', 0.0484, '中国人民银行'),
('2024-08-01', 'SF_ACCEPTANCE', 0.0651, '中国人民银行'),
('2024-08-01', 'SF_CORP_BOND', 0.1703, '中国人民银行'),
('2024-08-01', 'SF_GOVT_BOND', 1.6177, '中国人民银行'),
('2024-08-01', 'SF_EQUITY', 0.0132, '中国人民银行'),
('2024-08-01', 'SF_ABS', -0.0466, '中国人民银行'),
('2024-08-01', 'SF_LOAN_WRITEOFF', 0.0758, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-09-01', 'SF_NEW', 3.7635, '中国人民银行'),
('2024-09-01', 'SF_RMB_LOAN', 1.9742, '中国人民银行'),
('2024-09-01', 'SF_FOREIGN_LOAN', -0.0480, '中国人民银行'),
('2024-09-01', 'SF_ENTRUDED_LOAN', 0.0392, '中国人民银行'),
('2024-09-01', 'SF_TRUST_LOAN', 0.0006, '中国人民银行'),
('2024-09-01', 'SF_ACCEPTANCE', 0.1312, '中国人民银行'),
('2024-09-01', 'SF_CORP_BOND', -0.1926, '中国人民银行'),
('2024-09-01', 'SF_GOVT_BOND', 1.5357, '中国人民银行'),
('2024-09-01', 'SF_EQUITY', 0.0128, '中国人民银行'),
('2024-09-01', 'SF_ABS', -0.0163, '中国人民银行'),
('2024-09-01', 'SF_LOAN_WRITEOFF', 0.2154, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-10-01', 'SF_NEW', 1.4120, '中国人民银行'),
('2024-10-01', 'SF_RMB_LOAN', 0.2965, '中国人民银行'),
('2024-10-01', 'SF_FOREIGN_LOAN', -0.0710, '中国人民银行'),
('2024-10-01', 'SF_ENTRUDED_LOAN', -0.0219, '中国人民银行'),
('2024-10-01', 'SF_TRUST_LOAN', 0.0172, '中国人民银行'),
('2024-10-01', 'SF_ACCEPTANCE', -0.1396, '中国人民银行'),
('2024-10-01', 'SF_CORP_BOND', 0.0987, '中国人民银行'),
('2024-10-01', 'SF_GOVT_BOND', 1.0495, '中国人民银行'),
('2024-10-01', 'SF_EQUITY', 0.0284, '中国人民银行'),
('2024-10-01', 'SF_ABS', -0.0200, '中国人民银行'),
('2024-10-01', 'SF_LOAN_WRITEOFF', 0.0577, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-11-01', 'SF_NEW', 2.3288, '中国人民银行'),
('2024-11-01', 'SF_RMB_LOAN', 0.5216, '中国人民银行'),
('2024-11-01', 'SF_FOREIGN_LOAN', -0.0468, '中国人民银行'),
('2024-11-01', 'SF_ENTRUDED_LOAN', -0.0183, '中国人民银行'),
('2024-11-01', 'SF_TRUST_LOAN', 0.0091, '中国人民银行'),
('2024-11-01', 'SF_ACCEPTANCE', 0.0910, '中国人民银行'),
('2024-11-01', 'SF_CORP_BOND', 0.2381, '中国人民银行'),
('2024-11-01', 'SF_GOVT_BOND', 1.3089, '中国人民银行'),
('2024-11-01', 'SF_EQUITY', 0.0428, '中国人民银行'),
('2024-11-01', 'SF_ABS', -0.0112, '中国人民银行'),
('2024-11-01', 'SF_LOAN_WRITEOFF', 0.0824, '中国人民银行');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `source`) VALUES
('2024-12-01', 'SF_NEW', 2.8537, '中国人民银行'),
('2024-12-01', 'SF_RMB_LOAN', 0.8402, '中国人民银行'),
('2024-12-01', 'SF_FOREIGN_LOAN', -0.0675, '中国人民银行'),
('2024-12-01', 'SF_ENTRUDED_LOAN', -0.0020, '中国人民银行'),
('2024-12-01', 'SF_TRUST_LOAN', 0.0151, '中国人民银行'),
('2024-12-01', 'SF_ACCEPTANCE', -0.1330, '中国人民银行'),
('2024-12-01', 'SF_CORP_BOND', -0.0159, '中国人民银行'),
('2024-12-01', 'SF_GOVT_BOND', 1.7566, '中国人民银行'),
('2024-12-01', 'SF_EQUITY', 0.0484, '中国人民银行'),
('2024-12-01', 'SF_ABS', -0.0117, '中国人民银行'),
('2024-12-01', 'SF_LOAN_WRITEOFF', 0.2637, '中国人民银行');

-- -----------------------------------------------------------
-- 5. 验证查询
-- -----------------------------------------------------------
SELECT indicator_code, COUNT(*) as record_count FROM macro_monthly WHERE indicator_code LIKE 'SF_%' AND stat_date BETWEEN '2021-01-01' AND '2024-12-01' GROUP BY indicator_code ORDER BY indicator_code;
-- 预期: 存量528条(11指标x48月) + 增量528条(11指标x48月) = 1056条
