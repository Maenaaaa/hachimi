package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_judgment")
public class AiJudgmentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sourceType;
    private Long sourceId;
    private String aiModel;
    private Double confidence;
    private String verdict;
    private String reasoning;
    private String evidence;
    private Boolean autoHandled;
    private Long handlerId;
    private String handleNote;
    private LocalDateTime handleTime;
    private String appealReason;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
