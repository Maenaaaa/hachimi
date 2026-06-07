package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminGoodsVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String condition;
    private String campus;
    private String tradeType;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String categoryName;
    private String coverImage;
    private LocalDateTime createTime;
}
