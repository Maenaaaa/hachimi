package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.Category;
import com.campus.exchange.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> listAll() {
        return Result.ok(categoryService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.ok(categoryService.getById(id));
    }
}
