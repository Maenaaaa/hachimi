-- ============================================================
-- 为现有表添加 AI 相关字段
-- ============================================================

-- report 表增加 AI 字段
ALTER TABLE `report` ADD COLUMN `ai_judgment_id` BIGINT DEFAULT NULL COMMENT 'AI判决记录ID' AFTER `handle_time`;
ALTER TABLE `report` ADD COLUMN `ai_auto_handled` TINYINT(1) DEFAULT 0 COMMENT '是否AI自动处理' AFTER `ai_judgment_id`;

-- order_dispute 表增加 AI 字段
ALTER TABLE `order_dispute` ADD COLUMN `ai_judgment_id` BIGINT DEFAULT NULL COMMENT 'AI判决记录ID' AFTER `handle_time`;
ALTER TABLE `order_dispute` ADD COLUMN `ai_auto_handled` TINYINT(1) DEFAULT 0 COMMENT '是否AI自动处理' AFTER `ai_judgment_id`;

-- goods 表增加 AI 审核字段
ALTER TABLE `goods` ADD COLUMN `ai_review_status` VARCHAR(20) DEFAULT NULL COMMENT 'AI审核状态: PENDING, APPROVED, REJECTED, ESCALATED' AFTER `status`;
ALTER TABLE `goods` ADD COLUMN `ai_review_confidence` DECIMAL(3,2) DEFAULT NULL COMMENT 'AI审核置信度' AFTER `ai_review_status`;
ALTER TABLE `goods` ADD COLUMN `ai_review_note` TEXT DEFAULT NULL COMMENT 'AI审核备注' AFTER `ai_review_confidence`;
