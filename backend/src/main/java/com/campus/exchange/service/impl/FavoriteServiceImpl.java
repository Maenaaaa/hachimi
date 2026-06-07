package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.entity.Favorite;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.FavoriteMapper;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.service.FavoriteService;
import com.campus.exchange.vo.GoodsCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;

    @Override
    @Transactional
    public void add(Long userId, Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        if (goods.getUserId().equals(userId)) throw new BusinessException("不能收藏自己的商品");

        if (favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getGoodsId, goodsId)) > 0) {
            throw new BusinessException("已收藏该商品");
        }

        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setGoodsId(goodsId);
        favoriteMapper.insert(f);

        goods.setFavoriteCount(goods.getFavoriteCount() + 1);
        goodsMapper.updateById(goods);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long goodsId) {
        Favorite f = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getGoodsId, goodsId));
        if (f == null) throw new BusinessException("未收藏该商品");

        favoriteMapper.deleteById(f.getId());

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null && goods.getFavoriteCount() > 0) {
            goods.setFavoriteCount(goods.getFavoriteCount() - 1);
            goodsMapper.updateById(goods);
        }
    }

    @Override
    public List<GoodsCardVO> getMyFavorites(Long userId, int page, int size) {
        return goodsMapper.selectFavoriteGoods(userId, (long) (page - 1) * size, size);
    }

    @Override
    public boolean isFavorited(Long userId, Long goodsId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getGoodsId, goodsId)) > 0;
    }
}
