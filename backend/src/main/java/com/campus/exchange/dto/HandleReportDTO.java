package com.campus.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HandleReportDTO {
    @NotBlank(message = "处理结果不能为空")
    private String status;
    private String handleNote;
}
