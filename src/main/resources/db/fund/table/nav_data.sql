CREATE TABLE nav_data (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '记录唯一标识ID',
  fund_code VARCHAR(10) NOT NULL COMMENT '基金代码，外键关联fund_base_info表',
  net_asset_value DECIMAL(8,4) UNSIGNED NOT NULL COMMENT '单位净值',
  accumulated_nav DECIMAL(8,4) UNSIGNED NOT NULL COMMENT '累计净值',
  adjusted_nav DECIMAL(8,4) UNSIGNED COMMENT '复权净值',
  nav_date DATE NOT NULL COMMENT '净值日期',
  daily_growth_rate DECIMAL(6,4) COMMENT '日增长率',
  dividend_per_unit DECIMAL(6,4) UNSIGNED DEFAULT 0.0000 COMMENT '每份分红金额',
  adjustment_factor DECIMAL(10,6) UNSIGNED COMMENT '复权因子',
  data_source VARCHAR(20) DEFAULT 'tushare' COMMENT '数据来源',
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

  -- 外键约束
  FOREIGN KEY (fund_code) REFERENCES fund_base_info(fund_code),

  -- 唯一约束，防止同一基金同一日插入多条净值记录
  UNIQUE KEY unique_fund_date (fund_code, nav_date),

  -- 索引优化
  INDEX idx_fund_code (fund_code),
  INDEX idx_nav_date (nav_date),
  INDEX idx_fund_date (fund_code, nav_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金历史净值表';