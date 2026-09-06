CREATE TABLE IF NOT EXISTS `stock_realtime_quote` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',

    -- 核心行情字段
    `current_price` DECIMAL(10,4) NOT NULL COMMENT '当前最新价',
    `open_price` DECIMAL(10,4) DEFAULT NULL COMMENT '今日开盘价',
    `high_price` DECIMAL(10,4) DEFAULT NULL COMMENT '今日最高价',
    `low_price` DECIMAL(10,4) DEFAULT NULL COMMENT '今日最低价',

    -- 衍生指标（由后台任务计算并写入，极大提升前端查询效率）
    `dynamic_yield` DECIMAL(8,6) DEFAULT NULL COMMENT '动态股息率(%) = 预期每股分红 / current_price',

    -- 时间戳字段
    `trade_date` DATE NOT NULL COMMENT '交易日期（如 2024-05-20）',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据更新时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_trade_date` (`stock_code`, `trade_date`) COMMENT '防止同一天产生多条重复记录',
    KEY `idx_update_time` (`update_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票实时行情与股息率快照表';