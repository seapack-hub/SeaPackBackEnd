CREATE TABLE IF NOT EXISTS `monitor_threshold_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_id` BIGINT UNSIGNED NOT NULL COMMENT '关联 user_stock_monitor 表的 ID',
  
  -- 阈值与触发规则
  `threshold_rate` DECIMAL(5,4) NOT NULL COMMENT '阈值比例，如 0.0300 代表 3%',
  `trigger_type` VARCHAR(20) NOT NULL COMMENT '触发类型: CROSS_UP-向上突破, CROSS_DOWN-向下跌破',
  
  -- 状态控制
  `is_active` TINYINT DEFAULT 1 COMMENT '该阈值规则是否生效 (1-是, 0-否)',
  
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_monitor_id` (`monitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股息阈值配置表';

ALTER TABLE `monitor_threshold_config` 
ADD COLUMN `last_triggered_time` DATETIME DEFAULT NULL COMMENT '上次触发时间，用于防抖',
ADD COLUMN `trigger_value` DECIMAL(10,4) DEFAULT NULL COMMENT '实际触发时的数值快照';