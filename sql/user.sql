/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : yun-picture

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 07/03/2026 18:22:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
  `vipExpireTime` datetime NULL DEFAULT NULL COMMENT '会员到期时间',
  `vipCode` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员码',
  `vipNumber` bigint NULL DEFAULT NULL COMMENT '会员编号',
  `shareCode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分享码',
  `inviteUser` bigint NULL DEFAULT NULL COMMENT '邀请用户id',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_userAccount`(`userAccount` ASC) USING BTREE,
  INDEX `idx_userName`(`userName` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2026487421816496131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2001556182340935681, '我是老6', 'c217c89240bf69d08764ff1410616ee7', NULL, NULL, '我平时喜欢吹牛皮', 'admin', NULL, NULL, NULL, NULL, NULL, '2025-12-18 15:32:35', '2025-12-18 15:32:35', '2026-02-25 11:34:22', 0);
INSERT INTO `user` VALUES (2001915226368913409, 'admin', 'c217c89240bf69d08764ff1410616ee7', '我是老大', '', '', 'admin', NULL, NULL, NULL, NULL, NULL, '2025-12-19 15:19:18', '2025-12-19 15:19:18', '2026-02-25 12:54:56', 0);
INSERT INTO `user` VALUES (2003011936369504258, 'test1', 'c217c89240bf69d08764ff1410616ee7', NULL, NULL, NULL, 'user', NULL, NULL, NULL, NULL, NULL, '2025-12-22 15:57:14', '2025-12-22 15:57:14', '2025-12-22 15:57:14', 0);
INSERT INTO `user` VALUES (2003012479758364673, 'test6', 'c217c89240bf69d08764ff1410616ee7', NULL, NULL, NULL, 'user', NULL, NULL, NULL, NULL, NULL, '2025-12-22 15:59:24', '2025-12-22 15:59:24', '2025-12-22 15:59:24', 0);
INSERT INTO `user` VALUES (2008373936259731458, 'test', 'c217c89240bf69d08764ff1410616ee7', NULL, NULL, NULL, 'user', NULL, NULL, NULL, NULL, NULL, '2026-01-06 11:03:54', '2026-01-06 11:03:54', '2026-01-06 11:03:54', 0);
INSERT INTO `user` VALUES (2008374503631704065, 'test2', 'c217c89240bf69d08764ff1410616ee7', NULL, NULL, NULL, 'user', NULL, NULL, NULL, NULL, NULL, '2026-01-06 11:06:10', '2026-01-06 11:06:10', '2026-01-06 11:06:10', 0);

SET FOREIGN_KEY_CHECKS = 1;
