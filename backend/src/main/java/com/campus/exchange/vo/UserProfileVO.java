package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String school;
    private String role;
    private String status;
    private BigDecimal creditScore;
    private Integer goodsCount;
    private Integer orderCount;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean isVerified;
    private String authTitle;
}
