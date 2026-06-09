package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_dispute")
public class OrderDispute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long applicantId;
    private String reason;
    private String status;
    private Long handlerId;
    private String handleNote;
    private LocalDateTime handleTime;
    private Long aiJudgmentId;
    private Boolean aiAutoHandled;
    private String selectedChatMessageIds;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
