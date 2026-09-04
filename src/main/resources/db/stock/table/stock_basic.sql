CREATE TABLE `stock_basic` (
  `stock_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码，如 600519',
  `stock_name` VARCHAR(100) NOT NULL COMMENT '股票名称，如 贵州茅台',
  `exchange` VARCHAR(10) NOT NULL COMMENT '交易所，如 SH(沪市), SZ(深市)',
  `industry` VARCHAR(50) DEFAULT NULL COMMENT '所属行业，如 银行、煤炭',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `uk_stock_code` (`stock_code`) COMMENT '股票代码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票基础信息表';