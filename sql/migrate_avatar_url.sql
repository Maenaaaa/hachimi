-- 数据库迁移脚本：将头像URL转换为相对路径
-- 执行前请备份数据库！

-- 更新用户表的avatar字段
-- 将 http://192.168.0.21:9000/avatars/xxx.jpg 格式转换为 avatars/xxx.jpg
UPDATE user 
SET avatar = CASE 
    WHEN avatar LIKE 'http://%/avatars/%' THEN 
        CONCAT('avatars/', SUBSTRING_INDEX(avatar, '/avatars/', -1))
    ELSE avatar
END
WHERE avatar LIKE 'http://%/avatars/%';

-- 验证迁移结果
SELECT id, username, avatar FROM user WHERE avatar IS NOT NULL LIMIT 10;
