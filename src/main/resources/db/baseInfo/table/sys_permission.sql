CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父级权限ID (0表示顶级)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限/菜单名称',
  `perm_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限标识符',
  `type` tinyint NOT NULL COMMENT '资源类型 (1-目录, 2-菜单, 3-按钮)',
  `path` varchar(200) DEFAULT NULL COMMENT '前端路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '前端组件路径',
  `sort_order` int DEFAULT 0 COMMENT '显示排序',
  `status` tinyint DEFAULT 1 COMMENT '状态 (1正常 0停用)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';