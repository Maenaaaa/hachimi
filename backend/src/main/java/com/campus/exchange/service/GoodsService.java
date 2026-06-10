package com.campus.exchange.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.GoodsPublishDTO;
import com.campus.exchange.dto.GoodsSearchDTO;
import com.campus.exchange.dto.GoodsUpdateDTO;
import com.campus.exchange.vo.GoodsCardVO;
import com.campus.exchange.vo.GoodsDetailVO;

import java.util.List;

public interface GoodsService {
    GoodsDetailVO publish(Long userId, GoodsPublishDTO dto);
    GoodsDetailVO update(Long goodsId, Long userId, GoodsUpdateDTO dto);
    void delete(Long goodsId, Long userId);
    void toggleStatus(Long goodsId, Long userId, String status);
    GoodsDetailVO getDetail(Long goodsId, Long currentUserId);
    List<GoodsCardVO> getRecommendList(int page, int size, Long currentUserId);
    List<GoodsCardVO> getLatestList(int page, int size, Long currentUserId);
    void recordView(Long goodsId, Long userId);
    List<GoodsCardVO> getMyGoods(Long userId, String status, int page, int size);
    List<GoodsCardVO> getPublicUserGoods(Long userId, int page, int size);
    Page<GoodsCardVO> search(GoodsSearchDTO dto, int page, int size);
}
