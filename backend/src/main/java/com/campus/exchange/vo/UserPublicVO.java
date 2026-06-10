package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserPublicVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String school;
    private BigDecimal creditScore;
    private String realName;
    private String authTitle;
    private Integer goodsCount;
    private Integer followerCount;
    private Integer followingCount;
}
