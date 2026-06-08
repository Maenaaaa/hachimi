-- ============================================================
-- Campus Exchange Platform — Complete Database Schema
-- MySQL 8.0+
-- Character set: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_exchange
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_exchange;

-- ============================================================
-- 1. user — 用户表
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`      VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname`      VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar`        VARCHAR(500) NOT NULL DEFAULT '' COMMENT '头像URL',
  `phone`         VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '手机号',
  `email`         VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
  `school`        VARCHAR(100) NOT NULL DEFAULT '' COMMENT '学校',
  `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
  `status`        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, DISABLED',
  `credit_score`  DECIMAL(2,1) NOT NULL DEFAULT 5.0 COMMENT '信用评分 (1.0-5.0)',
  `real_name`     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '真实姓名',
  `student_id`    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '学号',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_username` (`username`),
  INDEX `idx_nickname` (`nickname`),
  INDEX `idx_status` (`status`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. user_auth — 实名认证表
-- ============================================================
DROP TABLE IF EXISTS `user_auth`;
CREATE TABLE `user_auth` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
  `real_name`      VARCHAR(50)  NOT NULL COMMENT '真实姓名',
  `student_id`     VARCHAR(50)  NOT NULL COMMENT '学号',
  `id_card_image`  VARCHAR(500) NOT NULL COMMENT '学生证/身份证照片URL',
  `status`         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态: PENDING, APPROVED, REJECTED',
  `reject_reason`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT '拒绝原因',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_user_auth_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实名认证表';

-- ============================================================
-- 3. category — 商品分类表
-- ============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `icon`        VARCHAR(100) NOT NULL DEFAULT '' COMMENT '分类图标标识',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================================
-- 4. goods — 商品表
-- ============================================================
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `title`          VARCHAR(200)   NOT NULL COMMENT '商品标题',
  `description`    TEXT           COMMENT '商品描述',
  `original_price` DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '原价',
  `price`          DECIMAL(10,2)  NOT NULL COMMENT '转让价格',
  `condition`      VARCHAR(30)    NOT NULL DEFAULT 'MINOR_WEAR' COMMENT '成色',
  `campus`         VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '所在校区',
  `category_id`    BIGINT         NOT NULL COMMENT '分类ID',
  `user_id`        BIGINT         NOT NULL COMMENT '发布者ID',
  `status`         VARCHAR(30)    NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态: PENDING_REVIEW, ACTIVE, INACTIVE, REJECTED, TAKEN_DOWN',
  `trade_type`     VARCHAR(30)    NOT NULL DEFAULT 'FACE_TO_FACE' COMMENT '交易方式: FACE_TO_FACE, DELIVERY, BOTH',
  `view_count`     INT            NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` INT            NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_price` (`price`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_view_count` (`view_count`),
  FULLTEXT INDEX `ft_title_desc` (`title`, `description`),
  CONSTRAINT `fk_goods_category` FOREIGN KEY (`category_id`) REFERENCES `category`(`id`),
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 5. goods_image — 商品图片表
-- ============================================================
DROP TABLE IF EXISTS `goods_image`;
CREATE TABLE `goods_image` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `goods_id`    BIGINT       NOT NULL COMMENT '商品ID',
  `image_url`   VARCHAR(500) NOT NULL COMMENT '图片URL',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_goods_id` (`goods_id`),
  CONSTRAINT `fk_image_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- ============================================================
-- 6. goods_view — 商品浏览记录表
-- ============================================================
DROP TABLE IF EXISTS `goods_view`;
CREATE TABLE `goods_view` (
  `id`        BIGINT   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `goods_id`  BIGINT   NOT NULL COMMENT '商品ID',
  `user_id`   BIGINT   COMMENT '浏览用户ID（未登录为NULL）',
  `view_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  INDEX `idx_goods_id` (`goods_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_view_time` (`view_time`),
  CONSTRAINT `fk_view_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品浏览记录表';

-- ============================================================
-- 7. favorite — 收藏表
-- ============================================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `goods_id`    BIGINT   NOT NULL COMMENT '商品ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_goods` (`user_id`, `goods_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_goods_id` (`goods_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_favorite_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================================
-- 8. follow — 关注表
-- ============================================================
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `follower_id` BIGINT   NOT NULL COMMENT '关注者ID',
  `followee_id` BIGINT   NOT NULL COMMENT '被关注者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_follower_followee` (`follower_id`, `followee_id`),
  INDEX `idx_follower_id` (`follower_id`),
  INDEX `idx_followee_id` (`followee_id`),
  CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- ============================================================
-- 9. conversation — 会话表
-- ============================================================
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `goods_id`         BIGINT       NOT NULL COMMENT '关联商品ID',
  `buyer_id`         BIGINT       NOT NULL COMMENT '买家ID',
  `seller_id`        BIGINT       NOT NULL COMMENT '卖家ID',
  `last_message`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '最后一条消息',
  `last_message_time` DATETIME    COMMENT '最后消息时间',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_buyer_id` (`buyer_id`),
  INDEX `idx_seller_id` (`seller_id`),
  INDEX `idx_goods_id` (`goods_id`),
  INDEX `idx_last_msg_time` (`last_message_time`),
  CONSTRAINT `fk_conv_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`),
  CONSTRAINT `fk_conv_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_conv_seller` FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- ============================================================
-- 10. message — 消息表
-- ============================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT       NOT NULL COMMENT '会话ID',
  `sender_id`       BIGINT       NOT NULL COMMENT '发送者ID',
  `receiver_id`     BIGINT       NOT NULL COMMENT '接收者ID',
  `content`         TEXT         NOT NULL COMMENT '消息内容',
  `message_type`    VARCHAR(20)  NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT, IMAGE',
  `is_read`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_id` (`conversation_id`),
  INDEX `idx_sender_id` (`sender_id`),
  INDEX `idx_receiver_id` (`receiver_id`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_msg_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation`(`id`),
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================================
-- 11. orders — 订单表
-- ============================================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `goods_id`    BIGINT         NOT NULL COMMENT '商品ID',
  `buyer_id`    BIGINT         NOT NULL COMMENT '买家ID',
  `seller_id`   BIGINT         NOT NULL COMMENT '卖家ID',
  `status`      VARCHAR(30)    NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING, IN_PROGRESS, COMPLETED, CANCELLED',
  `amount`      DECIMAL(10,2)  NOT NULL COMMENT '交易金额',
  `remark`      VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '买家备注',
  `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_goods_id` (`goods_id`),
  INDEX `idx_buyer_id` (`buyer_id`),
  INDEX `idx_seller_id` (`seller_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`),
  CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 12. order_log — 订单操作日志表
-- ============================================================
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id`    BIGINT       NOT NULL COMMENT '订单ID',
  `action`      VARCHAR(30)  NOT NULL COMMENT '操作类型: CREATE, CONFIRM, CANCEL, COMPLETE',
  `operator_id` BIGINT       NOT NULL COMMENT '操作人ID',
  `remark`      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  CONSTRAINT `fk_log_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';

-- ============================================================
-- 13. review — 评价表
-- ============================================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id`    BIGINT       NOT NULL COMMENT '订单ID',
  `reviewer_id` BIGINT       NOT NULL COMMENT '评价人ID',
  `reviewee_id` BIGINT       NOT NULL COMMENT '被评价人ID',
  `rating`      TINYINT      NOT NULL COMMENT '评分 (1-5)',
  `content`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '评价内容',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_reviewer_id` (`reviewer_id`),
  INDEX `idx_reviewee_id` (`reviewee_id`),
  UNIQUE INDEX `uk_order_reviewer` (`order_id`, `reviewer_id`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),
  CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_review_reviewee` FOREIGN KEY (`reviewee_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================================
-- 14. report — 举报表
-- ============================================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT       NOT NULL COMMENT '举报人ID',
  `type`        VARCHAR(20)  NOT NULL COMMENT '举报类型: GOODS, USER',
  `target_id`   BIGINT       NOT NULL COMMENT '被举报对象ID（商品ID或用户ID）',
  `reason`      VARCHAR(30)  NOT NULL COMMENT '举报原因: FALSE_INFO, FRAUD, AD, VIOLATION',
  `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '举报描述',
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING, APPROVED, REJECTED',
  `handler_id`  BIGINT       COMMENT '处理人ID',
  `handle_note` VARCHAR(500) COMMENT '处理备注',
  `handle_time` DATETIME     COMMENT '处理时间',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_reporter_id` (`reporter_id`),
  INDEX `idx_type_target` (`type`, `target_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_handler_id` (`handler_id`),
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================================
-- 15. notification — 通知表
-- ============================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id`     BIGINT       NOT NULL COMMENT '接收用户ID',
  `type`        VARCHAR(20)  NOT NULL COMMENT '通知类型: SYSTEM, ORDER, REVIEW',
  `title`       VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '通知内容',
  `related_id`  BIGINT       COMMENT '关联业务ID',
  `is_read`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id_read` (`user_id`, `is_read`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================================
-- 16. announcement — 公告表
-- ============================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`        VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content`      TEXT         NOT NULL COMMENT '公告内容',
  `publisher_id` BIGINT       NOT NULL COMMENT '发布人ID（管理员）',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_publisher_id` (`publisher_id`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';
