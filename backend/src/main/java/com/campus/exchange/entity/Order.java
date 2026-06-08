package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("order_no")
    private String orderNo;
    private Long goodsId;
    private Long exchangeGoodsId;
    private Long buyerId;
    private Long sellerId;
    private String status;
    private BigDecimal amount;
    private String remark;
    private String meetTime;
    private String meetPlace;
    private String cancelReason;
    private Long cancelRequesterId;
    private LocalDateTime cancelRequestTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
