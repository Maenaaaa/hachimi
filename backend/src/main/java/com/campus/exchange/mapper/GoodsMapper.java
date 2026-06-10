package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.GoodsSearchDTO;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.vo.GoodsCardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    Page<GoodsCardVO> searchGoods(Page<GoodsCardVO> page, @Param("dto") GoodsSearchDTO dto);

    List<GoodsCardVO> selectRecommendList(@Param("offset") long offset, @Param("size") long size,
                                          @Param("userId") Long userId);

    List<GoodsCardVO> selectLatestList(@Param("offset") long offset, @Param("size") long size,
                                        @Param("userId") Long userId);

    List<GoodsCardVO> selectMyGoods(@Param("userId") Long userId, @Param("status") String status,
                                     @Param("offset") long offset, @Param("size") long size);

    List<GoodsCardVO> selectPublicUserGoods(@Param("userId") Long userId,
                                            @Param("offset") long offset, @Param("size") long size);

    List<GoodsCardVO> selectFavoriteGoods(@Param("userId") Long userId,
                                           @Param("offset") long offset, @Param("size") long size);

    GoodsCardVO selectGoodsCardById(@Param("id") Long id);
}
