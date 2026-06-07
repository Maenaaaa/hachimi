package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.entity.Notification;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.NotificationMapper;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void create(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        notificationMapper.insert(n);

        pushToUser(userId, toVO(n));
    }

    @Override
    public List<NotificationVO> getList(Long userId, int page, int size) {
        Page<Notification> p = new Page<>(page, size);
        return notificationMapper.selectPage(p,
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime)).getRecords()
                .stream().map(this::toVO).toList();
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || !n.getUserId().equals(userId)) throw new BusinessException("通知不存在");
        n.setIsRead(1);
        notificationMapper.updateById(n);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)).intValue();
    }

    private void pushToUser(Long userId, NotificationVO notification) {
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", notification);
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setRelatedId(n.getRelatedId());
        vo.setIsRead(n.getIsRead());
        vo.setCreateTime(n.getCreateTime());
        return vo;
    }
}
