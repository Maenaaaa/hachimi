-- 为订单争议表添加选择的聊天记录ID字段
ALTER TABLE `order_dispute`
  ADD COLUMN `selected_chat_message_ids` VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '选择的聊天记录ID列表，逗号分隔';
