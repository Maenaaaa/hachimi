package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.GoodsSearchDTO;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.service.HotSearchService;
import com.campus.exchange.vo.GoodsCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final GoodsService goodsService;
    private final HotSearchService hotSearchService;

    @GetMapping
    public Result<Map<String, Object>> search(GoodsSearchDTO dto,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            hotSearchService.record(dto.getKeyword());
        }
        var result = goodsService.search(dto, page, size);
        return Result.ok(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize()
        ));
    }

    @GetMapping("/hot")
    public Result<List<String>> getHotSearches() {
        return Result.ok(hotSearchService.getHotSearches());
    }
}
