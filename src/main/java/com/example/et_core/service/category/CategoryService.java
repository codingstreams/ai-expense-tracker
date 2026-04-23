package com.example.et_core.service.category;

import com.example.et_core.model.Category;

import java.util.List;

public interface CategoryService {
    boolean existsByUserAndCategory(String appUserId, Long aLong);

  List<Category> getAllWithoutUserId();

  Category getByName(String category);
}
