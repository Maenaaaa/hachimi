package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.common.PageResult;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.AdminService;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final ReportMapper reportMapper;
    private final UserAuthMapper userAuthMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final CategoryMapper categoryMapper;
    private final NotificationService notificationService;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)));
        vo.setTotalGoods(goodsMapper.selectCount(new LambdaQueryWrapper<Goods>().eq(Goods::getDeleted, 0)));
        vo.setTotalOrders(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getDeleted, 0)));
        vo.setTodayNewUsers(userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, LocalDate.now().atStartOfDay())));
        vo.setTodayNewOrders(orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, LocalDate.now().atStartOfDay())));
        vo.setPendingReviewGoods(goodsMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, "PENDING_REVIEW")));
        vo.setPendingReports(reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")));
        vo.setTodayActiveUsers(userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getUpdateTime, LocalDate.now().atStartOfDay())));
        return vo;
    }

    @Override
    public PageResult<AdminUserVO> listUsers(String keyword, int page, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword).or().like(User::getEmail, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> p = new Page<>(page, size);
        userMapper.selectPage(p, wrapper);
        List<AdminUserVO> records = p.getRecords().stream().map(this::toAdminUserVO).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    @Override
    public AdminUserVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return toAdminUserVO(user);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if ("ADMIN".equals(user.getRole())) throw new BusinessException("不能禁用管理员");
        user.setStatus("DISABLED");
        userMapper.updateById(user);
        notificationService.create(userId, "SYSTEM", "账号已被禁用", "您的账号已被管理员禁用，如有疑问请联系管理员", null);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus("ACTIVE");
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void approveGoods(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        goods.setStatus("ACTIVE");
        goodsMapper.updateById(goods);
        notificationService.create(goods.getUserId(), "REVIEW", "商品审核通过",
                "您的商品「" + goods.getTitle() + "」已通过审核", goodsId);
    }

    @Override
    @Transactional
    public void rejectGoods(Long goodsId, String reason) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        goods.setStatus("REJECTED");
        goodsMapper.updateById(goods);
        notificationService.create(goods.getUserId(), "REVIEW", "商品审核未通过",
                "您的商品「" + goods.getTitle() + "」未通过审核，原因：" + (reason != null ? reason : ""), goodsId);
    }

    @Override
    @Transactional
    public void takeDownGoods(Long goodsId, String reason) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        goods.setStatus("TAKEN_DOWN");
        goodsMapper.updateById(goods);
        notificationService.create(goods.getUserId(), "REVIEW", "商品已被下架",
                "您的商品「" + goods.getTitle() + "」已被管理员下架，原因：" + (reason != null ? reason : ""), goodsId);
    }

    @Override
    @Transactional
    public void deleteGoods(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        goodsMapper.deleteById(goodsId);
        notificationService.create(goods.getUserId(), "SYSTEM", "商品已被删除",
                "您的商品「" + goods.getTitle() + "」已被管理员删除", null);
    }

    @Override
    public PageResult<AdminGoodsVO> listGoods(String status, String keyword, int page, int size) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>().eq(Goods::getDeleted, 0);
        if (status != null && !status.isEmpty()) wrapper.eq(Goods::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Goods::getTitle, keyword);
        wrapper.orderByDesc(Goods::getCreateTime);
        Page<Goods> p = new Page<>(page, size);
        goodsMapper.selectPage(p, wrapper);
        List<AdminGoodsVO> records = p.getRecords().stream().map(this::toAdminGoodsVO).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void approveVerification(Long authId) {
        UserAuth auth = userAuthMapper.selectById(authId);
        if (auth == null) throw new BusinessException("认证记录不存在");
        auth.setStatus("APPROVED");
        userAuthMapper.updateById(auth);
        User user = userMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setRealName(auth.getRealName());
            user.setStudentId(auth.getStudentId());
            user.setAuthTitle(auth.getAuthTitle());
            userMapper.updateById(user);
        }
        notificationService.create(auth.getUserId(), "REVIEW", "实名认证已通过", "您的实名认证已通过审核", authId);
    }

    @Override
    @Transactional
    public void rejectVerification(Long authId, String reason) {
        UserAuth auth = userAuthMapper.selectById(authId);
        if (auth == null) throw new BusinessException("认证记录不存在");
        auth.setStatus("REJECTED");
        auth.setRejectReason(reason);
        userAuthMapper.updateById(auth);
        notificationService.create(auth.getUserId(), "REVIEW", "实名认证未通过",
                "您的实名认证未通过审核，原因：" + (reason != null ? reason : ""), authId);
    }

    @Override
    public PageResult<AdminUserVO> listVerifications(String status, int page, int size) {
        Page<UserAuth> p = new Page<>(page, size);
        LambdaQueryWrapper<UserAuth> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(UserAuth::getStatus, status);
        wrapper.orderByDesc(UserAuth::getCreateTime);
        userAuthMapper.selectPage(p, wrapper);
        List<AdminUserVO> records = p.getRecords().stream().map(auth -> {
            User user = userMapper.selectById(auth.getUserId());
            if (user == null) return null;
            AdminUserVO vo = toAdminUserVO(user);
            vo.setAuthId(auth.getId());
            vo.setVerificationStatus(auth.getStatus());
            vo.setStudentId(auth.getStudentId());
            vo.setAuthTitle(auth.getAuthTitle());
            vo.setIdCardImage(auth.getIdCardImage());
            return vo;
        }).filter(vo -> vo != null).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    private AdminUserVO toAdminUserVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setSchool(user.getSchool());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreditScore(user.getCreditScore());
        vo.setRealName(user.getRealName());
        vo.setGoodsCount(goodsMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, user.getId()).eq(Goods::getDeleted, 0)).intValue());
        vo.setOrderCount(orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .and(w -> w.eq(Order::getBuyerId, user.getId()).or().eq(Order::getSellerId, user.getId()))).intValue());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private AdminGoodsVO toAdminGoodsVO(Goods goods) {
        AdminGoodsVO vo = new AdminGoodsVO();
        vo.setId(goods.getId());
        vo.setTitle(goods.getTitle());
        vo.setPrice(goods.getPrice());
        vo.setCondition(goods.getCondition());
        vo.setCampus(goods.getCampus());
        vo.setTradeType(goods.getTradeType());
        vo.setStatus(goods.getStatus());
        vo.setViewCount(goods.getViewCount());
        vo.setFavoriteCount(goods.getFavoriteCount());
        vo.setUserId(goods.getUserId());
        User user = userMapper.selectById(goods.getUserId());
        vo.setUserNickname(user != null ? user.getNickname() : "");
        vo.setUserAvatar(user != null ? user.getAvatar() : "");
        Category cat = categoryMapper.selectById(goods.getCategoryId());
        vo.setCategoryName(cat != null ? cat.getName() : "");
        vo.setCreateTime(goods.getCreateTime());
        GoodsImage firstImg = goodsImageMapper.selectOne(
                new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goods.getId())
                        .orderByAsc(GoodsImage::getSortOrder).last("LIMIT 1"));
        vo.setCoverImage(firstImg != null ? firstImg.getImageUrl() : "");
        return vo;
    }
}
