package com.campus.exchange.service;

import com.campus.exchange.vo.GoodsCardVO;
import java.util.List;

public interface FavoriteService {
    void add(Long userId, Long goodsId);
    void remove(Long userId, Long goodsId);
    List<GoodsCardVO> getMyFavorites(Long userId, int page, int size);
    boolean isFavorited(Long userId, Long goodsId);
}
