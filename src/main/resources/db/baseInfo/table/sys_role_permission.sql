CREATE TABLE `sys_role_permission` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
   PRIMARY KEY (`role_id`, `permission_id`) USING BTREE,
   KEY `idx_permission_id` (`permission_id`) USING BTREE,
   CONSTRAINT `fk_role_perm_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
   CONSTRAINT `fk_role_perm_perm_id` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与权限关联表';