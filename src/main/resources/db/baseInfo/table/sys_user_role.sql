CREATE TABLE `sys_user_role` (
 `user_id` bigint NOT NULL COMMENT '用户ID',
 `role_id` bigint NOT NULL COMMENT '角色ID',
 PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
 KEY `idx_role_id` (`role_id`) USING BTREE,
 -- 【重点】直接在建表时声明外键并设置级联删除
 CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
 CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与角色关联表';