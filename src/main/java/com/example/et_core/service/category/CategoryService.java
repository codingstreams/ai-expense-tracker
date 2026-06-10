package com.example.et_core.service.category;

import com.example.et_core.dto.CategoryDto;
import com.example.et_core.model.SystemCategory;

import java.util.List;

public interface CategoryService {
    /**
     * Checks if a category exists for the given user.
     * Validates against both SystemCategory and UserCategory.
     */
    boolean existsByUserAndCategory(String appUserId, Long categoryId, boolean isSystemCategory);

    /**
     * Returns all system-level categories (global).
     */
    List<SystemCategory> getAllSystemCategories();

    /**
     * Finds a system category by name.
     */
    SystemCategory getSystemCategoryByName(String categoryName);

    /**
     * Returns all categories visible to the user:
     * system categories + user's custom categories.
     */
    List<CategoryDto> getAllCategoriesForUser(String appUserId);
}
