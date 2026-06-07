package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.CreateOrderDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.OrderService;
import com.campus.exchange.vo.OrderDetailVO;
import com.campus.exchange.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@CurrentUser User user, @Valid @RequestBody CreateOrderDTO dto) {
        return Result.ok(orderService.create(user.getId(), dto));
    }

    @PutMapping("/{id}/confirm")
    public Result<OrderVO> confirm(@CurrentUser User user, @PathVariable Long id) {
        return Result.ok(orderService.confirm(id, user.getId()));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@CurrentUser User user, @PathVariable Long id) {
        return Result.ok(orderService.cancel(id, user.getId()));
    }

    @PutMapping("/{id}/complete")
    public Result<OrderVO> complete(@CurrentUser User user, @PathVariable Long id) {
        return Result.ok(orderService.complete(id, user.getId()));
    }

    @GetMapping("/buyer")
    public Result<List<OrderVO>> getBuyerOrders(@CurrentUser User user,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return Result.ok(orderService.getBuyerOrders(user.getId(), status, page, size));
    }

    @GetMapping("/seller")
    public Result<List<OrderVO>> getSellerOrders(@CurrentUser User user,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.ok(orderService.getSellerOrders(user.getId(), status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> getDetail(@CurrentUser User user, @PathVariable Long id) {
        return Result.ok(orderService.getDetail(id, user.getId()));
    }
}
