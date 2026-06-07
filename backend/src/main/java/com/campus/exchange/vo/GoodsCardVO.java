package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsCardVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String condition;
    private Integer viewCount;
    private Integer favoriteCount;
    private String status;
    private String campus;
    private String tradeType;
    private LocalDateTime createTime;
    private Long sellerId;
    private String sellerNickname;
    private String sellerAvatar;
    private String coverImage;
}
