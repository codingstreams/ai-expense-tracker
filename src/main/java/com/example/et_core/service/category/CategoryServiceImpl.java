package com.example.et_core.service.category;

import com.example.et_core.dto.CategoryDto;
import com.example.et_core.model.Category;
import com.example.et_core.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepo categoryRepo;

  @Override
  public boolean existsByUserAndCategory(String appUserId, Long categoryId) {
    return categoryRepo.existsByAppUserIdAndCategoryId(appUserId, categoryId) || categoryRepo.existsById(categoryId);
  }

  @Override
  public List<Category> getAllWithoutUserId() {
    return categoryRepo.findAllByAppUserIsNull();
  }

  @Override
  public Category getByName(String category) {
    return categoryRepo.findByName(category)
        .orElseThrow(() -> new RuntimeException("Category not found with name " + category));
  }

  @Override
  public List<CategoryDto> getAllCategories() {
    return categoryRepo.findAll()
        .stream()
        .map(c -> new CategoryDto(c.getId(), c.getName())) // we can also create mapper
        .toList();
  }
}
