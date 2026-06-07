package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("goods_comment")
public class GoodsComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long userId;
    private Long parentId;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
