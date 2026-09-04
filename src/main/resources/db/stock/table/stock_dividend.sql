CREATE TABLE `stock_dividend` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  
  -- 核心关联字段修改为股票代码
  `stock_code` VARCHAR(20) NOT NULL COMMENT '关联股票代码，如 600519',
  
  `year` INT NOT NULL COMMENT '分红所属年份',
  `dividend_type` VARCHAR(20) NOT NULL COMMENT '分红类型：INTERIM-中期分红, FINAL-末期分红/年度分红',
  
  -- 多维度分红指标
  `cash_per_share` DECIMAL(10,4) DEFAULT 0.0000 COMMENT '每股派发现金金额(元)，无现金分红则为0',
  `bonus_shares_per_10` DECIMAL(10,4) DEFAULT 0.0000 COMMENT '每10股送红股数量(股)',
  `transfer_shares_per_10` DECIMAL(10,4) DEFAULT 0.0000 COMMENT '每10股转增股本数量(股)',
  
  -- 方案描述与状态
  `plan_text` VARCHAR(255) DEFAULT NULL COMMENT '分红方案原文，如: 10派5元送3股转2股',
  `announcement_date` DATE DEFAULT NULL COMMENT '预案公告日期',
  `ex_dividend_date` DATE DEFAULT NULL COMMENT '除权除息日',
  `status` VARCHAR(20) DEFAULT 'PROPOSED' COMMENT '实施状态：PROPOSED-预案, APPROVED-已批准, IMPLEMENTED-已实施',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  
  -- 建立复合索引，加速按股票和年份查询
  KEY `idx_stock_year` (`stock_code`, `year`),
  KEY `idx_ex_date` (`ex_dividend_date`),
  
  -- 添加外键约束，但不再级联删除，防止误删股票导致分红数据丢失
  CONSTRAINT `fk_dividend_stock_code` FOREIGN KEY (`stock_code`) REFERENCES `stock_basic` (`stock_code`) ON UPDATE CASCADE ON DELETE RESTRICT
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票分红明细表';