CREATE TABLE `sys_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
    `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint DEFAULT 1 COMMENT '状态 (1正常 0停用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_role_code` (`role_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';