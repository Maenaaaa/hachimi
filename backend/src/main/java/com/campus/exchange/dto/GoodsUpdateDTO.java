package com.campus.exchange.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsUpdateDTO {
    private String title;
    private Long categoryId;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String condition;
    private String description;
    private String campus;
    private String tradeType;
    private List<String> images;
}
