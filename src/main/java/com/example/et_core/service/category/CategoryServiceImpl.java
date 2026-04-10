package com.example.et_core.service.category;

import com.example.et_core.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepo categoryRepo;

  @Override
  public boolean existsByUserAndCategory(String appUserId, Long categoryId) {
    return categoryRepo.existsByAppUserIdAndCategoryId(appUserId, categoryId) || categoryRepo.existsById(categoryId);
  }
}
