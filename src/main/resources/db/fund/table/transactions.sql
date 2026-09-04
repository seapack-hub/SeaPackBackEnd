CREATE TABLE transactions (
    -- 唯一标识与关联信息
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '交易记录唯一标识ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID，外键引用user(id)',
    fund_code VARCHAR(10) NOT NULL COMMENT '关联基金代码，外键引用fund_base_info(fund_code)',
    
    -- 交易核心数据
    trade_type ENUM('subscribe', 'purchase', 'redeem', 'sell') NOT NULL COMMENT '交易类型:申购/买入/赎回/卖出',
    trade_date DATE NOT NULL COMMENT '交易发生日期（净值日期）',
    trade_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易提交时间',
    nav DECIMAL(10, 4) NOT NULL COMMENT '交易时基金的单位净值',
    shares DECIMAL(15, 4) NOT NULL COMMENT '交易份额（正数）',
    amount DECIMAL(12, 2) NOT NULL COMMENT '交易金额（元）。申购/买入为正，赎回/卖出为负。',
    fee DECIMAL(10, 2) DEFAULT 0.00 COMMENT '交易手续费',
    
    -- 状态与备注
    status ENUM('pending', 'confirmed', 'failed') DEFAULT 'confirmed' COMMENT '交易状态:待确认/已确认/失败',
    note TEXT COMMENT '交易备注（如赎回至银行卡）',
    
    -- 系统记录
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (fund_code) REFERENCES fund_base_info(fund_code),
    
    -- 索引优化
    INDEX idx_user_id (user_id),
    INDEX idx_fund_code (fund_code),
    INDEX idx_trade_date (trade_date),
    INDEX idx_user_trade_date (user_id, trade_date) -- 优化按用户和日期范围的查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金交易记录表，所有买入和卖出行为的唯一数据源';