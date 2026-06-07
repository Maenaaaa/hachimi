package com.campus.exchange.service;

import com.campus.exchange.vo.MessageVO;
import java.util.List;

public interface MessageService {
    MessageVO sendMessage(Long conversationId, Long senderId, String content, String messageType);
    List<MessageVO> getMessages(Long conversationId, Long userId, int page, int size);
    void markAsRead(Long conversationId, Long userId);
    int getUnreadCount(Long userId);
}
