package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal price;
    @TableField("`condition`")
    private String condition;
    private String campus;
    private String tradeType;
    private Long categoryId;
    private Long userId;
    private String status;
    private String aiReviewStatus;
    private Double aiReviewConfidence;
    private String aiReviewNote;
    private Integer viewCount;
    private Integer favoriteCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
