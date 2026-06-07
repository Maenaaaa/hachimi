-- ============================================================
-- Campus Exchange Platform — Seed Data
-- ============================================================
USE campus_exchange;

-- ============================================================
-- Categories — 商品分类
-- ============================================================
INSERT INTO `category` (`name`, `icon`, `sort_order`) VALUES
('教材书籍', 'book', 1),
('数码产品', 'laptop', 2),
('宿舍用品', 'home', 3),
('体育用品', 'sport', 4),
('自行车', 'bike', 5),
('服饰鞋包', 'clothes', 6),
('其他', 'more', 7);

-- ============================================================
-- Admin User (password: admin123 → BCrypt)
-- ============================================================
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `school`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'ADMIN', 'ACTIVE', '校园交换平台');

-- ============================================================
-- Sample Users (password: 123456 → BCrypt)
-- ============================================================
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `school`, `credit_score`, `real_name`) VALUES
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iK6Z5Eh', '张三同学', 'USER', 'ACTIVE', '计算机学院', 4.8, '张三'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iJ6Z5Eh', '李四学姐', 'USER', 'ACTIVE', '经管学院', 4.5, '李四'),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iH6Z5Eh', '王五学长', 'USER', 'ACTIVE', '电子信息学院', 4.2, '王五');

-- ============================================================
-- Sample Goods
-- ============================================================
INSERT INTO `goods` (`title`, `description`, `original_price`, `price`, `condition`, `campus`, `category_id`, `user_id`, `status`, `view_count`, `favorite_count`)
SELECT '高等数学（第七版）上册', '同济大学数学系编写，九成新，只有少量笔记，考研必备教材。', 46.00, 20.00, 'MINOR_WEAR', '主校区', 1, u.id, 'ACTIVE', 128, 12 FROM user u WHERE u.username='zhangsan'
UNION ALL
SELECT 'Python编程：从入门到实践', '几乎全新，买来没翻几次，送给需要的学弟学妹。', 89.00, 45.00, 'LIKE_NEW', '东校区', 1, u.id, 'ACTIVE', 256, 28 FROM user u WHERE u.username='zhangsan'
UNION ALL
SELECT 'iPad Air 5 64G WiFi版 深空灰', '2023年购入，平时只用来看网课和记笔记，无磕碰无划痕，电池健康95%。配件齐全。', 4799.00, 3200.00, 'MINOR_WEAR', '主校区', 2, u.id, 'ACTIVE', 1024, 86 FROM user u WHERE u.username='lisi'
UNION ALL
SELECT '机械键盘 IKBC C104 茶轴', '用了大概半年，换了新键盘所以出掉，手感很好，声音清脆。', 399.00, 180.00, 'MINOR_WEAR', '西校区', 2, u.id, 'ACTIVE', 345, 22 FROM user u WHERE u.username='wangwu'
UNION ALL
SELECT '宿舍用小电风扇', '静音迷你风扇，三档风速，USB充电，宿舍床头必备。', 59.00, 25.00, 'MINOR_WEAR', '主校区', 3, u.id, 'ACTIVE', 89, 6 FROM user u WHERE u.username='zhangsan'
UNION ALL
SELECT '瑜伽垫 加厚10mm', '买来练了几次就闲置了，几乎全新，加厚款不硌膝盖。', 79.00, 35.00, 'LIKE_NEW', '东校区', 4, u.id, 'ACTIVE', 67, 4 FROM user u WHERE u.username='wangwu'
UNION ALL
SELECT '凤凰牌山地自行车 26寸', '骑了一年多，变速正常，刹车灵敏。送车锁和打气筒。毕业离校转让。', 1599.00, 600.00, 'VISIBLE_WEAR', '主校区', 5, u.id, 'ACTIVE', 567, 45 FROM user u WHERE u.username='lisi'
UNION ALL
SELECT 'Only 羽绒服 女款 M码', '去年冬天买的就穿了两次，几乎全新，白色很显气质。', 899.00, 350.00, 'LIKE_NEW', '东校区', 6, u.id, 'ACTIVE', 189, 15 FROM user u WHERE u.username='lisi';

-- ============================================================
-- Sample Goods Images
-- ============================================================
INSERT INTO `goods_image` (`goods_id`, `image_url`, `sort_order`) VALUES
(1, '/images/goods/math-book-1.jpg', 0),
(1, '/images/goods/math-book-2.jpg', 1),
(2, '/images/goods/python-book-1.jpg', 0),
(3, '/images/goods/ipad-1.jpg', 0),
(3, '/images/goods/ipad-2.jpg', 1),
(3, '/images/goods/ipad-3.jpg', 2),
(4, '/images/goods/keyboard-1.jpg', 0),
(5, '/images/goods/fan-1.jpg', 0),
(6, '/images/goods/yoga-mat-1.jpg', 0),
(7, '/images/goods/bike-1.jpg', 0),
(7, '/images/goods/bike-2.jpg', 1),
(8, '/images/goods/coat-1.jpg', 0);

-- ============================================================
-- Sample Follows
-- ============================================================
INSERT INTO `follow` (`follower_id`, `followee_id`)
SELECT z.id, l.id FROM user z, user l WHERE z.username='zhangsan' AND l.username='lisi'
UNION ALL SELECT z.id, w.id FROM user z, user w WHERE z.username='zhangsan' AND w.username='wangwu'
UNION ALL SELECT l.id, z.id FROM user l, user z WHERE l.username='lisi' AND z.username='zhangsan'
UNION ALL SELECT l.id, w.id FROM user l, user w WHERE l.username='lisi' AND w.username='wangwu'
UNION ALL SELECT w.id, z.id FROM user w, user z WHERE w.username='wangwu' AND z.username='zhangsan'
UNION ALL SELECT w.id, l.id FROM user w, user l WHERE w.username='wangwu' AND l.username='lisi';

-- ============================================================
-- Sample Favorites
-- ============================================================
INSERT INTO `favorite` (`user_id`, `goods_id`)
SELECT u.id, g.id FROM user u, goods g WHERE u.username='zhangsan' AND g.title='iPad Air 5 64G WiFi版 深空灰'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='zhangsan' AND g.title='机械键盘 IKBC C104 茶轴'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='zhangsan' AND g.title='凤凰牌山地自行车 26寸'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='lisi' AND g.title='高等数学（第七版）上册'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='lisi' AND g.title='Python编程：从入门到实践'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='lisi' AND g.title='凤凰牌山地自行车 26寸'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='wangwu' AND g.title='Python编程：从入门到实践'
UNION ALL SELECT u.id, g.id FROM user u, goods g WHERE u.username='wangwu' AND g.title='iPad Air 5 64G WiFi版 深空灰';

-- ============================================================
-- Sample Announcement
-- ============================================================
INSERT INTO `announcement` (`title`, `content`, `publisher_id`) VALUES
('欢迎来到校园闲置物品交换平台！', '📢 平台公约：\n1. 请如实描述商品信息，诚信交易。\n2. 交易请选择校内公共场所见面，注意人身安全。\n3. 发现虚假信息请及时举报。\n4. 祝大家交易愉快！', 1);
