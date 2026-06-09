package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DisputeVO {
    private Long id;
    private Long orderId;
    private String goodsTitle;
    private Long applicantId;
    private String applicantNickname;
    private String applicantAvatar;
    private String reason;
    private String status;
    private Long handlerId;
    private String handlerNickname;
    private String handleNote;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
    private String selectedChatMessageIds;
}
