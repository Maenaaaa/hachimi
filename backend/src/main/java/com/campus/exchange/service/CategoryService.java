package com.campus.exchange.service;

import com.campus.exchange.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    Category getById(Long id);
}
