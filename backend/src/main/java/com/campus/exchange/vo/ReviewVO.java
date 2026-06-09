package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long orderId;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCoverImage;
    private Long reviewerId;
    private String reviewerNickname;
    private String reviewerAvatar;
    private String reviewerRole;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
