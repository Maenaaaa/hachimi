package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.ConversationService;
import com.campus.exchange.service.MessageService;
import com.campus.exchange.vo.ConversationVO;
import com.campus.exchange.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatRestController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping("/conversation")
    public Result<ConversationVO> getOrCreateConversation(@CurrentUser User user,
                                                           @RequestBody Map<String, Long> body) {
        return Result.ok(conversationService.getOrCreate(body.get("goodsId"), user.getId()));
    }

    @GetMapping("/conversation/list")
    public Result<List<ConversationVO>> getConversationList(@CurrentUser User user) {
        return Result.ok(conversationService.getList(user.getId()));
    }

    @GetMapping("/message/{conversationId}")
    public Result<List<MessageVO>> getMessages(@CurrentUser User user,
                                                @PathVariable Long conversationId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        return Result.ok(messageService.getMessages(conversationId, user.getId(), page, size));
    }

    @PutMapping("/message/{conversationId}/read")
    public Result<Void> markAsRead(@CurrentUser User user, @PathVariable Long conversationId) {
        messageService.markAsRead(conversationId, user.getId());
        return Result.ok();
    }

    @GetMapping("/message/unread-count")
    public Result<Integer> getUnreadCount(@CurrentUser User user) {
        return Result.ok(messageService.getUnreadCount(user.getId()));
    }
}
