package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCoverImage;
    private String goodsTradeType;
    private Long exchangeGoodsId;
    private String exchangeGoodsTitle;
    private String exchangeGoodsCoverImage;
    private BigDecimal amount;
    private String status;
    private String remark;
    private String meetTime;
    private String meetPlace;
    private Long buyerId;
    private String buyerNickname;
    private String buyerAvatar;
    private Long sellerId;
    private String sellerNickname;
    private String sellerAvatar;
    private Boolean buyerReviewed;
    private Boolean sellerReviewed;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
