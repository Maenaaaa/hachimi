package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.FavoriteService;
import com.campus.exchange.vo.GoodsCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public Result<Void> add(@CurrentUser User user, @RequestBody Map<String, Long> body) {
        favoriteService.add(user.getId(), body.get("goodsId"));
        return Result.ok();
    }

    @DeleteMapping("/{goodsId}")
    public Result<Void> remove(@CurrentUser User user, @PathVariable Long goodsId) {
        favoriteService.remove(user.getId(), goodsId);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<GoodsCardVO>> getMyFavorites(@CurrentUser User user,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return Result.ok(favoriteService.getMyFavorites(user.getId(), page, size));
    }

    @GetMapping("/check/{goodsId}")
    public Result<Boolean> isFavorited(@CurrentUser User user, @PathVariable Long goodsId) {
        return Result.ok(favoriteService.isFavorited(user.getId(), goodsId));
    }
}
