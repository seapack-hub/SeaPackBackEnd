CREATE TABLE IF NOT EXISTS `user_stock_monitor` (
                                                    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
                                                    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',

    -- 基础配置
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用监控 (1-启用, 0-暂停)',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户对这只股票的备注（如：长线收息）',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_stock` (`user_id`, `stock_code`) COMMENT '防止同一用户重复添加同一只股票',
    KEY `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户股票监控池';