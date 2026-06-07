package com.campus.exchange.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.dto.ChatMessageDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.MessageService;
import com.campus.exchange.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    @MessageMapping("/chat")
    public void handleChat(@Payload ChatMessageDTO dto, Principal principal) {
        Long senderId = null;
        if (principal != null) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, principal.getName()));
            if (user != null) {
                senderId = user.getId();
            }
        }

        MessageVO messageVO = messageService.sendMessage(
                dto.getConversationId(), senderId, dto.getContent(), dto.getMessageType());

        messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getConversationId(), messageVO);
    }
}
