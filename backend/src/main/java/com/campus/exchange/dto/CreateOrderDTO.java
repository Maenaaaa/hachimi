package com.campus.exchange.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderDTO {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    private Long exchangeGoodsId;
    private String remark;
    private String meetTime;
    private String meetPlace;
}
