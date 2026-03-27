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

 Date: 27/03/2026 15:24:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for space
-- ----------------------------
DROP TABLE IF EXISTS `space`;
CREATE TABLE `space`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spaceName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '空间名称',
  `spaceLevel` int NULL DEFAULT 0 COMMENT '空间等级:0-普通版 1-专业版 2-旗舰版',
  `maxSize` bigint NULL DEFAULT 0 COMMENT '空间中图片的最大总大小',
  `maxCount` bigint NULL DEFAULT 0 COMMENT '空间中图片的最大数量',
  `totalSize` bigint NULL DEFAULT 0 COMMENT '当前空间中图片的总大小',
  `totalCount` bigint NULL DEFAULT 0 COMMENT '当前空间中图片的数量',
  `userId` bigint NOT NULL COMMENT '创建用户id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `spaceType` int NOT NULL DEFAULT 0 COMMENT '空间类型：0-私有个人空间 1-团队空间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_spaceName`(`spaceName` ASC) USING BTREE,
  INDEX `idx_spaceLevel`(`spaceLevel` ASC) USING BTREE,
  INDEX `idx_spaceType`(`spaceType` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2030485422461386754 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '空间' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of space
-- ----------------------------
INSERT INTO `space` VALUES (2008371139942469634, '测试', 0, 104857600, 100, 1738300, 3, 2001915226368913409, '2026-01-06 10:52:48', '2026-01-06 10:52:48', '2026-03-26 11:04:19', 0, 0);
INSERT INTO `space` VALUES (2008374503782699010, '默认私有空间', 0, 104857600, 100, 0, 0, 2008374503631704065, '2026-01-06 11:06:10', '2026-01-06 11:06:10', '2026-03-08 11:33:27', 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
