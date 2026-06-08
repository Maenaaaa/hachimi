package com.campus.exchange.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCoverImage;
    private BigDecimal goodsPrice;
    private String goodsTradeType;
    private Long otherUserId;
    private String otherUserNickname;
    private String otherUserAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
