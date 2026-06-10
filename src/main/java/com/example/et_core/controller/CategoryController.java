package com.example.et_core.controller;

import com.example.et_core.dto.CategoryDto;
import com.example.et_core.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(categoryService.getAllCategoriesForUser(userId));
    }
}