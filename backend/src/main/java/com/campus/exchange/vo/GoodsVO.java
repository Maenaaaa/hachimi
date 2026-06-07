package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String condition;
    private String campus;
    private String tradeType;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createTime;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private BigDecimal userCreditScore;
    private List<String> images;
}
