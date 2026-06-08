package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.CreateReviewDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.ReviewService;
import com.campus.exchange.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<ReviewVO> create(@CurrentUser User user, @Valid @RequestBody CreateReviewDTO dto) {
        return Result.ok(reviewService.create(user.getId(), dto));
    }

    @GetMapping("/{id}")
    public Result<ReviewVO> getById(@PathVariable Long id) {
        return Result.ok(reviewService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public Result<List<ReviewVO>> getBothByOrderId(@PathVariable Long orderId) {
        return Result.ok(reviewService.getBothByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<ReviewVO>> getUserReviews(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reviewService.getUserReviews(userId, page, size));
    }

    @GetMapping("/received")
    public Result<List<ReviewVO>> getMyReceivedReviews(@CurrentUser User user,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reviewService.getMyReceivedReviews(user.getId(), page, size));
    }
}
