package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.GoodsSearchDTO;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.vo.GoodsCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final GoodsService goodsService;

    @GetMapping
    public Result<Map<String, Object>> search(GoodsSearchDTO dto,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        var result = goodsService.search(dto, page, size);
        return Result.ok(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize()
        ));
    }
}
