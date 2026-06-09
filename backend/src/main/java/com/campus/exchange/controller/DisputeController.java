package com.campus.exchange.controller;

import com.campus.exchange.common.PageResult;
import com.campus.exchange.common.Result;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.DisputeService;
import com.campus.exchange.vo.DisputeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dispute")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public Result<PageResult<DisputeVO>> list(@RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return Result.ok(disputeService.list(status, page, size));
    }

    @GetMapping("/{id}")
    public Result<DisputeVO> getById(@PathVariable Long id) {
        return Result.ok(disputeService.getById(id));
    }

    @PutMapping("/{id}/handle")
    public Result<DisputeVO> handle(@CurrentUser User user, @PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        return Result.ok(disputeService.handle(id, user.getId(), body.get("status"), body.get("handleNote")));
    }
}
