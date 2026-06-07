package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.entity.Category;
import com.campus.exchange.mapper.CategoryMapper;
import com.campus.exchange.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getDeleted, 0).orderByAsc(Category::getSortOrder));
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }
}
