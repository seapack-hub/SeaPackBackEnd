CREATE TABLE `department` (
  `dept_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `parent_dept_id` INT UNSIGNED DEFAULT NULL COMMENT '父部门ID（NULL表示根部门）',
  `dept_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '部门层级（1=一级，2=二级，...）',
  `dept_path` VARCHAR(255) COMMENT '层级路径（冗余优化，如1/3/5）',
  `seq` TINYINT UNSIGNED DEFAULT 0 COMMENT '同级排序序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dept_id`),
  FOREIGN KEY (`parent_dept_id`) REFERENCES `department`(`dept_id`)
      ON DELETE CASCADE ON UPDATE CASCADE, -- 级联更新/删除
  INDEX `idx_parent` (`parent_dept_id`),  -- 优化递归查询
  INDEX `idx_level` (`dept_level`),       -- 加速层级过滤
  INDEX `idx_path` (`dept_path`(20))      -- 前缀索引优化路径查询
) ENGINE=InnoDB COMMENT='部门表（层级结构）';