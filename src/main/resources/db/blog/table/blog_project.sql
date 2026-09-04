CREATE TABLE `blog_project` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(100) NOT NULL                COMMENT '项目名称',
  `description` VARCHAR(500) NOT NULL DEFAULT ''      COMMENT '项目描述',
  `icon`        VARCHAR(50)  NOT NULL DEFAULT ''      COMMENT 'SPIcon图标名',
  `color`       VARCHAR(20)  NOT NULL DEFAULT '#409eff' COMMENT '图标颜色',
  `bg_color`    VARCHAR(50)  NOT NULL DEFAULT 'rgba(64,158,255,0.1)' COMMENT '图标背景色',
  `url`         VARCHAR(500) NOT NULL DEFAULT ''      COMMENT '项目链接',
  `sort`        INT          NOT NULL DEFAULT 0       COMMENT '排序号',
  `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '状态: 0隐藏 1显示',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客开源项目表';