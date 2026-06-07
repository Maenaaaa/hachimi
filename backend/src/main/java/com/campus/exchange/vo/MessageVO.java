package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private Long receiverId;
    private String content;
    private String messageType;
    private Integer isRead;
    private LocalDateTime createTime;
}
