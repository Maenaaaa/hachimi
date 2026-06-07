package com.campus.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportDTO {
    @NotBlank(message = "举报类型不能为空")
    private String type;
    @NotNull(message = "举报目标不能为空")
    private Long targetId;
    @NotBlank(message = "举报原因不能为空")
    private String reason;
    private String description;
}
