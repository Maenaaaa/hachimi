package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.GoodsPublishDTO;
import com.campus.exchange.dto.GoodsUpdateDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.vo.GoodsCardVO;
import com.campus.exchange.vo.GoodsDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @PostMapping
    public Result<GoodsDetailVO> publish(@CurrentUser User user, @Valid @RequestBody GoodsPublishDTO dto) {
        return Result.ok(goodsService.publish(user.getId(), dto));
    }

    @PutMapping("/{id}")
    public Result<GoodsDetailVO> update(@CurrentUser User user, @PathVariable Long id, @RequestBody GoodsUpdateDTO dto) {
        return Result.ok(goodsService.update(id, user.getId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@CurrentUser User user, @PathVariable Long id) {
        goodsService.delete(id, user.getId());
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@CurrentUser User user, @PathVariable Long id, @RequestBody Map<String, String> body) {
        goodsService.toggleStatus(id, user.getId(), body.get("status"));
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<GoodsDetailVO> getDetail(@PathVariable Long id, @CurrentUser User user) {
        Long currentUserId = user != null ? user.getId() : null;
        return Result.ok(goodsService.getDetail(id, currentUserId));
    }

    @GetMapping("/recommend")
    public Result<List<GoodsCardVO>> getRecommendList(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @CurrentUser User user) {
        Long currentUserId = user != null ? user.getId() : null;
        return Result.ok(goodsService.getRecommendList(page, size, currentUserId));
    }

    @GetMapping("/latest")
    public Result<List<GoodsCardVO>> getLatestList(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @CurrentUser User user) {
        Long currentUserId = user != null ? user.getId() : null;
        return Result.ok(goodsService.getLatestList(page, size, currentUserId));
    }

    @PostMapping("/{id}/view")
    public Result<Void> recordView(@PathVariable Long id, @CurrentUser User user) {
        goodsService.recordView(id, user != null ? user.getId() : null);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<GoodsCardVO>> getMyGoods(@CurrentUser User user,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return Result.ok(goodsService.getMyGoods(user.getId(), status, page, size));
    }

    @GetMapping("/user/{userId}")
    public Result<List<GoodsCardVO>> getUserGoods(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "50") int size) {
        return Result.ok(goodsService.getMyGoods(userId, null, page, size));
    }
}
