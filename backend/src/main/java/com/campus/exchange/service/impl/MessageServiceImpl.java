package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.entity.Conversation;
import com.campus.exchange.entity.Message;
import com.campus.exchange.entity.User;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.ConversationMapper;
import com.campus.exchange.mapper.MessageMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.MessageService;
import com.campus.exchange.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public MessageVO sendMessage(Long conversationId, Long senderId, String content, String messageType) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) throw new BusinessException("会话不存在");

        Long receiverId = conv.getBuyerId().equals(senderId) ? conv.getSellerId() : conv.getBuyerId();

        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setMessageType(messageType != null ? messageType : "TEXT");
        msg.setIsRead(0);
        messageMapper.insert(msg);

        conv.setLastMessage(content.length() > 50 ? content.substring(0, 50) : content);
        conv.setLastMessageTime(LocalDateTime.now());
        conversationMapper.updateById(conv);

        return toVO(msg);
    }

    @Override
    public List<MessageVO> getMessages(Long conversationId, Long userId, int page, int size) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || (!conv.getBuyerId().equals(userId) && !conv.getSellerId().equals(userId))) {
            throw new BusinessException(403, "无权查看此会话");
        }

        Page<Message> p = new Page<>(page, size);
        List<Message> msgs = messageMapper.selectPage(p,
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreateTime)).getRecords();
        return msgs.stream().map(this::toVO).toList();
    }

    @Override
    public void markAsRead(Long conversationId, Long userId) {
        messageMapper.markAsRead(conversationId, userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, 0)).intValue();
    }

    private MessageVO toVO(Message msg) {
        User sender = userMapper.selectById(msg.getSenderId());
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setSenderNickname(sender != null ? sender.getNickname() : "");
        vo.setSenderAvatar(sender != null ? sender.getAvatar() : "");
        vo.setReceiverId(msg.getReceiverId());
        vo.setContent(msg.getContent());
        vo.setMessageType(msg.getMessageType());
        vo.setIsRead(msg.getIsRead());
        vo.setCreateTime(msg.getCreateTime());
        return vo;
    }
}
