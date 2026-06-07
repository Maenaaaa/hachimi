package com.campus.exchange.service;

import com.campus.exchange.vo.NotificationVO;
import java.util.List;

public interface NotificationService {
    void create(Long userId, String type, String title, String content, Long relatedId);
    List<NotificationVO> getList(Long userId, int page, int size);
    void markAsRead(Long id, Long userId);
    void markAllAsRead(Long userId);
    int getUnreadCount(Long userId);
}
