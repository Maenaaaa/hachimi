package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.CreateReviewDTO;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.service.ReviewService;
import com.campus.exchange.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewVO create(Long reviewerId, CreateReviewDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (!"COMPLETED".equals(order.getStatus())) throw new BusinessException("订单未完成，无法评价");
        if (!order.getBuyerId().equals(reviewerId) && !order.getSellerId().equals(reviewerId))
            throw new BusinessException(403, "无权评价此订单");

        if (reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId()).eq(Review::getReviewerId, reviewerId)) > 0) {
            throw new BusinessException("已评价过该用户");
        }

        Long revieweeId = order.getBuyerId().equals(reviewerId) ? order.getSellerId() : order.getBuyerId();

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        reviewMapper.insert(review);

        recalculateCreditScore(revieweeId);

        User reviewer = userMapper.selectById(reviewerId);
        notificationService.create(revieweeId, "REVIEW", "收到新评价",
                (reviewer != null ? reviewer.getNickname() : "用户") + "给您打出了" + dto.getRating() + "星评价", review.getId());

        return toVO(review);
    }

    @Override
    public ReviewVO getById(Long reviewId, Long currentUserId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException("评价不存在");

        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) throw new BusinessException("关联订单不存在");

        boolean isBuyer = currentUserId != null && order.getBuyerId().equals(currentUserId);
        boolean isSeller = currentUserId != null && order.getSellerId().equals(currentUserId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(403, "无权查看该评价");
        }

        if (!review.getReviewerId().equals(currentUserId)) {
            List<Review> allReviews = reviewMapper.selectList(
                    new LambdaQueryWrapper<Review>().eq(Review::getOrderId, review.getOrderId())
                            .eq(Review::getDeleted, 0));
            long reviewCount = allReviews.size();
            if (reviewCount < 2) {
                ReviewVO vo = toVO(review);
                vo.setContent("");
                vo.setRating(0);
                return vo;
            }
        }
        return toVO(review);
    }

    @Override
    public ReviewVO getByOrderId(Long orderId) {
        Review review = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId)
                        .eq(Review::getDeleted, 0).last("LIMIT 1"));
        if (review == null) throw new BusinessException("评价不存在");
        return toVO(review);
    }

    @Override
    public List<ReviewVO> getBothByOrderId(Long orderId, Long currentUserId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        boolean isBuyer = currentUserId != null && order.getBuyerId().equals(currentUserId);
        boolean isSeller = currentUserId != null && order.getSellerId().equals(currentUserId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(403, "无权查看订单评价");
        }

        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId)
                        .eq(Review::getDeleted, 0));

        if (reviews.size() >= 2) {
            return reviews.stream().map(this::toVO).toList();
        }

        ReviewVO currentUserReview = null;
        ReviewVO otherReview = null;

        for (Review review : reviews) {
            if (review.getReviewerId().equals(currentUserId)) {
                currentUserReview = toVO(review);
            } else {
                otherReview = toVO(review);
            }
        }

        if (currentUserReview != null && otherReview == null) {
            return List.of(currentUserReview);
        }

        if (otherReview != null && currentUserReview == null) {
            otherReview.setContent("");
            otherReview.setRating(0);
            return List.of(otherReview);
        }

        return List.of();
    }

    @Override
    public List<ReviewVO> getUserReviews(Long userId, int page, int size) {
        Page<Review> p = new Page<>(page, size);
        List<Review> reviews = reviewMapper.selectPage(p,
                new LambdaQueryWrapper<Review>().eq(Review::getRevieweeId, userId)
                        .eq(Review::getDeleted, 0).orderByDesc(Review::getCreateTime)).getRecords();
        return reviews.stream().map(this::toVO).toList();
    }

    @Override
    public List<ReviewVO> getMyReceivedReviews(Long userId, int page, int size) {
        return getUserReviews(userId, page, size);
    }

    private void recalculateCreditScore(Long userId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>().eq(Review::getRevieweeId, userId).eq(Review::getDeleted, 0));
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
            BigDecimal score = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setCreditScore(score);
                userMapper.updateById(user);
            }
        }
    }

    private ReviewVO toVO(Review review) {
        User reviewer = userMapper.selectById(review.getReviewerId());
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setReviewerId(review.getReviewerId());
        vo.setReviewerNickname(reviewer != null ? reviewer.getNickname() : "");
        vo.setReviewerAvatar(reviewer != null ? reviewer.getAvatar() : "");
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());

        if (review.getOrderId() != null) {
            Order order = orderMapper.selectById(review.getOrderId());
            if (order != null) {
                vo.setGoodsId(order.getGoodsId());
                vo.setReviewerRole(order.getBuyerId().equals(review.getReviewerId()) ? "BUYER" : "SELLER");
                Goods goods = goodsMapper.selectById(order.getGoodsId());
                if (goods != null) {
                    vo.setGoodsTitle(goods.getTitle());
                    GoodsImage img = goodsImageMapper.selectOne(
                            new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goods.getId())
                                    .orderByAsc(GoodsImage::getSortOrder).last("LIMIT 1"));
                    vo.setGoodsCoverImage(img != null ? img.getImageUrl() : "");
                }
            }
        }
        return vo;
    }
}
