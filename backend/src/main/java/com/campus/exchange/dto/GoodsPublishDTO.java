package com.campus.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsPublishDTO {
    @NotBlank(message = "商品标题不能为空")
    @jakarta.validation.constraints.Size(max = 100, message = "标题最长100个字符")
    private String title;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    private BigDecimal originalPrice;
    private BigDecimal price;
    @NotBlank(message = "交易类型不能为空")
    private String tradeType;
    @NotBlank(message = "商品成色不能为空")
    private String condition;
    private String description;
    @NotBlank(message = "所在校区不能为空")
    private String campus;
    @NotEmpty(message = "请至少上传一张图片")
    private List<String> images;
}
