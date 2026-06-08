-- 订单取消申请相关字段
ALTER TABLE `orders`
  ADD COLUMN `cancel_reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '取消原因',
  ADD COLUMN `cancel_requester_id` BIGINT COMMENT '取消申请人ID',
  ADD COLUMN `cancel_request_time` DATETIME COMMENT '取消申请时间';

-- 订单争议表
CREATE TABLE IF NOT EXISTS `order_dispute` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '争议ID',
  `order_id`     BIGINT       NOT NULL COMMENT '订单ID',
  `applicant_id` BIGINT       NOT NULL COMMENT '申请人ID',
  `reason`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '申请原因',
  `status`       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING, RESOLVED, REJECTED',
  `handler_id`   BIGINT       COMMENT '处理人ID',
  `handle_note`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT '处理备注',
  `handle_time`  DATETIME     COMMENT '处理时间',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_applicant_id` (`applicant_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_dispute_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),
  CONSTRAINT `fk_dispute_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_dispute_handler` FOREIGN KEY (`handler_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单争议表';
