package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("goods_view")
public class GoodsView {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long userId;
    private LocalDateTime viewTime;
}
