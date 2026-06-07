package com.campus.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RealNameVerifyDTO {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    @NotBlank(message = "学号不能为空")
    private String studentId;
}
