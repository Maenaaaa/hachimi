package com.campus.exchange.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReviewContext {

    private Long goodsId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String condition;
    private String categoryName;
    private double publisherCreditScore;
    private Long publisherId;
    private List<String> imageUrls;
}
