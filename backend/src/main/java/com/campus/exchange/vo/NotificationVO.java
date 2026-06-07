package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createTime;
}
