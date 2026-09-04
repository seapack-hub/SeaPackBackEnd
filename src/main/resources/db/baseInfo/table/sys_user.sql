DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
     `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
     `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
     `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
     `gender` tinyint NULL DEFAULT NULL COMMENT '性别 (1男 0女)',
     `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
     `status` tinyint NULL DEFAULT NULL COMMENT '状态 (1正常 0停用)',
     `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
     `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称',
     `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
     `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;