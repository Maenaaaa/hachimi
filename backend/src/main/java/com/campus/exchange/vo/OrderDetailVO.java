package com.campus.exchange.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderVO {
    private String goodsDescription;
    private List<OrderLogVO> logs;
}
