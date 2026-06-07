package com.campus.exchange.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @Size(max = 20, message = "昵称最长20个字符")
    private String nickname;
    @Size(max = 20, message = "手机号最长20位")
    private String phone;
    @Size(max = 100, message = "邮箱最长100个字符")
    private String email;
    @Size(max = 50, message = "学校名称最长50个字符")
    private String school;
}
