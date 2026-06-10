-- ============================================================
-- 迁移脚本：将数据库中的完整MinIO URL转换为相对路径格式
-- 执行前请备份数据库！
-- ============================================================

USE campus_exchange;

-- 1. 修复商品图片表 (goods_image)
-- 将 http://xxx:9000/goods-images/xxx 转换为 goods-images/xxx
UPDATE goods_image 
SET image_url = CONCAT('goods-images/', SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '?', 1), '/goods-images/', -1))
WHERE image_url LIKE 'http%://%/goods-images/%';

-- 2. 修复用户表头像 (user.avatar)
-- 将 http://xxx:9000/avatars/xxx 转换为 avatars/xxx
UPDATE user 
SET avatar = CONCAT('avatars/', SUBSTRING_INDEX(SUBSTRING_INDEX(avatar, '?', 1), '/avatars/', -1))
WHERE avatar LIKE 'http%://%/avatars/%';

-- 3. 修复实名认证表 (user_auth.id_card_image)
-- 将 http://xxx:9000/verification/xxx 转换为 verification/xxx
UPDATE user_auth 
SET id_card_image = CONCAT('verification/', SUBSTRING_INDEX(SUBSTRING_INDEX(id_card_image, '?', 1), '/verification/', -1))
WHERE id_card_image LIKE 'http%://%/verification/%';

-- 4. 修复消息表中的图片URL (message.content)
-- 处理 IMAGE 类型的消息
UPDATE message 
SET content = CONCAT('chat-images/', SUBSTRING_INDEX(SUBSTRING_INDEX(content, '?', 1), '/chat-images/', -1))
WHERE message_type = 'IMAGE' 
  AND content LIKE 'http%://%/chat-images/%';

-- 5. 修复消息表中的商品卡片消息（coverImage字段）
-- 这些是JSON格式的消息，需要提取coverImage字段并转换
UPDATE message 
SET content = JSON_SET(
    content, 
    '$.coverImage', 
    CONCAT('goods-images/', SUBSTRING_INDEX(SUBSTRING_INDEX(JSON_UNQUOTE(JSON_EXTRACT(content, '$.coverImage')), '?', 1), '/goods-images/', -1))
)
WHERE content LIKE '%"coverImage":"http%://%/goods-images/%';

-- 验证迁移结果
SELECT 'goods_image' as table_name, COUNT(*) as remaining_urls
FROM goods_image 
WHERE image_url LIKE 'http%://%'
UNION ALL
SELECT 'user' as table_name, COUNT(*) as remaining_urls
FROM user 
WHERE avatar LIKE 'http%://%'
UNION ALL
SELECT 'user_auth' as table_name, COUNT(*) as remaining_urls
FROM user_auth 
WHERE id_card_image LIKE 'http%://%'
UNION ALL
SELECT 'message (IMAGE)' as table_name, COUNT(*) as remaining_urls
FROM message 
WHERE message_type = 'IMAGE' AND content LIKE 'http%://%';
