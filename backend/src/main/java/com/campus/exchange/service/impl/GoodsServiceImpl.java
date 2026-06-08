package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.GoodsPublishDTO;
import com.campus.exchange.dto.GoodsSearchDTO;
import com.campus.exchange.dto.GoodsUpdateDTO;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.vo.GoodsCardVO;
import com.campus.exchange.vo.GoodsDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final GoodsViewMapper goodsViewMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public GoodsDetailVO publish(Long userId, GoodsPublishDTO dto) {
        Goods goods = new Goods();
        goods.setTitle(dto.getTitle());
        goods.setCategoryId(dto.getCategoryId());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setPrice(dto.getPrice());
        goods.setCondition(dto.getCondition());
        goods.setDescription(dto.getDescription());
        goods.setCampus(dto.getCampus());
        goods.setTradeType(dto.getTradeType());
        goods.setUserId(userId);
        goods.setStatus("PENDING_REVIEW");
        goodsMapper.insert(goods);

        for (int i = 0; i < dto.getImages().size(); i++) {
            GoodsImage img = new GoodsImage();
            img.setGoodsId(goods.getId());
            img.setImageUrl(dto.getImages().get(i));
            img.setSortOrder(i);
            goodsImageMapper.insert(img);
        }

        return getDetail(goods.getId(), userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "goodsDetail", key = "#goodsId")
    public GoodsDetailVO update(Long goodsId, Long userId, GoodsUpdateDTO dto) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getDeleted() == 1) throw new BusinessException("商品不存在");
        if (!goods.getUserId().equals(userId)) throw new BusinessException(403, "无权修改此商品");

        if (dto.getTitle() != null) goods.setTitle(dto.getTitle());
        if (dto.getCategoryId() != null) goods.setCategoryId(dto.getCategoryId());
        if (dto.getOriginalPrice() != null) goods.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getPrice() != null) goods.setPrice(dto.getPrice());
        if (dto.getCondition() != null) goods.setCondition(dto.getCondition());
        if (dto.getDescription() != null) goods.setDescription(dto.getDescription());
        if (dto.getCampus() != null) goods.setCampus(dto.getCampus());
        if (dto.getTradeType() != null) goods.setTradeType(dto.getTradeType());
        if (!"PENDING_REVIEW".equals(goods.getStatus())) {
            goods.setStatus("PENDING_REVIEW");
        }
        goodsMapper.updateById(goods);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            goodsImageMapper.delete(new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goodsId));
            for (int i = 0; i < dto.getImages().size(); i++) {
                GoodsImage img = new GoodsImage();
                img.setGoodsId(goods.getId());
                img.setImageUrl(dto.getImages().get(i));
                img.setSortOrder(i);
                goodsImageMapper.insert(img);
            }
        }

        return getDetail(goods.getId(), userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "goodsDetail", key = "#goodsId")
    public void delete(Long goodsId, Long userId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        if (!goods.getUserId().equals(userId)) throw new BusinessException(403, "无权删除此商品");

        long activeOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getGoodsId, goodsId)
                .in(Order::getStatus, "PENDING", "IN_PROGRESS"));
        if (activeOrders > 0) throw new BusinessException("该商品有进行中的订单，无法删除");

        goodsMapper.deleteById(goodsId);
    }

    @Override
    @CacheEvict(value = "goodsDetail", key = "#goodsId")
    public void toggleStatus(Long goodsId, Long userId, String status) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        if (!goods.getUserId().equals(userId)) throw new BusinessException(403, "无权操作此商品");
        goods.setStatus(status);
        goodsMapper.updateById(goods);
    }

    @Override
    public GoodsDetailVO getDetail(Long goodsId, Long currentUserId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getDeleted() == 1) throw new BusinessException(404, "商品不存在或已下架");
        if (!goods.getUserId().equals(currentUserId) &&
                !"ACTIVE".equals(goods.getStatus()) && !"SOLD".equals(goods.getStatus()) && !"INACTIVE".equals(goods.getStatus())) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        User seller = userMapper.selectById(goods.getUserId());
        Category category = categoryMapper.selectById(goods.getCategoryId());
        List<GoodsImage> images = goodsImageMapper.selectList(
                new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goodsId).orderByAsc(GoodsImage::getSortOrder));

        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setId(goods.getId());
        vo.setTitle(goods.getTitle());
        vo.setDescription(goods.getDescription());
        vo.setOriginalPrice(goods.getOriginalPrice());
        vo.setPrice(goods.getPrice());
        vo.setCondition(goods.getCondition());
        vo.setCampus(goods.getCampus());
        vo.setTradeType(goods.getTradeType());
        vo.setStatus(goods.getStatus());
        vo.setViewCount(goods.getViewCount());
        vo.setFavoriteCount(goods.getFavoriteCount());
        vo.setCreateTime(goods.getCreateTime());
        vo.setCategoryId(category != null ? category.getId() : null);
        vo.setCategoryName(category != null ? category.getName() : "");
        vo.setUserId(seller != null ? seller.getId() : null);
        vo.setUserNickname(seller != null ? seller.getNickname() : "");
        vo.setUserAvatar(seller != null ? seller.getAvatar() : "");
        vo.setUserCreditScore(seller != null ? seller.getCreditScore() : null);
        vo.setImages(images.stream().map(GoodsImage::getImageUrl).toList());
        vo.setSellerGoodsCount(goodsMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, goods.getUserId()).eq(Goods::getStatus, "ACTIVE")).intValue());
        vo.setSellerFollowerCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, goods.getUserId())).intValue());

        if (currentUserId != null) {
            vo.setIsFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, currentUserId).eq(Favorite::getGoodsId, goodsId)) > 0);
            vo.setIsFollowed(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getFollowerId, currentUserId).eq(Follow::getFolloweeId, goods.getUserId())) > 0);
        }

        return vo;
    }

    @Override
    public List<GoodsCardVO> getRecommendList(int page, int size) {
        return goodsMapper.selectRecommendList((long) (page - 1) * size, size);
    }

    @Override
    public List<GoodsCardVO> getLatestList(int page, int size) {
        return goodsMapper.selectLatestList((long) (page - 1) * size, size);
    }

    @Override
    public void recordView(Long goodsId, Long userId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            goods.setViewCount(goods.getViewCount() + 1);
            goodsMapper.updateById(goods);

            GoodsView view = new GoodsView();
            view.setGoodsId(goodsId);
            view.setUserId(userId);
            view.setViewTime(LocalDateTime.now());
            goodsViewMapper.insert(view);
        }
    }

    @Override
    public List<GoodsCardVO> getMyGoods(Long userId, String status, int page, int size) {
        return goodsMapper.selectMyGoods(userId, status, (long) (page - 1) * size, size);
    }

    @Override
    public Page<GoodsCardVO> search(GoodsSearchDTO dto, int page, int size) {
        Page<GoodsCardVO> p = new Page<>(page, size);
        return goodsMapper.searchGoods(p, dto);
    }
}
