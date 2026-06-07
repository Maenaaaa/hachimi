package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<List<NotificationVO>> getList(@CurrentUser User user,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return Result.ok(notificationService.getList(user.getId(), page, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@CurrentUser User user, @PathVariable Long id) {
        notificationService.markAsRead(id, user.getId());
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@CurrentUser User user) {
        notificationService.markAllAsRead(user.getId());
        return Result.ok();
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@CurrentUser User user) {
        return Result.ok(notificationService.getUnreadCount(user.getId()));
    }
}
