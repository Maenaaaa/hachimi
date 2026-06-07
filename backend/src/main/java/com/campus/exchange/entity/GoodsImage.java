package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("goods_image")
public class GoodsImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private String imageUrl;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
