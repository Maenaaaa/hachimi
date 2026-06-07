package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserVO {
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
    private String realName;
    private String studentId;
    private LocalDateTime createTime;
}
