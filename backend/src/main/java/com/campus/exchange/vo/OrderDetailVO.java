package com.campus.exchange.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderVO {
    private String goodsDescription;
    private List<OrderLogVO> logs;
    private String cancelReason;
    private Long cancelRequesterId;
    private String cancelRequesterName;
    private java.time.LocalDateTime cancelRequestTime;
}
