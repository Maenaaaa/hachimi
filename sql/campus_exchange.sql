/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : campus_exchange

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 08/06/2026 21:19:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍏?憡ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍏?憡鏍囬?',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍏?憡鍐呭?',
  `publisher_id` bigint NOT NULL COMMENT '鍙戝竷浜篒D锛堢?鐞嗗憳锛',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戝竷鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher_id`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍏?憡琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES (1, '测试公告功能', '测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告测试公告', 5, '2026-06-07 23:33:43', '2026-06-07 23:33:43', 0);
INSERT INTO `announcement` VALUES (2, '1212121', '1212121212121212', 5, '2026-06-07 23:36:28', '2026-06-07 23:36:28', 0);
INSERT INTO `announcement` VALUES (3, '32132', '3213123123123123123123123123123', 5, '2026-06-08 20:21:37', '2026-06-08 20:21:37', 0);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鍒嗙被鍥炬爣鏍囪瘑',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭搴忓彿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍟嗗搧鍒嗙被琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '教材书籍', '📚', 1, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);
INSERT INTO `category` VALUES (2, '数码产品', '💻', 2, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);
INSERT INTO `category` VALUES (3, '宿舍用品', '🏠', 3, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);
INSERT INTO `category` VALUES (4, '体育用品', '⚽', 4, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);
INSERT INTO `category` VALUES (5, '自行车', '🚲', 5, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);
INSERT INTO `category` VALUES (6, '服饰鞋包', '👗', 6, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);
INSERT INTO `category` VALUES (7, '其他', '📦', 7, '2026-06-03 09:22:56', '2026-06-07 17:10:02', 0);

-- ----------------------------
-- Table structure for conversation
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽ID',
  `goods_id` bigint NOT NULL COMMENT '鍏宠仈鍟嗗搧ID',
  `buyer_id` bigint NOT NULL COMMENT '涔板?ID',
  `seller_id` bigint NOT NULL COMMENT '鍗栧?ID',
  `last_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鏈?悗涓?潯娑堟伅',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '鏈?悗娑堟伅鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_last_msg_time`(`last_message_time` ASC) USING BTREE,
  CONSTRAINT `fk_conv_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_conv_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_conv_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '浼氳瘽琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation
-- ----------------------------
INSERT INTO `conversation` VALUES (1, 2, 6, 9, '{\"type\":\"card\",\"goodsId\":2,\"title\":\"琪琪\",\"coverImag', '2026-06-08 18:19:51', '2026-06-07 17:22:26', '2026-06-07 17:22:26', 0);
INSERT INTO `conversation` VALUES (2, 3, 9, 6, '{\"type\":\"card\",\"goodsId\":3,\"title\":\"哈机密\",\"coverIma', '2026-06-08 00:28:50', '2026-06-08 00:13:39', '2026-06-08 00:13:39', 0);
INSERT INTO `conversation` VALUES (3, 12, 6, 9, 'xingb', '2026-06-08 20:16:01', '2026-06-08 20:14:34', '2026-06-08 20:14:34', 0);
INSERT INTO `conversation` VALUES (4, 5, 6, 9, '{\"type\":\"card\",\"goodsId\":5,\"title\":\"nas\",\"coverIma', '2026-06-08 20:16:32', '2026-06-08 20:16:31', '2026-06-08 20:16:31', 0);

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏀惰棌ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `goods_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鏀惰棌鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_goods`(`user_id` ASC, `goods_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鏀惰棌琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, 6, 2, '2026-06-07 17:22:24');
INSERT INTO `favorite` VALUES (2, 9, 3, '2026-06-08 00:19:58');

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍏虫敞ID',
  `follower_id` bigint NOT NULL COMMENT '鍏虫敞鑰匢D',
  `followee_id` bigint NOT NULL COMMENT '琚?叧娉ㄨ?ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍏虫敞鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follower_followee`(`follower_id` ASC, `followee_id` ASC) USING BTREE,
  INDEX `idx_follower_id`(`follower_id` ASC) USING BTREE,
  INDEX `idx_followee_id`(`followee_id` ASC) USING BTREE,
  CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍏虫敞琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follow
-- ----------------------------
INSERT INTO `follow` VALUES (9, 9, 6, '2026-06-08 19:13:51');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍟嗗搧ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍟嗗搧鏍囬?',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '鍟嗗搧鎻忚堪',
  `original_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '鍘熶环',
  `price` decimal(10, 2) NOT NULL COMMENT '杞??浠锋牸',
  `condition` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MINOR_WEAR' COMMENT '鎴愯壊',
  `campus` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鎵?湪鏍″尯',
  `trade_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SELL' COMMENT '交易类型: SELL=出售, EXCHANGE=置换',
  `category_id` bigint NOT NULL COMMENT '鍒嗙被ID',
  `user_id` bigint NOT NULL COMMENT '鍙戝竷鑰匢D',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '鐘舵?: PENDING_REVIEW, ACTIVE, INACTIVE, REJECTED, TAKEN_DOWN',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '娴忚?娆℃暟',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '鏀惰棌娆℃暟',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戝竷鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_price`(`price` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_desc`(`title`, `description`),
  CONSTRAINT `fk_goods_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍟嗗搧琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, '马家', '好', 2.00, 1.00, 'good', '主校区', 'SELL', 1, 6, 'ACTIVE', 0, 0, '2026-06-07 17:11:38', '2026-06-08 19:56:42', 1);
INSERT INTO `goods` VALUES (2, '琪琪', '年后大卡的卡卡是大妈看了上面的了卡米拉昆明', 20.00, 2.00, 'new', '西区', 'SELL', 3, 9, 'INACTIVE', 0, 1, '2026-06-07 17:19:36', '2026-06-07 17:19:36', 0);
INSERT INTO `goods` VALUES (3, '哈机密', 'KLGDKBNGLKDSAGLKDSBNGLKDSNGLKDSN', 0.00, 0.00, 'new', '哈机密', 'EXCHANGE', 4, 6, 'SOLD', 0, 1, '2026-06-07 19:32:17', '2026-06-08 19:56:41', 1);
INSERT INTO `goods` VALUES (4, '1111', '121212121', 21212.00, 2121.00, 'LIKE_NEW', '主校区', 'SELL', 1, 9, 'INACTIVE', 0, 0, '2026-06-08 00:01:27', '2026-06-08 00:01:27', 0);
INSERT INTO `goods` VALUES (5, 'nas', '111111', 12122.22, 121.22, 'LIKE_NEW', '主校区', 'SELL', 1, 9, 'ACTIVE', 0, 0, '2026-06-08 00:26:05', '2026-06-08 00:26:05', 0);
INSERT INTO `goods` VALUES (6, '12121', '121212212测试修改1', 121212.00, 12121.00, 'LIKE_NEW', '主校区', 'SELL', 1, 9, 'SOLD', 0, 0, '2026-06-08 00:34:28', '2026-06-08 00:34:28', 0);
INSERT INTO `goods` VALUES (7, '测试', '1122323', 0.00, 0.00, 'BRAND_NEW', '主校区', 'EXCHANGE', 1, 9, 'INACTIVE', 0, 0, '2026-06-08 13:41:35', '2026-06-08 13:41:35', 0);
INSERT INTO `goods` VALUES (8, '测试', '2323232323', 0.00, 1111.00, 'LIKE_NEW', '主校区', 'SELL', 1, 6, 'SOLD', 0, 0, '2026-06-08 17:18:51', '2026-06-08 19:56:39', 1);
INSERT INTO `goods` VALUES (9, '张三的东西', '2121', 22.00, 112.00, 'LIKE_NEW', '主校区', 'SELL', 1, 6, 'SOLD', 0, 0, '2026-06-08 17:42:39', '2026-06-08 19:56:36', 1);
INSERT INTO `goods` VALUES (10, '曼波的东西', '1212121212121', 12121.00, 1212.00, 'LIKE_NEW', '主校区', 'SELL', 1, 9, 'SOLD', 0, 0, '2026-06-08 17:52:46', '2026-06-08 17:52:46', 0);
INSERT INTO `goods` VALUES (11, '测试置换', '1111', 111.00, 111.00, 'BRAND_NEW', '主校区', 'EXCHANGE', 1, 6, 'ACTIVE', 0, 0, '2026-06-08 18:23:07', '2026-06-08 18:23:07', 0);
INSERT INTO `goods` VALUES (12, '测新发送', '111', 222.00, 111.00, 'MINOR_WEAR', '主校区', 'EXCHANGE', 1, 9, 'ACTIVE', 0, 0, '2026-06-08 18:36:22', '2026-06-08 18:36:22', 0);
INSERT INTO `goods` VALUES (13, '21212', 'wewewewew', 2121.00, 12.23, 'MINOR_WEAR', '主校区', 'SELL', 1, 6, 'ACTIVE', 0, 0, '2026-06-08 18:48:37', '2026-06-08 18:48:37', 0);
INSERT INTO `goods` VALUES (14, '12121212', 'weeweww', 0.00, 0.00, 'LIKE_NEW', '主校区', 'EXCHANGE', 1, 6, 'ACTIVE', 0, 0, '2026-06-08 20:25:08', '2026-06-08 20:25:08', 0);

-- ----------------------------
-- Table structure for goods_comment
-- ----------------------------
DROP TABLE IF EXISTS `goods_comment`;
CREATE TABLE `goods_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '留言用户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父留言ID（回复时使用）',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留言内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `fk_comment_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品留言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_comment
-- ----------------------------
INSERT INTO `goods_comment` VALUES (1, 4, 9, NULL, '测试留言', '2026-06-08 00:02:08');
INSERT INTO `goods_comment` VALUES (2, 4, 9, 1, '121212', '2026-06-08 00:08:56');
INSERT INTO `goods_comment` VALUES (3, 5, 9, NULL, '`1111', '2026-06-08 00:26:33');

-- ----------------------------
-- Table structure for goods_image
-- ----------------------------
DROP TABLE IF EXISTS `goods_image`;
CREATE TABLE `goods_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍥剧墖ID',
  `goods_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍥剧墖URL',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭搴忓彿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `fk_image_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍟嗗搧鍥剧墖琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_image
-- ----------------------------
INSERT INTO `goods_image` VALUES (1, 1, 'http://localhost:9000/goods-images/983add39-c7a1-4910-acce-68603413ed60.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T091122Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=dba9cc2d87e4d954347a3d39d9b385ab252b1bd3f2afb178b74036789cd5de5a', 0, '2026-06-07 17:11:38');
INSERT INTO `goods_image` VALUES (2, 2, 'http://localhost:9000/goods-images/a9224572-f573-4da2-b643-7c35bd871c68.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T091909Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=4da501d5cf1854f6d71570b1a74adf690aac78db073a73f6f4442dbc41e61e1a', 0, '2026-06-07 17:19:36');
INSERT INTO `goods_image` VALUES (3, 3, 'http://localhost:9000/goods-images/39dacca9-91ea-4631-b640-b51079a6a182.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T113157Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0806914418627d30a06bd7d790a3907b88f281f87df3586c3d3d62084d4f9327', 0, '2026-06-07 19:32:17');
INSERT INTO `goods_image` VALUES (4, 4, 'http://localhost:9000/goods-images/4bc0c924-72d4-4861-b18a-19e36ea69ee1.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T160120Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=18c1628c898de8b7893c67b85eff9e696aef75b0be966289ee8c5259cf239367', 0, '2026-06-08 00:01:27');
INSERT INTO `goods_image` VALUES (5, 5, 'http://localhost:9000/goods-images/ea02d78b-935f-4408-a560-3fe2cb5151bc.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T162549Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=d55020ac9aa655683990ca28fe1813479e45b6b04a394ed4beb4604821561892', 0, '2026-06-08 00:26:05');
INSERT INTO `goods_image` VALUES (12, 7, 'http://localhost:9000/goods-images/8b4ad560-cfff-418e-a34d-00eb344c5c42.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T054123Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=5aa5dd6f4ee46edc7684b3d2ab1297f1e20e30a9317445a6489dbb6a11ec4c4d', 0, '2026-06-08 13:41:35');
INSERT INTO `goods_image` VALUES (28, 6, 'http://localhost:9000/goods-images/7b5c5291-c291-4087-b1d9-93acdf1790be.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T163411Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=a73ffd8a352d5284e87476449159f98083c77d2f903d94913618f310180fd2e6', 0, '2026-06-08 13:49:58');
INSERT INTO `goods_image` VALUES (29, 6, 'http://localhost:9000/goods-images/d3d80418-4b7e-479d-910f-53a84f806aa4.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T163418Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=642e5c411b04de7a86929c3e90b472aaeca48f27a55c216aeec1dff4937425a8', 1, '2026-06-08 13:49:58');
INSERT INTO `goods_image` VALUES (30, 6, 'http://localhost:9000/goods-images/39c48ae3-77bb-400b-a252-e1ad44b0a207.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T163422Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=65f6bf1f4954b36d47ec10ce57636fe765f57f3ee46228efdc1efb8d88b4e1e7', 2, '2026-06-08 13:49:58');
INSERT INTO `goods_image` VALUES (31, 8, 'http://localhost:9000/goods-images/196a9364-2531-4056-aeb9-fac221876afb.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T091844Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=71a310d761328b171c6778f5814a124d79ca5b150293155a690154642b069924', 0, '2026-06-08 17:18:51');
INSERT INTO `goods_image` VALUES (32, 8, 'http://localhost:9000/goods-images/18a30ad9-eb28-4b95-a11e-db43d97817da.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T091846Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=d25462256429fcdac0df60accab1f5e8c186357328cab1ed0484cf06dc5f40d5', 1, '2026-06-08 17:18:51');
INSERT INTO `goods_image` VALUES (33, 9, 'http://localhost:9000/goods-images/53c15aa3-6ef9-4577-b407-b47238d5ff29.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T094234Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=b858d94a4d2d1b2c6ac3c5053459ac145dbfd015f74974866f6987fc3096b833', 0, '2026-06-08 17:42:39');
INSERT INTO `goods_image` VALUES (34, 10, 'http://localhost:9000/goods-images/50f24580-9a30-408a-b973-fb85e933edb5.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T095243Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f7df2f6b5512a0a052fbf0a6b2a03e7b3039803f2173cea80945e81bc252985b', 0, '2026-06-08 17:52:46');
INSERT INTO `goods_image` VALUES (36, 11, 'http://localhost:9000/goods-images/9f9af304-28d4-4414-a97e-d526f74e04de.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T102303Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=9966007b1e6334f6f8bbfbf7adc25a0e8eb389c8b17ffbea858ef2f3a1b58fcf', 0, '2026-06-08 18:24:12');
INSERT INTO `goods_image` VALUES (37, 12, 'http://localhost:9000/goods-images/c6fa0056-b205-45b5-af36-c1f866e706f4.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T103616Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0e7f5984a49e386ce96a913c15ff8f35642bdedfd8aec3b8c01de72f792ecad0', 0, '2026-06-08 18:36:22');
INSERT INTO `goods_image` VALUES (38, 13, 'http://localhost:9000/goods-images/9ff02b29-a1f6-4d31-8804-aecfab2b9965.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T104832Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=247d0ee15bf0d63983866dc8e99f0303ebc0213e39b8195f5e77c3ae9910ecef', 0, '2026-06-08 18:48:37');
INSERT INTO `goods_image` VALUES (39, 14, 'http://192.168.0.21:9000/goods-images/229a9bdb-276d-4991-988c-8a0550041fc2.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T122504Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=7dc704ca970098498d2132d890418665726eb93807186f7516f9a5a3f1e729d9', 0, '2026-06-08 20:25:08');

-- ----------------------------
-- Table structure for goods_view
-- ----------------------------
DROP TABLE IF EXISTS `goods_view`;
CREATE TABLE `goods_view`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁板綍ID',
  `goods_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '娴忚?鐢ㄦ埛ID锛堟湭鐧诲綍涓篘ULL锛',
  `view_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '娴忚?鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_view_time`(`view_time` ASC) USING BTREE,
  CONSTRAINT `fk_view_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍟嗗搧娴忚?璁板綍琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_view
-- ----------------------------

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娑堟伅ID',
  `conversation_id` bigint NOT NULL COMMENT '浼氳瘽ID',
  `sender_id` bigint NOT NULL COMMENT '鍙戦?鑰匢D',
  `receiver_id` bigint NOT NULL COMMENT '鎺ユ敹鑰匢D',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鍐呭?',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT '娑堟伅绫诲瀷: TEXT, IMAGE',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '鏄?惁宸茶?: 0=鏈??, 1=宸茶?',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戦?鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_msg_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '娑堟伅琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 1, 6, 9, '{\"type\":\"card\",\"goodsId\":2,\"title\":\"琪琪\",\"coverImage\":\"http://localhost:9000/goods-images/a9224572-f573-4da2-b643-7c35bd871c68.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T091909Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=4da501d5cf1854f6d71570b1a74adf690aac78db073a73f6f4442dbc41e61e1a\"}', 'CARD', 1, '2026-06-07 17:22:26');
INSERT INTO `message` VALUES (2, 1, 6, 9, '你好', 'TEXT', 1, '2026-06-07 17:22:40');
INSERT INTO `message` VALUES (3, 1, 9, 6, 'http://localhost:9000/chat-images/f0d9e4a7-0f4c-4a45-840f-e39f2c9fa928.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T092310Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=e90856a31c24f063d067a88b31091895fbd3423837d70693f6909889a5c18457', 'IMAGE', 1, '2026-06-07 17:23:11');
INSERT INTO `message` VALUES (4, 1, 9, 6, '1111', 'TEXT', 1, '2026-06-07 17:23:17');
INSERT INTO `message` VALUES (5, 1, 6, 9, 'sb', 'TEXT', 1, '2026-06-07 21:41:28');
INSERT INTO `message` VALUES (6, 1, 6, 9, '1111、', 'TEXT', 1, '2026-06-08 00:08:28');
INSERT INTO `message` VALUES (7, 1, 6, 9, '2121212', 'TEXT', 1, '2026-06-08 00:08:29');
INSERT INTO `message` VALUES (8, 1, 6, 9, '121212', 'TEXT', 1, '2026-06-08 00:08:30');
INSERT INTO `message` VALUES (9, 1, 9, 6, '111', 'TEXT', 1, '2026-06-08 00:08:42');
INSERT INTO `message` VALUES (10, 2, 9, 6, '{\"type\":\"card\",\"goodsId\":3,\"title\":\"哈机密\",\"coverImage\":\"http://localhost:9000/goods-images/39dacca9-91ea-4631-b640-b51079a6a182.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T113157Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0806914418627d30a06bd7d790a3907b88f281f87df3586c3d3d62084d4f9327\"}', 'CARD', 1, '2026-06-08 00:13:40');
INSERT INTO `message` VALUES (11, 2, 9, 6, '{\"type\":\"card\",\"goodsId\":3,\"title\":\"哈机密\",\"coverImage\":\"http://localhost:9000/goods-images/39dacca9-91ea-4631-b640-b51079a6a182.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T113157Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0806914418627d30a06bd7d790a3907b88f281f87df3586c3d3d62084d4f9327\"}', 'CARD', 1, '2026-06-08 00:28:48');
INSERT INTO `message` VALUES (12, 2, 9, 6, '{\"type\":\"card\",\"goodsId\":3,\"title\":\"哈机密\",\"coverImage\":\"http://localhost:9000/goods-images/39dacca9-91ea-4631-b640-b51079a6a182.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T113157Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0806914418627d30a06bd7d790a3907b88f281f87df3586c3d3d62084d4f9327\"}', 'CARD', 1, '2026-06-08 00:28:50');
INSERT INTO `message` VALUES (13, 1, 9, 6, '{\"type\":\"card\",\"goodsId\":2,\"title\":\"琪琪\",\"coverImage\":\"http://localhost:9000/goods-images/a9224572-f573-4da2-b643-7c35bd871c68.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T091909Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=4da501d5cf1854f6d71570b1a74adf690aac78db073a73f6f4442dbc41e61e1a\"}', 'CARD', 1, '2026-06-08 00:28:54');
INSERT INTO `message` VALUES (14, 1, 9, 6, '{\"type\":\"card\",\"goodsId\":2,\"title\":\"琪琪\",\"coverImage\":\"http://localhost:9000/goods-images/a9224572-f573-4da2-b643-7c35bd871c68.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T091909Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=4da501d5cf1854f6d71570b1a74adf690aac78db073a73f6f4442dbc41e61e1a\"}', 'CARD', 1, '2026-06-08 18:19:51');
INSERT INTO `message` VALUES (15, 3, 6, 9, '{\"type\":\"card\",\"goodsId\":12,\"title\":\"测新发送\",\"coverImage\":\"http://localhost:9000/goods-images/c6fa0056-b205-45b5-af36-c1f866e706f4.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T103616Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=0e7f5984a49e386ce96a913c15ff8f35642bdedfd8aec3b8c01de72f792ecad0\"}', 'CARD', 1, '2026-06-08 20:14:35');
INSERT INTO `message` VALUES (16, 3, 6, 9, 'http://192.168.0.21:9000/chat-images/23a28c22-73d3-452c-8cd0-d72da9c96e87.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T121540Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=17c3b14aff4aa0f23ce3aa5a3bd152f532936aebff259926c16885ccbf234647', 'IMAGE', 1, '2026-06-08 20:15:41');
INSERT INTO `message` VALUES (17, 3, 9, 6, 'xingb', 'TEXT', 1, '2026-06-08 20:16:01');
INSERT INTO `message` VALUES (18, 4, 6, 9, '{\"type\":\"card\",\"goodsId\":5,\"title\":\"nas\",\"coverImage\":\"http://localhost:9000/goods-images/ea02d78b-935f-4408-a560-3fe2cb5151bc.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260607%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260607T162549Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=d55020ac9aa655683990ca28fe1813479e45b6b04a394ed4beb4604821561892\"}', 'CARD', 1, '2026-06-08 20:16:32');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '閫氱煡ID',
  `user_id` bigint NOT NULL COMMENT '鎺ユ敹鐢ㄦ埛ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫氱煡绫诲瀷: SYSTEM, ORDER, REVIEW',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫氱煡鏍囬?',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '閫氱煡鍐呭?',
  `related_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈涓氬姟ID',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '鏄?惁宸茶?: 0=鏈??, 1=宸茶?',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '閫氱煡琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 9, 'REVIEW', '商品审核通过', '您的商品「琪琪」已通过审核', 2, 1, '2026-06-07 17:21:02');
INSERT INTO `notification` VALUES (2, 6, 'REVIEW', '商品审核通过', '您的商品「马家」已通过审核', 1, 1, '2026-06-07 17:21:06');
INSERT INTO `notification` VALUES (3, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「琪琪」', 1, 1, '2026-06-07 17:24:11');
INSERT INTO `notification` VALUES (4, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「琪琪」', 1, 1, '2026-06-07 17:24:30');
INSERT INTO `notification` VALUES (5, 9, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 1, 1, '2026-06-07 17:24:49');
INSERT INTO `notification` VALUES (6, 9, 'REVIEW', '收到新评价', '张三同学给您打出了2星评价', 1, 1, '2026-06-07 17:25:14');
INSERT INTO `notification` VALUES (7, 6, 'REVIEW', '收到新评价', '曼波给您打出了2星评价', 2, 1, '2026-06-07 17:25:52');
INSERT INTO `notification` VALUES (8, 6, 'REVIEW', '商品审核通过', '您的商品「哈机密」已通过审核', 3, 1, '2026-06-07 19:33:02');
INSERT INTO `notification` VALUES (9, 6, 'SYSTEM', '测试公告功能', '测试公告', 1, 1, '2026-06-07 23:33:43');
INSERT INTO `notification` VALUES (10, 7, 'SYSTEM', '测试公告功能', '测试公告', 1, 0, '2026-06-07 23:33:43');
INSERT INTO `notification` VALUES (11, 8, 'SYSTEM', '测试公告功能', '测试公告', 1, 0, '2026-06-07 23:33:43');
INSERT INTO `notification` VALUES (12, 9, 'SYSTEM', '测试公告功能', '测试公告', 1, 1, '2026-06-07 23:33:43');
INSERT INTO `notification` VALUES (13, 6, 'SYSTEM', '1212121', '1212121212121212', 2, 1, '2026-06-07 23:36:28');
INSERT INTO `notification` VALUES (14, 7, 'SYSTEM', '1212121', '1212121212121212', 2, 0, '2026-06-07 23:36:28');
INSERT INTO `notification` VALUES (15, 8, 'SYSTEM', '1212121', '1212121212121212', 2, 0, '2026-06-07 23:36:28');
INSERT INTO `notification` VALUES (16, 9, 'SYSTEM', '1212121', '1212121212121212', 2, 1, '2026-06-07 23:36:28');
INSERT INTO `notification` VALUES (17, 9, 'REVIEW', '商品审核通过', '您的商品「1111」已通过审核', 4, 1, '2026-06-08 00:01:44');
INSERT INTO `notification` VALUES (18, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「1111」', 2, 1, '2026-06-08 00:09:22');
INSERT INTO `notification` VALUES (19, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「1111」', 2, 1, '2026-06-08 00:09:40');
INSERT INTO `notification` VALUES (20, 9, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 2, 1, '2026-06-08 00:09:59');
INSERT INTO `notification` VALUES (21, 9, 'REVIEW', '收到新评价', '张三同学给您打出了5星评价', 3, 1, '2026-06-08 00:10:02');
INSERT INTO `notification` VALUES (22, 6, 'REVIEW', '收到新评价', '曼波给您打出了5星评价', 4, 1, '2026-06-08 00:18:25');
INSERT INTO `notification` VALUES (23, 9, 'REVIEW', '商品审核通过', '您的商品「12121」已通过审核', 6, 1, '2026-06-08 00:34:40');
INSERT INTO `notification` VALUES (24, 9, 'REVIEW', '商品审核通过', '您的商品「nas」已通过审核', 5, 1, '2026-06-08 00:34:51');
INSERT INTO `notification` VALUES (25, 9, 'REVIEW', '您的商品已被下架', '商品「12121」因被举报违规已被下架', 6, 1, '2026-06-08 13:24:39');
INSERT INTO `notification` VALUES (26, 6, 'REVIEW', '举报处理结果', '您提交的举报已处理，结果：举报成立', 1, 1, '2026-06-08 13:24:39');
INSERT INTO `notification` VALUES (27, 9, 'REVIEW', '商品审核通过', '您的商品「测试」已通过审核', 7, 1, '2026-06-08 13:41:43');
INSERT INTO `notification` VALUES (28, 9, 'REVIEW', '商品审核未通过', '您的商品「12121」未通过审核，原因：草泥马\n', 6, 1, '2026-06-08 13:45:56');
INSERT INTO `notification` VALUES (29, 9, 'REVIEW', '商品审核通过', '您的商品「12121」已通过审核', 6, 1, '2026-06-08 13:50:22');
INSERT INTO `notification` VALUES (30, 6, 'ORDER', '您有新的订单待确认', '买家已下单购买「哈机密」', 3, 1, '2026-06-08 14:02:04');
INSERT INTO `notification` VALUES (31, 9, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「哈机密」', 3, 1, '2026-06-08 14:03:35');
INSERT INTO `notification` VALUES (32, 6, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 3, 1, '2026-06-08 14:03:59');
INSERT INTO `notification` VALUES (33, 9, 'REVIEW', '收到新评价', '张三同学给您打出了5星评价', 5, 1, '2026-06-08 14:04:08');
INSERT INTO `notification` VALUES (34, 6, 'REVIEW', '收到新评价', '曼波给您打出了5星评价', 6, 1, '2026-06-08 14:04:17');
INSERT INTO `notification` VALUES (35, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「12121」', 4, 1, '2026-06-08 14:20:03');
INSERT INTO `notification` VALUES (36, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「12121」', 4, 1, '2026-06-08 14:23:02');
INSERT INTO `notification` VALUES (37, 9, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 4, 1, '2026-06-08 17:18:15');
INSERT INTO `notification` VALUES (38, 9, 'REVIEW', '收到新评价', '张三同学给您打出了5星评价', 7, 1, '2026-06-08 17:18:24');
INSERT INTO `notification` VALUES (39, 6, 'REVIEW', '商品审核通过', '您的商品「测试」已通过审核', 8, 1, '2026-06-08 17:19:17');
INSERT INTO `notification` VALUES (40, 6, 'ORDER', '您有新的订单待确认', '买家已下单购买「测试」', 5, 1, '2026-06-08 17:19:47');
INSERT INTO `notification` VALUES (41, 6, 'REVIEW', '收到新评价', '曼波给您打出了5星评价', 8, 1, '2026-06-08 17:20:03');
INSERT INTO `notification` VALUES (42, 9, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「测试」', 5, 1, '2026-06-08 17:20:47');
INSERT INTO `notification` VALUES (43, 9, 'ORDER', '卖家申请取消交易', '卖家申请取消订单，原因：不想要了', 5, 1, '2026-06-08 17:20:58');
INSERT INTO `notification` VALUES (44, 6, 'ORDER', '取消申请被拒绝', '买家拒绝了取消订单', 5, 1, '2026-06-08 17:21:16');
INSERT INTO `notification` VALUES (45, 6, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 5, 1, '2026-06-08 17:41:37');
INSERT INTO `notification` VALUES (46, 6, 'REVIEW', '商品审核通过', '您的商品「张三的东西」已通过审核', 9, 1, '2026-06-08 17:42:45');
INSERT INTO `notification` VALUES (47, 6, 'ORDER', '您有新的订单待确认', '买家已下单购买「张三的东西」', 6, 1, '2026-06-08 17:43:09');
INSERT INTO `notification` VALUES (48, 9, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「张三的东西」', 6, 1, '2026-06-08 17:43:21');
INSERT INTO `notification` VALUES (49, 6, 'ORDER', '买家申请取消交易', '买家申请取消订单，原因：12121', 6, 1, '2026-06-08 17:43:36');
INSERT INTO `notification` VALUES (50, 9, 'ORDER', '取消申请被拒绝', '卖家拒绝了取消订单', 6, 1, '2026-06-08 17:45:49');
INSERT INTO `notification` VALUES (51, 6, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 6, 1, '2026-06-08 17:52:01');
INSERT INTO `notification` VALUES (52, 9, 'REVIEW', '商品审核通过', '您的商品「曼波的东西」已通过审核', 10, 1, '2026-06-08 17:52:52');
INSERT INTO `notification` VALUES (53, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「曼波的东西」', 7, 1, '2026-06-08 17:53:01');
INSERT INTO `notification` VALUES (54, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「曼波的东西」', 7, 1, '2026-06-08 17:53:12');
INSERT INTO `notification` VALUES (55, 9, 'ORDER', '买家申请取消交易', '买家申请取消订单，原因：1111', 7, 1, '2026-06-08 17:53:22');
INSERT INTO `notification` VALUES (56, 6, 'ORDER', '取消申请被拒绝', '卖家拒绝了取消订单', 7, 1, '2026-06-08 17:53:28');
INSERT INTO `notification` VALUES (57, 9, 'ORDER', '买家申请取消交易', '买家申请取消订单，原因：121212', 7, 1, '2026-06-08 17:53:38');
INSERT INTO `notification` VALUES (58, 6, 'ORDER', '取消申请被拒绝', '卖家拒绝了取消订单', 7, 1, '2026-06-08 17:53:52');
INSERT INTO `notification` VALUES (59, 6, 'ORDER', '对方申请了仲裁', '曼波 申请管理员介入仲裁，原因：12121', 7, 1, '2026-06-08 17:59:39');
INSERT INTO `notification` VALUES (60, 6, 'ORDER', '仲裁结果：恢复交易', '管理员已驳回取消申请，交易继续', 7, 1, '2026-06-08 18:01:47');
INSERT INTO `notification` VALUES (61, 9, 'ORDER', '仲裁结果：恢复交易', '管理员已驳回取消申请，交易继续', 7, 1, '2026-06-08 18:01:47');
INSERT INTO `notification` VALUES (62, 6, 'ORDER', '对方申请了仲裁', '曼波 申请管理员介入仲裁，原因：121212', 7, 1, '2026-06-08 18:02:00');
INSERT INTO `notification` VALUES (63, 6, 'ORDER', '仲裁结果：取消交易', '管理员已判定取消订单，121212', 7, 1, '2026-06-08 18:02:31');
INSERT INTO `notification` VALUES (64, 9, 'ORDER', '仲裁结果：取消交易', '管理员已判定取消订单，121212', 7, 1, '2026-06-08 18:02:31');
INSERT INTO `notification` VALUES (65, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「曼波的东西」', 8, 1, '2026-06-08 18:04:34');
INSERT INTO `notification` VALUES (66, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「曼波的东西」', 8, 1, '2026-06-08 18:04:40');
INSERT INTO `notification` VALUES (67, 6, 'ORDER', '卖家申请取消交易', '卖家申请取消订单，原因：121212', 8, 1, '2026-06-08 18:05:01');
INSERT INTO `notification` VALUES (68, 9, 'ORDER', '取消申请被拒绝', '买家拒绝了取消订单', 8, 1, '2026-06-08 18:05:07');
INSERT INTO `notification` VALUES (69, 6, 'ORDER', '对方申请了仲裁', '曼波 申请管理员介入仲裁，原因：121212', 8, 1, '2026-06-08 18:05:15');
INSERT INTO `notification` VALUES (70, 6, 'ORDER', '仲裁结果：取消交易', '管理员已判定取消订单，1212121', 8, 1, '2026-06-08 18:05:24');
INSERT INTO `notification` VALUES (71, 9, 'ORDER', '仲裁结果：取消交易', '管理员已判定取消订单，1212121', 8, 1, '2026-06-08 18:05:24');
INSERT INTO `notification` VALUES (72, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「曼波的东西」', 9, 1, '2026-06-08 18:09:34');
INSERT INTO `notification` VALUES (73, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「曼波的东西」', 9, 1, '2026-06-08 18:09:40');
INSERT INTO `notification` VALUES (74, 6, 'ORDER', '卖家申请取消交易', '卖家申请取消订单，原因：121', 9, 1, '2026-06-08 18:09:51');
INSERT INTO `notification` VALUES (75, 9, 'ORDER', '取消申请被拒绝', '买家拒绝了取消订单', 9, 1, '2026-06-08 18:09:58');
INSERT INTO `notification` VALUES (76, 6, 'ORDER', '对方申请了仲裁', '曼波 申请管理员介入仲裁，原因：1111', 9, 1, '2026-06-08 18:10:10');
INSERT INTO `notification` VALUES (77, 6, 'ORDER', '仲裁结果：取消交易', '管理员已判定取消订单，121212', 9, 1, '2026-06-08 18:10:21');
INSERT INTO `notification` VALUES (78, 9, 'ORDER', '仲裁结果：取消交易', '管理员已判定取消订单，121212', 9, 1, '2026-06-08 18:10:21');
INSERT INTO `notification` VALUES (79, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「曼波的东西」', 10, 1, '2026-06-08 18:15:43');
INSERT INTO `notification` VALUES (80, 6, 'ORDER', '您的订单已被卖家确认', '卖家已确认订单「曼波的东西」', 10, 1, '2026-06-08 18:15:57');
INSERT INTO `notification` VALUES (81, 9, 'ORDER', '订单已完成', '订单已完成，请及时评价买家', 10, 1, '2026-06-08 18:16:13');
INSERT INTO `notification` VALUES (82, 9, 'REVIEW', '收到新评价', '张三同学给您打出了3星评价', 9, 1, '2026-06-08 18:18:30');
INSERT INTO `notification` VALUES (83, 6, 'REVIEW', '收到新评价', '曼波给您打出了2星评价', 10, 1, '2026-06-08 18:18:43');
INSERT INTO `notification` VALUES (84, 9, 'ORDER', '您有新的订单待确认', '买家已下单购买「测试」', 11, 1, '2026-06-08 18:21:17');
INSERT INTO `notification` VALUES (85, 6, 'ORDER', '订单已取消', '卖家已拒绝订单，商品已重新上架', 11, 1, '2026-06-08 18:21:57');
INSERT INTO `notification` VALUES (86, 6, 'REVIEW', '商品审核通过', '您的商品「测试置换」已通过审核', 11, 1, '2026-06-08 18:23:25');
INSERT INTO `notification` VALUES (87, 6, 'REVIEW', '商品审核通过', '您的商品「测试置换」已通过审核', 11, 1, '2026-06-08 18:24:23');
INSERT INTO `notification` VALUES (88, 6, 'ORDER', '您有新的订单待确认', '买家已下单购买「测试置换」', 12, 1, '2026-06-08 18:35:32');
INSERT INTO `notification` VALUES (89, 9, 'ORDER', '订单已取消', '卖家已拒绝订单，商品已重新上架', 12, 1, '2026-06-08 18:35:46');
INSERT INTO `notification` VALUES (90, 9, 'REVIEW', '商品审核通过', '您的商品「测新发送」已通过审核', 12, 1, '2026-06-08 18:36:38');
INSERT INTO `notification` VALUES (91, 6, 'REVIEW', '商品审核通过', '您的商品「21212」已通过审核', 13, 1, '2026-06-08 18:48:46');
INSERT INTO `notification` VALUES (92, 6, 'SYSTEM', '新粉丝', '曼波 关注了你', NULL, 1, '2026-06-08 19:13:52');
INSERT INTO `notification` VALUES (93, 6, 'SYSTEM', '32132', '3213123123123123123123123123123', 3, 1, '2026-06-08 20:21:37');
INSERT INTO `notification` VALUES (94, 7, 'SYSTEM', '32132', '3213123123123123123123123123123', 3, 0, '2026-06-08 20:21:37');
INSERT INTO `notification` VALUES (95, 8, 'SYSTEM', '32132', '3213123123123123123123123123123', 3, 0, '2026-06-08 20:21:37');
INSERT INTO `notification` VALUES (96, 9, 'SYSTEM', '32132', '3213123123123123123123123123123', 3, 1, '2026-06-08 20:21:37');
INSERT INTO `notification` VALUES (97, 6, 'REVIEW', '商品审核通过', '您的商品「12121212」已通过审核', 14, 1, '2026-06-08 20:27:10');
INSERT INTO `notification` VALUES (98, 9, 'SYSTEM', '账号已被禁用', '您的账号已被管理员禁用，如有疑问请联系管理员', NULL, 1, '2026-06-08 20:27:17');
INSERT INTO `notification` VALUES (99, 9, 'SYSTEM', '账号已被禁用', '您的账号已被管理员禁用，如有疑问请联系管理员', NULL, 1, '2026-06-08 20:36:48');
INSERT INTO `notification` VALUES (100, 9, 'SYSTEM', '账号已被禁用', '您的账号已被管理员禁用，如有疑问请联系管理员', NULL, 1, '2026-06-08 20:37:34');

-- ----------------------------
-- Table structure for order_dispute
-- ----------------------------
DROP TABLE IF EXISTS `order_dispute`;
CREATE TABLE `order_dispute`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '争议ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '申请原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '处理备注',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单争议表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_dispute
-- ----------------------------
INSERT INTO `order_dispute` VALUES (1, 7, 9, '12121', 'REJECTED', 5, '不太合适吧', '2026-06-08 18:01:47', '2026-06-08 17:59:39', '2026-06-08 17:59:39', 0);
INSERT INTO `order_dispute` VALUES (2, 7, 9, '121212', 'RESOLVED', 5, '121212', '2026-06-08 18:02:31', '2026-06-08 18:02:00', '2026-06-08 18:02:00', 0);
INSERT INTO `order_dispute` VALUES (3, 8, 9, '121212', 'RESOLVED', 5, '1212121', '2026-06-08 18:05:24', '2026-06-08 18:05:15', '2026-06-08 18:05:15', 0);
INSERT INTO `order_dispute` VALUES (4, 9, 9, '1111', 'RESOLVED', 5, '121212', '2026-06-08 18:10:21', '2026-06-08 18:10:10', '2026-06-08 18:10:10', 0);

-- ----------------------------
-- Table structure for order_log
-- ----------------------------
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏃ュ織ID',
  `order_id` bigint NOT NULL COMMENT '璁㈠崟ID',
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎿嶄綔绫诲瀷: CREATE, CONFIRM, CANCEL, COMPLETE',
  `operator_id` bigint NOT NULL COMMENT '鎿嶄綔浜篒D',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '澶囨敞',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '璁㈠崟鎿嶄綔鏃ュ織琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_log
-- ----------------------------
INSERT INTO `order_log` VALUES (1, 1, 'CREATE', 6, '买家创建订单', '2026-06-07 17:24:10');
INSERT INTO `order_log` VALUES (2, 1, 'CONFIRM', 9, '卖家确认订单', '2026-06-07 17:24:30');
INSERT INTO `order_log` VALUES (3, 1, 'COMPLETE', 6, '买家确认完成', '2026-06-07 17:24:49');
INSERT INTO `order_log` VALUES (4, 2, 'CREATE', 6, '买家创建订单', '2026-06-08 00:09:21');
INSERT INTO `order_log` VALUES (5, 2, 'CONFIRM', 9, '卖家确认订单', '2026-06-08 00:09:40');
INSERT INTO `order_log` VALUES (6, 2, 'COMPLETE', 6, '买家确认完成', '2026-06-08 00:09:58');
INSERT INTO `order_log` VALUES (7, 3, 'CREATE', 9, '买家发起置换', '2026-06-08 14:02:03');
INSERT INTO `order_log` VALUES (8, 3, 'CONFIRM', 6, '卖家确认订单', '2026-06-08 14:03:34');
INSERT INTO `order_log` VALUES (9, 3, 'COMPLETE', 9, '买家确认完成', '2026-06-08 14:03:59');
INSERT INTO `order_log` VALUES (10, 4, 'CREATE', 6, '买家创建订单', '2026-06-08 14:20:03');
INSERT INTO `order_log` VALUES (11, 4, 'CONFIRM', 9, '卖家确认订单', '2026-06-08 14:23:02');
INSERT INTO `order_log` VALUES (12, 4, 'COMPLETE', 6, '买家确认完成', '2026-06-08 17:18:15');
INSERT INTO `order_log` VALUES (13, 5, 'CREATE', 9, '买家创建订单', '2026-06-08 17:19:46');
INSERT INTO `order_log` VALUES (14, 5, 'CONFIRM', 6, '卖家确认订单', '2026-06-08 17:20:46');
INSERT INTO `order_log` VALUES (15, 5, 'CANCEL_REQUEST', 6, '卖家申请取消：不想要了', '2026-06-08 17:20:57');
INSERT INTO `order_log` VALUES (16, 5, 'CANCEL_REJECT', 9, '买家拒绝取消', '2026-06-08 17:21:15');
INSERT INTO `order_log` VALUES (17, 5, 'COMPLETE', 9, '买家确认完成', '2026-06-08 17:41:37');
INSERT INTO `order_log` VALUES (18, 6, 'CREATE', 9, '买家创建订单', '2026-06-08 17:43:09');
INSERT INTO `order_log` VALUES (19, 6, 'CONFIRM', 6, '卖家确认订单', '2026-06-08 17:43:20');
INSERT INTO `order_log` VALUES (20, 6, 'CANCEL_REQUEST', 9, '买家申请取消：12121', '2026-06-08 17:43:36');
INSERT INTO `order_log` VALUES (24, 6, 'CANCEL_REJECT', 6, '卖家拒绝取消', '2026-06-08 17:45:49');
INSERT INTO `order_log` VALUES (25, 6, 'COMPLETE', 9, '买家确认完成', '2026-06-08 17:52:01');
INSERT INTO `order_log` VALUES (26, 7, 'CREATE', 6, '买家创建订单', '2026-06-08 17:53:01');
INSERT INTO `order_log` VALUES (27, 7, 'CONFIRM', 9, '卖家确认订单', '2026-06-08 17:53:12');
INSERT INTO `order_log` VALUES (28, 7, 'CANCEL_REQUEST', 6, '买家申请取消：1111', '2026-06-08 17:53:21');
INSERT INTO `order_log` VALUES (29, 7, 'CANCEL_REJECT', 9, '卖家拒绝取消', '2026-06-08 17:53:27');
INSERT INTO `order_log` VALUES (30, 7, 'CANCEL_REQUEST', 6, '买家申请取消：121212', '2026-06-08 17:53:37');
INSERT INTO `order_log` VALUES (31, 7, 'CANCEL_REJECT', 9, '卖家拒绝取消', '2026-06-08 17:53:51');
INSERT INTO `order_log` VALUES (32, 8, 'CREATE', 6, '买家创建订单', '2026-06-08 18:04:33');
INSERT INTO `order_log` VALUES (33, 8, 'CONFIRM', 9, '卖家确认订单', '2026-06-08 18:04:40');
INSERT INTO `order_log` VALUES (34, 8, 'CANCEL_REQUEST', 9, '卖家申请取消：121212', '2026-06-08 18:05:01');
INSERT INTO `order_log` VALUES (35, 8, 'CANCEL_REJECT', 6, '买家拒绝取消', '2026-06-08 18:05:07');
INSERT INTO `order_log` VALUES (36, 9, 'CREATE', 6, '买家创建订单', '2026-06-08 18:09:34');
INSERT INTO `order_log` VALUES (37, 9, 'CONFIRM', 9, '卖家确认订单', '2026-06-08 18:09:40');
INSERT INTO `order_log` VALUES (38, 9, 'CANCEL_REQUEST', 9, '卖家申请取消：121', '2026-06-08 18:09:51');
INSERT INTO `order_log` VALUES (39, 9, 'CANCEL_REJECT', 6, '买家拒绝取消', '2026-06-08 18:09:58');
INSERT INTO `order_log` VALUES (40, 9, 'DISPUTE_RESOLVED', 5, '管理员仲裁判取消：121212', '2026-06-08 18:10:21');
INSERT INTO `order_log` VALUES (41, 10, 'CREATE', 6, '买家创建订单', '2026-06-08 18:15:43');
INSERT INTO `order_log` VALUES (42, 10, 'CONFIRM', 9, '卖家确认订单', '2026-06-08 18:15:57');
INSERT INTO `order_log` VALUES (43, 10, 'COMPLETE', 6, '买家确认完成', '2026-06-08 18:16:12');
INSERT INTO `order_log` VALUES (44, 11, 'CREATE', 6, '买家发起置换', '2026-06-08 18:21:16');
INSERT INTO `order_log` VALUES (45, 11, 'CANCEL', 9, '卖家拒绝订单', '2026-06-08 18:21:56');
INSERT INTO `order_log` VALUES (46, 12, 'CREATE', 9, '买家发起置换', '2026-06-08 18:35:31');
INSERT INTO `order_log` VALUES (47, 12, 'CANCEL', 6, '卖家拒绝订单', '2026-06-08 18:35:46');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁㈠崟ID',
  `goods_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `exchange_goods_id` bigint NULL DEFAULT NULL COMMENT '置换时对方商品ID',
  `buyer_id` bigint NOT NULL COMMENT '涔板?ID',
  `seller_id` bigint NOT NULL COMMENT '鍗栧?ID',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '璁㈠崟鐘舵?: PENDING, IN_PROGRESS, COMPLETED, CANCELLED',
  `amount` decimal(10, 2) NOT NULL COMMENT '浜ゆ槗閲戦?',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '涔板?澶囨敞',
  `meet_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '约定交易时间',
  `meet_place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '约定交易地点',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '取消原因',
  `cancel_requester_id` bigint NULL DEFAULT NULL COMMENT '取消申请人ID',
  `cancel_request_time` datetime NULL DEFAULT NULL COMMENT '取消申请时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '璁㈠崟琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 2, NULL, 6, 9, 'COMPLETED', 2.00, '1', '12', '12', '2026-06-07 17:24:11', '2026-06-07 17:24:11', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (2, 4, NULL, 6, 9, 'COMPLETED', 2121.00, '121212121212', '1212121', '121212', '2026-06-08 00:09:22', '2026-06-08 00:09:22', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (3, 3, NULL, 9, 6, 'COMPLETED', 0.00, '11111', '1111', '1111', '2026-06-08 14:02:04', '2026-06-08 14:02:04', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (4, 6, NULL, 6, 9, 'COMPLETED', 12121.00, '等我等我等我的', '对哇对哇', '对我的我的', '2026-06-08 14:20:03', '2026-06-08 14:20:03', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (5, 8, NULL, 9, 6, 'COMPLETED', 1111.00, '334', '11', '222', '2026-06-08 17:19:47', '2026-06-08 17:19:47', 0, '', 6, '2026-06-08 17:20:58');
INSERT INTO `orders` VALUES (6, 9, NULL, 9, 6, 'COMPLETED', 112.00, '12121212', '1212', '211212', '2026-06-08 17:43:09', '2026-06-08 17:43:09', 0, '', 9, '2026-06-08 17:43:36');
INSERT INTO `orders` VALUES (7, 10, NULL, 6, 9, 'CANCELLED', 1212.00, '12121', '2121', '2121', '2026-06-08 17:53:01', '2026-06-08 17:53:51', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (8, 10, NULL, 6, 9, 'CANCELLED', 1212.00, '21212121', '12121', '12121', '2026-06-08 18:04:34', '2026-06-08 18:05:07', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (9, 10, NULL, 6, 9, 'CANCELLED', 1212.00, '1212121', '1212', '12121', '2026-06-08 18:09:34', '2026-06-08 18:09:58', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (10, 10, NULL, 6, 9, 'COMPLETED', 1212.00, '1212', '12121', '12121', '2026-06-08 18:15:43', '2026-06-08 18:15:43', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (11, 7, NULL, 6, 9, 'CANCELLED', 0.00, '12121', '1212', '1212', '2026-06-08 18:21:17', '2026-06-08 18:21:17', 0, '', NULL, NULL);
INSERT INTO `orders` VALUES (12, 11, 7, 9, 6, 'CANCELLED', 111.00, '1212', '112', '212', '2026-06-08 18:35:32', '2026-06-08 18:35:32', 0, '', NULL, NULL);

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓炬姤ID',
  `reporter_id` bigint NOT NULL COMMENT '涓炬姤浜篒D',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '涓炬姤绫诲瀷: GOODS, USER',
  `target_id` bigint NOT NULL COMMENT '琚?妇鎶ュ?璞?D锛堝晢鍝両D鎴栫敤鎴稩D锛',
  `reason` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '涓炬姤鍘熷洜: FALSE_INFO, FRAUD, AD, VIOLATION',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '涓炬姤鎻忚堪',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '澶勭悊鐘舵?: PENDING, APPROVED, REJECTED',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '澶勭悊浜篒D',
  `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '澶勭悊澶囨敞',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '澶勭悊鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_type_target`(`type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_handler_id`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '涓炬姤琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------
INSERT INTO `report` VALUES (1, 6, 'GOODS', 6, 'FALSE_INFO', '这明明是假的\n', 'APPROVED', 5, '下架', '2026-06-08 13:24:39', '2026-06-08 13:18:12', '2026-06-08 13:18:12', 0);

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇勪环ID',
  `order_id` bigint NOT NULL COMMENT '璁㈠崟ID',
  `reviewer_id` bigint NOT NULL COMMENT '璇勪环浜篒D',
  `reviewee_id` bigint NOT NULL COMMENT '琚?瘎浠蜂汉ID',
  `rating` tinyint NOT NULL COMMENT '璇勫垎 (1-5)',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '璇勪环鍐呭?',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_reviewer`(`order_id` ASC, `reviewer_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_reviewer_id`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_reviewee_id`(`reviewee_id` ASC) USING BTREE,
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_reviewee` FOREIGN KEY (`reviewee_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '璇勪环琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review
-- ----------------------------
INSERT INTO `review` VALUES (1, 1, 6, 9, 2, '太差了\n', '2026-06-07 17:25:14', '2026-06-07 17:25:14', 0);
INSERT INTO `review` VALUES (2, 1, 9, 6, 2, '垃圾', '2026-06-07 17:25:52', '2026-06-07 17:25:52', 0);
INSERT INTO `review` VALUES (3, 2, 6, 9, 5, '12121212', '2026-06-08 00:10:02', '2026-06-08 00:10:02', 0);
INSERT INTO `review` VALUES (4, 2, 9, 6, 5, '1111', '2026-06-08 00:18:25', '2026-06-08 00:18:25', 0);
INSERT INTO `review` VALUES (5, 3, 6, 9, 5, '121212', '2026-06-08 14:04:08', '2026-06-08 14:04:08', 0);
INSERT INTO `review` VALUES (6, 3, 9, 6, 5, '2121212', '2026-06-08 14:04:17', '2026-06-08 14:04:17', 0);
INSERT INTO `review` VALUES (7, 4, 6, 9, 5, '1111', '2026-06-08 17:18:24', '2026-06-08 17:18:24', 0);
INSERT INTO `review` VALUES (8, 4, 9, 6, 5, '1212121', '2026-06-08 17:20:03', '2026-06-08 17:20:03', 0);
INSERT INTO `review` VALUES (9, 10, 6, 9, 3, '一般', '2026-06-08 18:18:30', '2026-06-08 18:18:30', 0);
INSERT INTO `review` VALUES (10, 10, 9, 6, 2, '你更一般', '2026-06-08 18:18:43', '2026-06-08 18:18:43', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐢ㄦ埛鍚',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵嗙爜锛圔Crypt鍔犲瘑锛',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鏄电О',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '澶村儚URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鎵嬫満鍙',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '閭??',
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '瀛︽牎',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '瑙掕壊: USER, ADMIN',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '鐘舵?: ACTIVE, DISABLED',
  `credit_score` decimal(2, 1) NOT NULL DEFAULT 5.0 COMMENT '淇＄敤璇勫垎 (1.0-5.0)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鐪熷疄濮撳悕',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '瀛﹀彿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎: 0=姝ｅ父, 1=宸插垹闄',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_nickname`(`nickname` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鐢ㄦ埛琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (5, 'admin', '$2b$10$8korOLb1VQiK7M5Lpct9jeQZJduLxPv1UmZUL67/vEIrtz0sjzoFa', '系统管理员', '', '', '', '校园交换平台', 'ADMIN', 'ACTIVE', 5.0, '', '', '2026-06-03 09:24:27', '2026-06-03 09:24:27', 0);
INSERT INTO `user` VALUES (6, 'zhangsan', '$2b$10$8korOLb1VQiK7M5Lpct9jeK3T5M6Q0pMyCnw.z1/mWH7jpfCoAiGW', '张三同学', 'http://192.168.0.21:9000/avatars/62cec884-8f2b-46fb-977d-d1f21409adae.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T131830Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f5e99e87e9453c8b5c103b419938925163c3ef13adf18b51458f463c67505c20', '15893232332', 'xingsjdd@1212.com', '西安邮电大学', 'USER', 'ACTIVE', 3.8, '张三', '', '2026-06-03 09:24:27', '2026-06-03 09:24:27', 0);
INSERT INTO `user` VALUES (7, 'lisi', '$2b$10$8korOLb1VQiK7M5Lpct9jeK3T5M6Q0pMyCnw.z1/mWH7jpfCoAiGW', '李四学姐', '', '', '', '经管学院', 'USER', 'ACTIVE', 5.0, '李四', '', '2026-06-03 09:24:27', '2026-06-03 09:24:27', 0);
INSERT INTO `user` VALUES (8, 'wangwu', '$2b$10$8korOLb1VQiK7M5Lpct9jeK3T5M6Q0pMyCnw.z1/mWH7jpfCoAiGW', '王五学长', '', '', '', '电子信息学院', 'USER', 'ACTIVE', 4.2, '王五', '', '2026-06-03 09:24:27', '2026-06-03 09:24:27', 0);
INSERT INTO `user` VALUES (9, 'manbo', '$2a$10$JcaLQZYQXbaWDUJMlI4BhOaVE/M.dmBsvkWor/5Cg.Km2wxbOZ4VO', '曼波', 'http://192.168.0.21:9000/avatars/9f027d5d-35b2-4b6e-a70f-23268d92aed7.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20260608%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260608T114200Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f0906a343926dc06a72e27029f39dae7fb43e26946a70451162853bb7fc9a79c', '', '', '', 'USER', 'ACTIVE', 4.0, '', '', '2026-06-03 17:49:19', '2026-06-03 17:49:19', 0);

-- ----------------------------
-- Table structure for user_auth
-- ----------------------------
DROP TABLE IF EXISTS `user_auth`;
CREATE TABLE `user_auth`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁よ瘉ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐪熷疄濮撳悕',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀛﹀彿',
  `id_card_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀛︾敓璇?韬?唤璇佺収鐗嘦RL',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '瀹℃牳鐘舵?: PENDING, APPROVED, REJECTED',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鎷掔粷鍘熷洜',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鐢宠?鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_user_auth_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '瀹炲悕璁よ瘉琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_auth
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
