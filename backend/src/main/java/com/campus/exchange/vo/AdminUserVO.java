package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private Long authId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String school;
    private String role;
    private String status;
    private BigDecimal creditScore;
    private String realName;
    private String studentId;
    private String authTitle;
    private String verificationStatus;
    private String idCardImage;
    private Integer goodsCount;
    private Integer orderCount;
    private LocalDateTime createTime;
}
