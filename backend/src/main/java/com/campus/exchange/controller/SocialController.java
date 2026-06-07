package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.FavoriteService;
import com.campus.exchange.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final FollowService followService;
    private final FavoriteService favoriteService;

    @GetMapping("/status")
    public Result<Map<String, Boolean>> getStatus(@CurrentUser User user,
                                                   @RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) Long goodsId) {
        boolean isFollowed = userId != null && followService.isFollowed(user.getId(), userId);
        boolean isFavorited = goodsId != null && favoriteService.isFavorited(user.getId(), goodsId);
        return Result.ok(Map.of("isFollowed", isFollowed, "isFavorited", isFavorited));
    }
}
