CREATE TABLE `alert_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_id` BIGINT UNSIGNED NOT NULL COMMENT '触发的监控规则ID',
  `triggered_rate` DECIMAL(5,2) NOT NULL COMMENT '触发时的实际股息率(%)',
  `triggered_price` DECIMAL(10,3) NOT NULL COMMENT '触发时的股价',
  `sent_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_time` (`rule_id`, `sent_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警通知日志表';