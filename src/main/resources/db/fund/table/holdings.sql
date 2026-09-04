CREATE TABLE holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '持仓记录唯一标识ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID，外键引用users(id)', -- 去掉了 UNSIGNED
    fund_code VARCHAR(10) NOT NULL COMMENT '关联基金代码，外键引用funds(fund_code)',
    total_shares DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '持有总份额（单位：份）',
    available_shares DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '可用份额（可赎回份额）',
    frozen_shares DECIMAL(20,4) NOT NULL DEFAULT 0.0000 COMMENT '冻结份额（申购未确认等）',
    avg_cost_price DECIMAL(10,4) NOT NULL COMMENT '平均成本单价（元/份）',
    total_cost DECIMAL(15,2) NOT NULL COMMENT '持仓总成本（元）',
    cost_principal DECIMAL(15,2) NOT NULL COMMENT '实际投入本金（元）',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (fund_code) REFERENCES fund_base_info(fund_code),
    
    -- 唯一约束，防止同一用户同一基金重复持仓
    UNIQUE KEY unique_user_fund (user_id, fund_code),
    
    -- 索引优化
    INDEX idx_user_id (user_id),
    INDEX idx_fund_code (fund_code),
    INDEX idx_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基金持仓表，记录实时持有的基金份额和成本信息';