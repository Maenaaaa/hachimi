package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderLogVO {
    private Long id;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDateTime createTime;
}
