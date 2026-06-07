package com.campus.exchange.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsDetailVO extends GoodsVO {
    private Integer sellerGoodsCount;
    private Integer sellerFollowerCount;
    private Boolean isFavorited;
    private Boolean isFollowed;
}
