package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.CreateCommentDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.CommentService;
import com.campus.exchange.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(@CurrentUser User user, @Valid @RequestBody CreateCommentDTO dto) {
        return Result.ok(commentService.create(user.getId(), dto));
    }

    @GetMapping("/goods/{goodsId}")
    public Result<List<CommentVO>> getByGoodsId(@PathVariable Long goodsId) {
        return Result.ok(commentService.getByGoodsId(goodsId));
    }
}
