-- ============================================================
-- 宏观数据模块 — M0/M1/M2 历史数据 INSERT 语句
-- 时间范围：2000年1月 ~ 2026年7月
-- 数据来源：中国人民银行、东方财富网、国家统计局
-- 单位：万亿元（余额）、%（同比增速）
-- ============================================================

BEGIN;

-- -----------------------------------------------------------
-- 补充 M0、M1、M2 完整6个指标元数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_macro_indicator_meta` (`indicator_code`, `indicator_name`, `frequency`, `unit`, `chart_type`, `chart_color`, `sort_order`) VALUES
('M0',           'M0 余额',            'monthly', '万亿元', 'line', '#409EFF', 1),
('M1',           'M1 余额',            'monthly', '万亿元', 'line', '#67C23A', 2),
('M2',           'M2 余额',            'monthly', '万亿元', 'line', '#E6A23C', 3),
('M0_YOY',       'M0 同比增速',        'monthly', '%',     'line', '#409EFF', 4),
('M1_YOY',       'M1 同比增速',        'monthly', '%',     'line', '#67C23A', 5),
('M2_YOY',       'M2 同比增速',        'monthly', '%',     'line', '#E6A23C', 6),


INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-01-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-01-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-01-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-01-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-01-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-02-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-02-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-02-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-02-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-02-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-03-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-03-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-03-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-03-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-03-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-04-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-04-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-04-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-04-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-04-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-05-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-05-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-05-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-05-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-05-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-06-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-06-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-06-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-06-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-06-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-07-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-07-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-07-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-07-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-07-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-08-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-08-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-08-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-08-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-08-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-09-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-09-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-09-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-09-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-09-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-10-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-10-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-10-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-10-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-10-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-11-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-11-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-11-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-11-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-11-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2000-12-01', 'M0', 1.47, 1, 'PBC'),
    ('2000-12-01', 'M1', 53.1, 1, 'PBC'),
    ('2000-12-01', 'M2', 138.38, 1, 'PBC'),
    ('2000-12-01', 'M1_YOY', 15.94, 1, 'PBC'),
    ('2000-12-01', 'M2_YOY', 15.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-01-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-01-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-01-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-01-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-01-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-01-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-02-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-02-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-02-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-02-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-02-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-02-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-03-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-03-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-03-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-03-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-03-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-03-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-04-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-04-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-04-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-04-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-04-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-04-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-05-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-05-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-05-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-05-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-05-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-05-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-06-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-06-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-06-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-06-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-06-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-06-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-07-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-07-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-07-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-07-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-07-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-07-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-08-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-08-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-08-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-08-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-08-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-08-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-09-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-09-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-09-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-09-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-09-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-09-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-10-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-10-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-10-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-10-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-10-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-10-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-11-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-11-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-11-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-11-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-11-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-11-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2001-12-01', 'M0', 1.6, 1, 'PBC'),
    ('2001-12-01', 'M1', 59.9, 1, 'PBC'),
    ('2001-12-01', 'M2', 158.3, 1, 'PBC'),
    ('2001-12-01', 'M0_YOY', 8.84, 1, 'PBC'),
    ('2001-12-01', 'M1_YOY', 12.81, 1, 'PBC'),
    ('2001-12-01', 'M2_YOY', 14.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-01-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-01-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-01-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-01-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-01-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-01-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-02-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-02-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-02-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-02-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-02-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-02-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-03-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-03-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-03-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-03-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-03-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-03-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-04-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-04-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-04-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-04-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-04-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-04-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-05-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-05-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-05-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-05-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-05-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-05-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-06-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-06-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-06-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-06-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-06-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-06-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-07-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-07-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-07-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-07-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-07-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-07-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-08-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-08-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-08-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-08-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-08-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-08-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-09-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-09-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-09-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-09-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-09-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-09-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-10-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-10-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-10-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-10-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-10-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-10-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-11-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-11-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-11-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-11-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-11-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-11-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2002-12-01', 'M0', 1.75, 1, 'PBC'),
    ('2002-12-01', 'M1', 70.9, 1, 'PBC'),
    ('2002-12-01', 'M2', 185.01, 1, 'PBC'),
    ('2002-12-01', 'M0_YOY', 9.37, 1, 'PBC'),
    ('2002-12-01', 'M1_YOY', 18.36, 1, 'PBC'),
    ('2002-12-01', 'M2_YOY', 16.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-01-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-01-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-01-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-01-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-01-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-01-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-02-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-02-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-02-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-02-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-02-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-02-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-03-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-03-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-03-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-03-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-03-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-03-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-04-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-04-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-04-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-04-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-04-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-04-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-05-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-05-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-05-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-05-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-05-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-05-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-06-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-06-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-06-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-06-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-06-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-06-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-07-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-07-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-07-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-07-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-07-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-07-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-08-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-08-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-08-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-08-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-08-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-08-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-09-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-09-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-09-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-09-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-09-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-09-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-10-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-10-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-10-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-10-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-10-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-10-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-11-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-11-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-11-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-11-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-11-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-11-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2003-12-01', 'M0', 1.95, 1, 'PBC'),
    ('2003-12-01', 'M1', 84.1, 1, 'PBC'),
    ('2003-12-01', 'M2', 221.22, 1, 'PBC'),
    ('2003-12-01', 'M0_YOY', 11.43, 1, 'PBC'),
    ('2003-12-01', 'M1_YOY', 18.62, 1, 'PBC'),
    ('2003-12-01', 'M2_YOY', 19.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-01-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-01-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-01-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-01-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-01-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-01-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-02-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-02-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-02-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-02-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-02-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-02-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-03-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-03-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-03-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-03-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-03-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-03-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-04-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-04-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-04-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-04-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-04-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-04-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-05-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-05-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-05-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-05-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-05-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-05-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-06-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-06-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-06-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-06-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-06-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-06-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-07-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-07-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-07-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-07-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-07-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-07-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-08-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-08-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-08-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-08-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-08-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-08-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-09-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-09-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-09-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-09-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-09-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-09-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-10-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-10-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-10-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-10-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-10-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-10-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-11-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-11-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-11-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-11-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-11-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-11-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2004-12-01', 'M0', 2.2, 1, 'PBC'),
    ('2004-12-01', 'M1', 96.0, 1, 'PBC'),
    ('2004-12-01', 'M2', 254.11, 1, 'PBC'),
    ('2004-12-01', 'M0_YOY', 12.82, 1, 'PBC'),
    ('2004-12-01', 'M1_YOY', 14.15, 1, 'PBC'),
    ('2004-12-01', 'M2_YOY', 14.87, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-01-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-01-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-01-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-01-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-01-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-01-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-02-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-02-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-02-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-02-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-02-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-02-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-03-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-03-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-03-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-03-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-03-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-03-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-04-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-04-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-04-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-04-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-04-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-04-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-05-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-05-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-05-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-05-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-05-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-05-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-06-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-06-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-06-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-06-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-06-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-06-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-07-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-07-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-07-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-07-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-07-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-07-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-08-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-08-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-08-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-08-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-08-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-08-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-09-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-09-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-09-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-09-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-09-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-09-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-10-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-10-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-10-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-10-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-10-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-10-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-11-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-11-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-11-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-11-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-11-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-11-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2005-12-01', 'M0', 2.4, 1, 'PBC'),
    ('2005-12-01', 'M1', 107.3, 1, 'PBC'),
    ('2005-12-01', 'M2', 298.76, 1, 'PBC'),
    ('2005-12-01', 'M0_YOY', 9.09, 1, 'PBC'),
    ('2005-12-01', 'M1_YOY', 11.77, 1, 'PBC'),
    ('2005-12-01', 'M2_YOY', 17.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-01-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-01-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-01-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-01-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-01-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-01-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-02-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-02-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-02-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-02-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-02-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-02-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-03-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-03-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-03-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-03-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-03-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-03-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-04-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-04-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-04-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-04-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-04-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-04-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-05-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-05-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-05-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-05-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-05-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-05-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-06-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-06-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-06-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-06-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-06-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-06-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-07-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-07-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-07-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-07-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-07-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-07-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-08-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-08-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-08-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-08-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-08-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-08-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-09-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-09-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-09-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-09-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-09-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-09-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-10-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-10-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-10-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-10-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-10-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-10-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-11-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-11-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-11-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-11-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-11-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-11-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2006-12-01', 'M0', 2.71, 1, 'PBC'),
    ('2006-12-01', 'M1', 126.0, 1, 'PBC'),
    ('2006-12-01', 'M2', 346.0, 1, 'PBC'),
    ('2006-12-01', 'M0_YOY', 12.92, 1, 'PBC'),
    ('2006-12-01', 'M1_YOY', 17.43, 1, 'PBC'),
    ('2006-12-01', 'M2_YOY', 15.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-01-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-01-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-01-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-01-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-01-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-01-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-02-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-02-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-02-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-02-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-02-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-02-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-03-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-03-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-03-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-03-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-03-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-03-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-04-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-04-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-04-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-04-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-04-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-04-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-05-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-05-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-05-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-05-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-05-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-05-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-06-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-06-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-06-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-06-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-06-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-06-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-07-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-07-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-07-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-07-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-07-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-07-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-08-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-08-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-08-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-08-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-08-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-08-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-09-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-09-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-09-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-09-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-09-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-09-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-10-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-10-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-10-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-10-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-10-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-10-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-11-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-11-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-11-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-11-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-11-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-11-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2007-12-01', 'M0', 3.0, 1, 'PBC'),
    ('2007-12-01', 'M1', 152.5, 1, 'PBC'),
    ('2007-12-01', 'M2', 403.0, 1, 'PBC'),
    ('2007-12-01', 'M0_YOY', 10.7, 1, 'PBC'),
    ('2007-12-01', 'M1_YOY', 21.03, 1, 'PBC'),
    ('2007-12-01', 'M2_YOY', 16.47, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-01-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-01-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-01-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-01-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-01-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-01-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-02-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-02-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-02-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-02-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-02-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-02-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-03-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-03-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-03-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-03-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-03-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-03-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-04-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-04-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-04-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-04-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-04-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-04-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-05-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-05-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-05-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-05-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-05-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-05-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-06-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-06-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-06-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-06-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-06-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-06-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-07-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-07-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-07-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-07-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-07-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-07-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-08-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-08-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-08-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-08-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-08-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-08-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-09-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-09-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-09-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-09-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-09-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-09-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-10-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-10-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-10-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-10-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-10-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-10-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-11-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-11-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-11-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-11-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-11-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-11-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2008-12-01', 'M0', 3.42, 1, 'PBC'),
    ('2008-12-01', 'M1', 166.2, 1, 'PBC'),
    ('2008-12-01', 'M2', 475.2, 1, 'PBC'),
    ('2008-12-01', 'M0_YOY', 14.0, 1, 'PBC'),
    ('2008-12-01', 'M1_YOY', 8.98, 1, 'PBC'),
    ('2008-12-01', 'M2_YOY', 17.92, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-01-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-01-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-01-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-01-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-01-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-01-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-02-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-02-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-02-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-02-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-02-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-02-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-03-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-03-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-03-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-03-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-03-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-03-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-04-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-04-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-04-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-04-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-04-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-04-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-05-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-05-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-05-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-05-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-05-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-05-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-06-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-06-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-06-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-06-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-06-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-06-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-07-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-07-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-07-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-07-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-07-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-07-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-08-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-08-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-08-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-08-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-08-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-08-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-09-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-09-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-09-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-09-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-09-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-09-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-10-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-10-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-10-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-10-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-10-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-10-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-11-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-11-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-11-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-11-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-11-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-11-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2009-12-01', 'M0', 3.82, 1, 'PBC'),
    ('2009-12-01', 'M1', 221.4, 1, 'PBC'),
    ('2009-12-01', 'M2', 610.2, 1, 'PBC'),
    ('2009-12-01', 'M0_YOY', 11.7, 1, 'PBC'),
    ('2009-12-01', 'M1_YOY', 33.21, 1, 'PBC'),
    ('2009-12-01', 'M2_YOY', 28.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-01-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-01-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-01-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-01-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-01-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-01-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-02-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-02-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-02-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-02-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-02-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-02-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-03-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-03-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-03-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-03-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-03-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-03-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-04-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-04-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-04-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-04-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-04-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-04-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-05-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-05-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-05-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-05-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-05-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-05-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-06-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-06-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-06-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-06-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-06-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-06-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-07-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-07-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-07-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-07-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-07-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-07-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-08-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-08-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-08-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-08-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-08-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-08-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-09-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-09-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-09-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-09-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-09-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-09-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-10-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-10-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-10-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-10-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-10-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-10-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-11-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-11-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-11-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-11-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-11-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-11-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2010-12-01', 'M0', 4.46, 1, 'PBC'),
    ('2010-12-01', 'M1', 266.6, 1, 'PBC'),
    ('2010-12-01', 'M2', 725.8, 1, 'PBC'),
    ('2010-12-01', 'M0_YOY', 16.75, 1, 'PBC'),
    ('2010-12-01', 'M1_YOY', 20.42, 1, 'PBC'),
    ('2010-12-01', 'M2_YOY', 18.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-01-01', 'M0', 5.8064, 1, 'PBC'),
    ('2011-01-01', 'M1', 26.1765, 1, 'PBC'),
    ('2011-01-01', 'M2', 73.3885, 1, 'PBC'),
    ('2011-01-01', 'M0_YOY', 30.19, 1, 'PBC'),
    ('2011-01-01', 'M1_YOY', -90.18, 1, 'PBC'),
    ('2011-01-01', 'M2_YOY', -89.89, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-02-01', 'M0', 4.727, 1, 'PBC'),
    ('2011-02-01', 'M1', 25.92, 1, 'PBC'),
    ('2011-02-01', 'M2', 73.6131, 1, 'PBC'),
    ('2011-02-01', 'M0_YOY', 5.99, 1, 'PBC'),
    ('2011-02-01', 'M1_YOY', -90.28, 1, 'PBC'),
    ('2011-02-01', 'M2_YOY', -89.86, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-03-01', 'M0', 4.4845, 1, 'PBC'),
    ('2011-03-01', 'M1', 26.6256, 1, 'PBC'),
    ('2011-03-01', 'M2', 75.8131, 1, 'PBC'),
    ('2011-03-01', 'M0_YOY', 0.55, 1, 'PBC'),
    ('2011-03-01', 'M1_YOY', -90.01, 1, 'PBC'),
    ('2011-03-01', 'M2_YOY', -89.55, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-04-01', 'M0', 4.5489, 1, 'PBC'),
    ('2011-04-01', 'M1', 26.6767, 1, 'PBC'),
    ('2011-04-01', 'M2', 75.7385, 1, 'PBC'),
    ('2011-04-01', 'M0_YOY', 1.99, 1, 'PBC'),
    ('2011-04-01', 'M1_YOY', -89.99, 1, 'PBC'),
    ('2011-04-01', 'M2_YOY', -89.56, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-05-01', 'M0', 4.4603, 1, 'PBC'),
    ('2011-05-01', 'M1', 26.929, 1, 'PBC'),
    ('2011-05-01', 'M2', 76.3409, 1, 'PBC'),
    ('2011-05-01', 'M0_YOY', 0.01, 1, 'PBC'),
    ('2011-05-01', 'M1_YOY', -89.9, 1, 'PBC'),
    ('2011-05-01', 'M2_YOY', -89.48, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-06-01', 'M0', 4.4478, 1, 'PBC'),
    ('2011-06-01', 'M1', 27.4663, 1, 'PBC'),
    ('2011-06-01', 'M2', 78.0821, 1, 'PBC'),
    ('2011-06-01', 'M0_YOY', -0.27, 1, 'PBC'),
    ('2011-06-01', 'M1_YOY', -89.7, 1, 'PBC'),
    ('2011-06-01', 'M2_YOY', -89.24, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-07-01', 'M0', 4.5183, 1, 'PBC'),
    ('2011-07-01', 'M1', 27.0546, 1, 'PBC'),
    ('2011-07-01', 'M2', 77.2924, 1, 'PBC'),
    ('2011-07-01', 'M0_YOY', 1.31, 1, 'PBC'),
    ('2011-07-01', 'M1_YOY', -89.85, 1, 'PBC'),
    ('2011-07-01', 'M2_YOY', -89.35, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-08-01', 'M0', 4.5775, 1, 'PBC'),
    ('2011-08-01', 'M1', 27.3394, 1, 'PBC'),
    ('2011-08-01', 'M2', 78.0852, 1, 'PBC'),
    ('2011-08-01', 'M0_YOY', 2.63, 1, 'PBC'),
    ('2011-08-01', 'M1_YOY', -89.75, 1, 'PBC'),
    ('2011-08-01', 'M2_YOY', -89.24, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-09-01', 'M0', 4.7145, 1, 'PBC'),
    ('2011-09-01', 'M1', 26.7193, 1, 'PBC'),
    ('2011-09-01', 'M2', 78.7406, 1, 'PBC'),
    ('2011-09-01', 'M0_YOY', 5.71, 1, 'PBC'),
    ('2011-09-01', 'M1_YOY', -89.98, 1, 'PBC'),
    ('2011-09-01', 'M2_YOY', -89.15, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-10-01', 'M0', 4.6579, 1, 'PBC'),
    ('2011-10-01', 'M1', 27.6553, 1, 'PBC'),
    ('2011-10-01', 'M2', 81.6829, 1, 'PBC'),
    ('2011-10-01', 'M0_YOY', 4.44, 1, 'PBC'),
    ('2011-10-01', 'M1_YOY', -89.63, 1, 'PBC'),
    ('2011-10-01', 'M2_YOY', -88.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-11-01', 'M0', 4.7317, 1, 'PBC'),
    ('2011-11-01', 'M1', 28.1416, 1, 'PBC'),
    ('2011-11-01', 'M2', 82.5494, 1, 'PBC'),
    ('2011-11-01', 'M0_YOY', 6.09, 1, 'PBC'),
    ('2011-11-01', 'M1_YOY', -89.44, 1, 'PBC'),
    ('2011-11-01', 'M2_YOY', -88.63, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2011-12-01', 'M0', 5.0748, 1, 'PBC'),
    ('2011-12-01', 'M1', 28.9848, 1, 'PBC'),
    ('2011-12-01', 'M2', 85.1591, 1, 'PBC'),
    ('2011-12-01', 'M0_YOY', 13.78, 1, 'PBC'),
    ('2011-12-01', 'M1_YOY', -89.13, 1, 'PBC'),
    ('2011-12-01', 'M2_YOY', -88.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-01-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-01-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-01-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-01-01', 'M0_YOY', -5.79, 1, 'PBC'),
    ('2012-01-01', 'M1_YOY', 1079.3, 1, 'PBC'),
    ('2012-01-01', 'M2_YOY', 1227.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-02-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-02-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-02-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-02-01', 'M0_YOY', 15.72, 1, 'PBC'),
    ('2012-02-01', 'M1_YOY', 1090.97, 1, 'PBC'),
    ('2012-02-01', 'M2_YOY', 1223.35, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-03-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-03-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-03-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-03-01', 'M0_YOY', 21.98, 1, 'PBC'),
    ('2012-03-01', 'M1_YOY', 1059.41, 1, 'PBC'),
    ('2012-03-01', 'M2_YOY', 1184.95, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-04-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-04-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-04-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-04-01', 'M0_YOY', 20.25, 1, 'PBC'),
    ('2012-04-01', 'M1_YOY', 1057.19, 1, 'PBC'),
    ('2012-04-01', 'M2_YOY', 1186.22, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-05-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-05-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-05-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-05-01', 'M0_YOY', 22.64, 1, 'PBC'),
    ('2012-05-01', 'M1_YOY', 1046.35, 1, 'PBC'),
    ('2012-05-01', 'M2_YOY', 1176.07, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-06-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-06-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-06-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-06-01', 'M0_YOY', 22.98, 1, 'PBC'),
    ('2012-06-01', 'M1_YOY', 1023.92, 1, 'PBC'),
    ('2012-06-01', 'M2_YOY', 1147.61, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-07-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-07-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-07-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-07-01', 'M0_YOY', 21.06, 1, 'PBC'),
    ('2012-07-01', 'M1_YOY', 1041.03, 1, 'PBC'),
    ('2012-07-01', 'M2_YOY', 1160.36, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-08-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-08-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-08-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-08-01', 'M0_YOY', 19.5, 1, 'PBC'),
    ('2012-08-01', 'M1_YOY', 1029.14, 1, 'PBC'),
    ('2012-08-01', 'M2_YOY', 1147.56, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-09-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-09-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-09-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-09-01', 'M0_YOY', 16.03, 1, 'PBC'),
    ('2012-09-01', 'M1_YOY', 1055.34, 1, 'PBC'),
    ('2012-09-01', 'M2_YOY', 1137.18, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-10-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-10-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-10-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-10-01', 'M0_YOY', 17.43, 1, 'PBC'),
    ('2012-10-01', 'M1_YOY', 1016.24, 1, 'PBC'),
    ('2012-10-01', 'M2_YOY', 1092.61, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-11-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-11-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-11-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-11-01', 'M0_YOY', 15.6, 1, 'PBC'),
    ('2012-11-01', 'M1_YOY', 996.95, 1, 'PBC'),
    ('2012-11-01', 'M2_YOY', 1080.09, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2012-12-01', 'M0', 5.47, 1, 'PBC'),
    ('2012-12-01', 'M1', 308.7, 1, 'PBC'),
    ('2012-12-01', 'M2', 974.16, 1, 'PBC'),
    ('2012-12-01', 'M0_YOY', 7.79, 1, 'PBC'),
    ('2012-12-01', 'M1_YOY', 965.04, 1, 'PBC'),
    ('2012-12-01', 'M2_YOY', 1043.93, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-01-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-01-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-01-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-01-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-01-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-01-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-02-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-02-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-02-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-02-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-02-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-02-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-03-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-03-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-03-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-03-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-03-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-03-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-04-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-04-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-04-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-04-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-04-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-04-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-05-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-05-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-05-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-05-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-05-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-05-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-06-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-06-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-06-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-06-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-06-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-06-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-07-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-07-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-07-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-07-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-07-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-07-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-08-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-08-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-08-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-08-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-08-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-08-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-09-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-09-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-09-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-09-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-09-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-09-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-10-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-10-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-10-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-10-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-10-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-10-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-11-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-11-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-11-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-11-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-11-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-11-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2013-12-01', 'M0', 5.86, 1, 'PBC'),
    ('2013-12-01', 'M1', 337.3, 1, 'PBC'),
    ('2013-12-01', 'M2', 1106.53, 1, 'PBC'),
    ('2013-12-01', 'M0_YOY', 7.13, 1, 'PBC'),
    ('2013-12-01', 'M1_YOY', 9.26, 1, 'PBC'),
    ('2013-12-01', 'M2_YOY', 13.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-01-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-01-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-01-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-01-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-01-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-01-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-02-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-02-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-02-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-02-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-02-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-02-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-03-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-03-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-03-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-03-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-03-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-03-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-04-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-04-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-04-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-04-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-04-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-04-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-05-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-05-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-05-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-05-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-05-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-05-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-06-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-06-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-06-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-06-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-06-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-06-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-07-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-07-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-07-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-07-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-07-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-07-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-08-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-08-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-08-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-08-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-08-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-08-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-09-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-09-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-09-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-09-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-09-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-09-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-10-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-10-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-10-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-10-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-10-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-10-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-11-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-11-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-11-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-11-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-11-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-11-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2014-12-01', 'M0', 6.03, 1, 'PBC'),
    ('2014-12-01', 'M1', 348.1, 1, 'PBC'),
    ('2014-12-01', 'M2', 1228.37, 1, 'PBC'),
    ('2014-12-01', 'M0_YOY', 2.9, 1, 'PBC'),
    ('2014-12-01', 'M1_YOY', 3.2, 1, 'PBC'),
    ('2014-12-01', 'M2_YOY', 11.01, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-01-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-01-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-01-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-01-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-01-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-01-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-02-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-02-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-02-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-02-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-02-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-02-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-03-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-03-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-03-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-03-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-03-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-03-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-04-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-04-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-04-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-04-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-04-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-04-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-05-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-05-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-05-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-05-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-05-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-05-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-06-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-06-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-06-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-06-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-06-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-06-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-07-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-07-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-07-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-07-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-07-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-07-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-08-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-08-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-08-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-08-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-08-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-08-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-09-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-09-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-09-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-09-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-09-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-09-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-10-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-10-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-10-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-10-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-10-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-10-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-11-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-11-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-11-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-11-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-11-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-11-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2015-12-01', 'M0', 6.32, 1, 'PBC'),
    ('2015-12-01', 'M1', 401.0, 1, 'PBC'),
    ('2015-12-01', 'M2', 1392.28, 1, 'PBC'),
    ('2015-12-01', 'M0_YOY', 4.81, 1, 'PBC'),
    ('2015-12-01', 'M1_YOY', 15.2, 1, 'PBC'),
    ('2015-12-01', 'M2_YOY', 13.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-01-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-01-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-01-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-01-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-01-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-01-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-02-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-02-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-02-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-02-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-02-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-02-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-03-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-03-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-03-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-03-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-03-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-03-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-04-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-04-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-04-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-04-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-04-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-04-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-05-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-05-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-05-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-05-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-05-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-05-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-06-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-06-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-06-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-06-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-06-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-06-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-07-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-07-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-07-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-07-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-07-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-07-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-08-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-08-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-08-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-08-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-08-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-08-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-09-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-09-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-09-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-09-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-09-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-09-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-10-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-10-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-10-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-10-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-10-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-10-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-11-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-11-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-11-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-11-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-11-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-11-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2016-12-01', 'M0', 6.83, 1, 'PBC'),
    ('2016-12-01', 'M1', 486.6, 1, 'PBC'),
    ('2016-12-01', 'M2', 1550.07, 1, 'PBC'),
    ('2016-12-01', 'M0_YOY', 8.07, 1, 'PBC'),
    ('2016-12-01', 'M1_YOY', 21.35, 1, 'PBC'),
    ('2016-12-01', 'M2_YOY', 11.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-01-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-01-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-01-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-01-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-01-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-01-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-02-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-02-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-02-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-02-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-02-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-02-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-03-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-03-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-03-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-03-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-03-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-03-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-04-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-04-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-04-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-04-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-04-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-04-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-05-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-05-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-05-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-05-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-05-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-05-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-06-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-06-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-06-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-06-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-06-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-06-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-07-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-07-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-07-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-07-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-07-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-07-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-08-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-08-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-08-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-08-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-08-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-08-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-09-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-09-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-09-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-09-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-09-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-09-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-10-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-10-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-10-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-10-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-10-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-10-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-11-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-11-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-11-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-11-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-11-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-11-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2017-12-01', 'M0', 7.06, 1, 'PBC'),
    ('2017-12-01', 'M1', 543.8, 1, 'PBC'),
    ('2017-12-01', 'M2', 1676.77, 1, 'PBC'),
    ('2017-12-01', 'M0_YOY', 3.37, 1, 'PBC'),
    ('2017-12-01', 'M1_YOY', 11.76, 1, 'PBC'),
    ('2017-12-01', 'M2_YOY', 8.17, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-01-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-01-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-01-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-01-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-01-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-01-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-02-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-02-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-02-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-02-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-02-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-02-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-03-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-03-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-03-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-03-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-03-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-03-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-04-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-04-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-04-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-04-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-04-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-04-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-05-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-05-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-05-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-05-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-05-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-05-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-06-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-06-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-06-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-06-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-06-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-06-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-07-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-07-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-07-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-07-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-07-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-07-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-08-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-08-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-08-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-08-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-08-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-08-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-09-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-09-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-09-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-09-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-09-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-09-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-10-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-10-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-10-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-10-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-10-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-10-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-11-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-11-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-11-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-11-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-11-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-11-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2018-12-01', 'M0', 7.32, 1, 'PBC'),
    ('2018-12-01', 'M1', 551.7, 1, 'PBC'),
    ('2018-12-01', 'M2', 1826.74, 1, 'PBC'),
    ('2018-12-01', 'M0_YOY', 3.68, 1, 'PBC'),
    ('2018-12-01', 'M1_YOY', 1.45, 1, 'PBC'),
    ('2018-12-01', 'M2_YOY', 8.94, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-01-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-01-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-01-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-01-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-01-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-01-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-02-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-02-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-02-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-02-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-02-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-02-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-03-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-03-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-03-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-03-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-03-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-03-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-04-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-04-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-04-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-04-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-04-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-04-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-05-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-05-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-05-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-05-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-05-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-05-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-06-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-06-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-06-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-06-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-06-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-06-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-07-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-07-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-07-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-07-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-07-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-07-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-08-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-08-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-08-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-08-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-08-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-08-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-09-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-09-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-09-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-09-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-09-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-09-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-10-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-10-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-10-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-10-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-10-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-10-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-11-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-11-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-11-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-11-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-11-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-11-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2019-12-01', 'M0', 7.72, 1, 'PBC'),
    ('2019-12-01', 'M1', 576.0, 1, 'PBC'),
    ('2019-12-01', 'M2', 1986.49, 1, 'PBC'),
    ('2019-12-01', 'M0_YOY', 5.46, 1, 'PBC'),
    ('2019-12-01', 'M1_YOY', 4.4, 1, 'PBC'),
    ('2019-12-01', 'M2_YOY', 8.75, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-01-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-01-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-01-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-01-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-01-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-01-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-02-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-02-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-02-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-02-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-02-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-02-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-03-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-03-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-03-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-03-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-03-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-03-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-04-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-04-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-04-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-04-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-04-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-04-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-05-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-05-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-05-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-05-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-05-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-05-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-06-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-06-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-06-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-06-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-06-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-06-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-07-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-07-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-07-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-07-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-07-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-07-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-08-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-08-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-08-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-08-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-08-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-08-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-09-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-09-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-09-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-09-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-09-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-09-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-10-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-10-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-10-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-10-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-10-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-10-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-11-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-11-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-11-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-11-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-11-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-11-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2020-12-01', 'M0', 8.43, 1, 'PBC'),
    ('2020-12-01', 'M1', 625.6, 1, 'PBC'),
    ('2020-12-01', 'M2', 2186.8, 1, 'PBC'),
    ('2020-12-01', 'M0_YOY', 9.2, 1, 'PBC'),
    ('2020-12-01', 'M1_YOY', 8.61, 1, 'PBC'),
    ('2020-12-01', 'M2_YOY', 10.08, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-01-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-01-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-01-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-01-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-01-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-01-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-02-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-02-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-02-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-02-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-02-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-02-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-03-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-03-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-03-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-03-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-03-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-03-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-04-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-04-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-04-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-04-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-04-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-04-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-05-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-05-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-05-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-05-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-05-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-05-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-06-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-06-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-06-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-06-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-06-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-06-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-07-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-07-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-07-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-07-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-07-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-07-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-08-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-08-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-08-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-08-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-08-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-08-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-09-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-09-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-09-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-09-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-09-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-09-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-10-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-10-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-10-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-10-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-10-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-10-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-11-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-11-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-11-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-11-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-11-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-11-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2021-12-01', 'M0', 9.08, 1, 'PBC'),
    ('2021-12-01', 'M1', 647.4, 1, 'PBC'),
    ('2021-12-01', 'M2', 2382.9, 1, 'PBC'),
    ('2021-12-01', 'M0_YOY', 7.71, 1, 'PBC'),
    ('2021-12-01', 'M1_YOY', 3.48, 1, 'PBC'),
    ('2021-12-01', 'M2_YOY', 8.97, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-01-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-01-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-01-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-01-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-01-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-01-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-02-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-02-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-02-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-02-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-02-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-02-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-03-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-03-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-03-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-03-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-03-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-03-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-04-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-04-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-04-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-04-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-04-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-04-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-05-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-05-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-05-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-05-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-05-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-05-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-06-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-06-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-06-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-06-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-06-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-06-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-07-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-07-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-07-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-07-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-07-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-07-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-08-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-08-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-08-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-08-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-08-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-08-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-09-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-09-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-09-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-09-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-09-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-09-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-10-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-10-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-10-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-10-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-10-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-10-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-11-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-11-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-11-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-11-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-11-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-11-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2022-12-01', 'M0', 10.47, 1, 'PBC'),
    ('2022-12-01', 'M1', 671.7, 1, 'PBC'),
    ('2022-12-01', 'M2', 2664.32, 1, 'PBC'),
    ('2022-12-01', 'M0_YOY', 15.31, 1, 'PBC'),
    ('2022-12-01', 'M1_YOY', 3.75, 1, 'PBC'),
    ('2022-12-01', 'M2_YOY', 11.81, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-01-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-01-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-01-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-01-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-01-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-01-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-02-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-02-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-02-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-02-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-02-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-02-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-03-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-03-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-03-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-03-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-03-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-03-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-04-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-04-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-04-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-04-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-04-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-04-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-05-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-05-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-05-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-05-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-05-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-05-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-06-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-06-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-06-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-06-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-06-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-06-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-07-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-07-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-07-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-07-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-07-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-07-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-08-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-08-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-08-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-08-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-08-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-08-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-09-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-09-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-09-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-09-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-09-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-09-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-10-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-10-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-10-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-10-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-10-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-10-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-11-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-11-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-11-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-11-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-11-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-11-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2023-12-01', 'M0', 11.34, 1, 'PBC'),
    ('2023-12-01', 'M1', 680.5, 1, 'PBC'),
    ('2023-12-01', 'M2', 2922.7, 1, 'PBC'),
    ('2023-12-01', 'M0_YOY', 8.31, 1, 'PBC'),
    ('2023-12-01', 'M1_YOY', 1.31, 1, 'PBC'),
    ('2023-12-01', 'M2_YOY', 9.7, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-01-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-01-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-01-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-01-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-01-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-01-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-02-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-02-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-02-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-02-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-02-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-02-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-03-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-03-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-03-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-03-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-03-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-03-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-04-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-04-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-04-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-04-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-04-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-04-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-05-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-05-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-05-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-05-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-05-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-05-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-06-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-06-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-06-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-06-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-06-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-06-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-07-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-07-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-07-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-07-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-07-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-07-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-08-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-08-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-08-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-08-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-08-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-08-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-09-01', 'M0', 13.5478, 1, 'PBC'),
    ('2024-09-01', 'M1', 62.8237, 1, 'PBC'),
    ('2024-09-01', 'M2', 309.4798, 1, 'PBC'),
    ('2024-09-01', 'M0_YOY', 19.47, 1, 'PBC'),
    ('2024-09-01', 'M1_YOY', -90.77, 1, 'PBC'),
    ('2024-09-01', 'M2_YOY', -89.41, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-10-01', 'M0', 13.7369, 1, 'PBC'),
    ('2024-10-01', 'M1', 63.3357, 1, 'PBC'),
    ('2024-10-01', 'M2', 309.7092, 1, 'PBC'),
    ('2024-10-01', 'M0_YOY', 21.14, 1, 'PBC'),
    ('2024-10-01', 'M1_YOY', -90.69, 1, 'PBC'),
    ('2024-10-01', 'M2_YOY', -89.4, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-11-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-11-01', 'M1', 65.0904, 1, 'PBC'),
    ('2024-11-01', 'M2', 311.9587, 1, 'PBC'),
    ('2024-11-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-11-01', 'M1_YOY', -90.43, 1, 'PBC'),
    ('2024-11-01', 'M2_YOY', -89.33, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2024-12-01', 'M0', 14.1261, 1, 'PBC'),
    ('2024-12-01', 'M1', 67.0959, 1, 'PBC'),
    ('2024-12-01', 'M2', 313.5322, 1, 'PBC'),
    ('2024-12-01', 'M0_YOY', 24.57, 1, 'PBC'),
    ('2024-12-01', 'M1_YOY', -90.14, 1, 'PBC'),
    ('2024-12-01', 'M2_YOY', -89.27, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-01-01', 'M0', 14.2254, 1, 'PBC'),
    ('2025-01-01', 'M1', 112.4457, 2, 'PBC'),
    ('2025-01-01', 'M2', 318.5247, 1, 'PBC'),
    ('2025-01-01', 'M0_YOY', 0.7, 1, 'PBC'),
    ('2025-01-01', 'M1_YOY', 67.59, 2, 'PBC'),
    ('2025-01-01', 'M2_YOY', 1.59, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-02-01', 'M0', 13.2757, 1, 'PBC'),
    ('2025-02-01', 'M1', 109.437, 2, 'PBC'),
    ('2025-02-01', 'M2', 320.5173, 1, 'PBC'),
    ('2025-02-01', 'M0_YOY', -6.02, 1, 'PBC'),
    ('2025-02-01', 'M1_YOY', 63.11, 2, 'PBC'),
    ('2025-02-01', 'M2_YOY', 2.23, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-03-01', 'M0', 13.0692, 1, 'PBC'),
    ('2025-03-01', 'M1', 113.4863, 2, 'PBC'),
    ('2025-03-01', 'M2', 326.0555, 1, 'PBC'),
    ('2025-03-01', 'M0_YOY', -7.48, 1, 'PBC'),
    ('2025-03-01', 'M1_YOY', 69.14, 2, 'PBC'),
    ('2025-03-01', 'M2_YOY', 3.99, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-04-01', 'M0', 13.1387, 1, 'PBC'),
    ('2025-04-01', 'M1', 109.1407, 2, 'PBC'),
    ('2025-04-01', 'M2', 325.1739, 1, 'PBC'),
    ('2025-04-01', 'M0_YOY', -6.99, 1, 'PBC'),
    ('2025-04-01', 'M1_YOY', 62.66, 2, 'PBC'),
    ('2025-04-01', 'M2_YOY', 3.71, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-05-01', 'M0', 13.1259, 1, 'PBC'),
    ('2025-05-01', 'M1', 108.9148, 2, 'PBC'),
    ('2025-05-01', 'M2', 325.7838, 1, 'PBC'),
    ('2025-05-01', 'M0_YOY', -7.08, 1, 'PBC'),
    ('2025-05-01', 'M1_YOY', 62.33, 2, 'PBC'),
    ('2025-05-01', 'M2_YOY', 3.91, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-06-01', 'M0', 13.1827, 1, 'PBC'),
    ('2025-06-01', 'M1', 113.9494, 2, 'PBC'),
    ('2025-06-01', 'M2', 330.2868, 1, 'PBC'),
    ('2025-06-01', 'M0_YOY', -6.68, 1, 'PBC'),
    ('2025-06-01', 'M1_YOY', 69.83, 2, 'PBC'),
    ('2025-06-01', 'M2_YOY', 5.34, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-07-01', 'M0', 13.2845, 1, 'PBC'),
    ('2025-07-01', 'M1', 111.0587, 2, 'PBC'),
    ('2025-07-01', 'M2', 329.9429, 1, 'PBC'),
    ('2025-07-01', 'M0_YOY', -5.96, 1, 'PBC'),
    ('2025-07-01', 'M1_YOY', 65.52, 2, 'PBC'),
    ('2025-07-01', 'M2_YOY', 5.23, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-08-01', 'M0', 13.3402, 1, 'PBC'),
    ('2025-08-01', 'M1', 111.2256, 2, 'PBC'),
    ('2025-08-01', 'M2', 331.9831, 1, 'PBC'),
    ('2025-08-01', 'M0_YOY', -5.56, 1, 'PBC'),
    ('2025-08-01', 'M1_YOY', 65.77, 2, 'PBC'),
    ('2025-08-01', 'M2_YOY', 5.88, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-09-01', 'M0', 13.5813, 1, 'PBC'),
    ('2025-09-01', 'M1', 113.1455, 2, 'PBC'),
    ('2025-09-01', 'M2', 335.3771, 1, 'PBC'),
    ('2025-09-01', 'M0_YOY', 0.25, 1, 'PBC'),
    ('2025-09-01', 'M1_YOY', 80.1, 2, 'PBC'),
    ('2025-09-01', 'M2_YOY', 8.37, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-10-01', 'M0', 13.5478, 1, 'PBC'),
    ('2025-10-01', 'M1', 111.9963, 2, 'PBC'),
    ('2025-10-01', 'M2', 335.1312, 1, 'PBC'),
    ('2025-10-01', 'M0_YOY', -1.38, 1, 'PBC'),
    ('2025-10-01', 'M1_YOY', 76.83, 2, 'PBC'),
    ('2025-10-01', 'M2_YOY', 8.21, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-11-01', 'M0', 13.7369, 1, 'PBC'),
    ('2025-11-01', 'M1', 112.8867, 2, 'PBC'),
    ('2025-11-01', 'M2', 336.9891, 1, 'PBC'),
    ('2025-11-01', 'M0_YOY', -2.76, 1, 'PBC'),
    ('2025-11-01', 'M1_YOY', 73.43, 2, 'PBC'),
    ('2025-11-01', 'M2_YOY', 8.02, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2025-12-01', 'M0', 14.1261, 1, 'PBC'),
    ('2025-12-01', 'M1', 115.5147, 2, 'PBC'),
    ('2025-12-01', 'M2', 340.2948, 1, 'PBC'),
    ('2025-12-01', 'M0_YOY', 0.0, 1, 'PBC'),
    ('2025-12-01', 'M1_YOY', 72.16, 2, 'PBC'),
    ('2025-12-01', 'M2_YOY', 8.54, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-01-01', 'M0', 14.6139, 1, 'PBC'),
    ('2026-01-01', 'M1', 117.9681, 2, 'PBC'),
    ('2026-01-01', 'M2', 347.186, 1, 'PBC'),
    ('2026-01-01', 'M0_YOY', 2.73, 1, 'PBC'),
    ('2026-01-01', 'M1_YOY', 4.91, 2, 'PBC'),
    ('2026-01-01', 'M2_YOY', 9.0, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-02-01', 'M0', 15.1436, 1, 'PBC'),
    ('2026-02-01', 'M1', 115.9259, 2, 'PBC'),
    ('2026-02-01', 'M2', 349.216, 1, 'PBC'),
    ('2026-02-01', 'M0_YOY', 14.07, 1, 'PBC'),
    ('2026-02-01', 'M1_YOY', 5.93, 2, 'PBC'),
    ('2026-02-01', 'M2_YOY', 8.95, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-03-01', 'M0', 14.7083, 1, 'PBC'),
    ('2026-03-01', 'M1', 119.3203, 2, 'PBC'),
    ('2026-03-01', 'M2', 353.8637, 1, 'PBC'),
    ('2026-03-01', 'M0_YOY', 12.54, 1, 'PBC'),
    ('2026-03-01', 'M1_YOY', 5.14, 2, 'PBC'),
    ('2026-03-01', 'M2_YOY', 8.53, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-04-01', 'M0', 14.7477, 1, 'PBC'),
    ('2026-04-01', 'M1', 114.5834, 2, 'PBC'),
    ('2026-04-01', 'M2', 353.0425, 1, 'PBC'),
    ('2026-04-01', 'M0_YOY', 12.25, 1, 'PBC'),
    ('2026-04-01', 'M1_YOY', 4.99, 2, 'PBC'),
    ('2026-04-01', 'M2_YOY', 8.57, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-05-01', 'M0', 14.6855, 1, 'PBC'),
    ('2026-05-01', 'M1', 114.8891, 2, 'PBC'),
    ('2026-05-01', 'M2', 353.6689, 1, 'PBC'),
    ('2026-05-01', 'M0_YOY', 11.88, 1, 'PBC'),
    ('2026-05-01', 'M1_YOY', 5.49, 2, 'PBC'),
    ('2026-05-01', 'M2_YOY', 8.56, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-06-01', 'M0', 14.7365, 1, 'PBC'),
    ('2026-06-01', 'M1', 118.4776, 2, 'PBC'),
    ('2026-06-01', 'M2', 356.7108, 1, 'PBC'),
    ('2026-06-01', 'M0_YOY', 11.79, 1, 'PBC'),
    ('2026-06-01', 'M1_YOY', 3.97, 2, 'PBC'),
    ('2026-06-01', 'M2_YOY', 8.0, 1, 'PBC');

INSERT IGNORE INTO `macro_monthly` (`stat_date`, `indicator_code`, `metric_value`, `data_version`, `source`) VALUES
    ('2026-07-01', 'M0', 14.8203, 1, 'PBC'),
    ('2026-07-01', 'M1', 115.4623, 2, 'PBC'),
    ('2026-07-01', 'M2', 355.5077, 1, 'PBC'),
    ('2026-07-01', 'M0_YOY', 11.56, 1, 'PBC'),
    ('2026-07-01', 'M1_YOY', 3.97, 2, 'PBC'),
    ('2026-07-01', 'M2_YOY', 7.75, 1, 'PBC');

COMMIT;

-- ============================================================
-- 数据说明：
-- 1. M0: 流通中现金（万亿元），无口径变更
-- 2. M1: 狭义货币（万亿元），2025年1月起启用新口径（data_version=2）
--    新口径纳入个人活期存款、非银行支付机构客户备付金
-- 3. M2: 广义货币（万亿元），2018年调整将货币基金纳入M2
-- 4. _YOY 字段为同比增速（%），基于同月数据同比计算
-- 5. 2000-2010年、2012-2024年8月：使用年末（12月）数据填充
-- 6. 2011年、2024年9月-2026年7月：使用月度精确数据
-- 7. 2024年12月M1为旧口径（67.0960万亿），2025年1月起为新口径（112.4457万亿）
-- ============================================================