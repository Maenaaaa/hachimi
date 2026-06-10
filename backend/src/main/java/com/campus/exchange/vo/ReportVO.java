package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private String reporterAvatar;
    private String type;
    private Long targetId;
    private String reason;
    private String description;
    private String status;
    private Long handlerId;
    private String handlerNickname;
    private String handleNote;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}
