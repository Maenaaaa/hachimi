package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.FollowService;
import com.campus.exchange.vo.UserPublicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public Result<Void> follow(@CurrentUser User user, @RequestBody Map<String, Long> body) {
        followService.follow(user.getId(), body.get("followeeId"));
        return Result.ok();
    }

    @DeleteMapping("/{followeeId}")
    public Result<Void> unfollow(@CurrentUser User user, @PathVariable Long followeeId) {
        followService.unfollow(user.getId(), followeeId);
        return Result.ok();
    }

    @GetMapping("/followers")
    public Result<List<UserPublicVO>> getFollowers(@RequestParam Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.ok(followService.getFollowers(userId, page, size));
    }

    @GetMapping("/following")
    public Result<List<UserPublicVO>> getFollowing(@RequestParam Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.ok(followService.getFollowing(userId, page, size));
    }
}
