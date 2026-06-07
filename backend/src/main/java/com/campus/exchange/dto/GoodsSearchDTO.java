package com.campus.exchange.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoodsSearchDTO {
    private String keyword;
    private Long categoryId;
    private String condition;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String campus;
    private String sort;
}
