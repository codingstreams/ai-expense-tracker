package com.example.et_core.service.category;

import com.example.et_core.dto.CategoryDto;
import com.example.et_core.model.SystemCategory;
import com.example.et_core.repo.SystemCategoryRepo;
import com.example.et_core.repo.UserCategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final SystemCategoryRepo systemCategoryRepo;
  private final UserCategoryRepo userCategoryRepo;

  @Override
  public boolean existsByUserAndCategory(String appUserId, Long categoryId, boolean isSystemCategory) {
    if (isSystemCategory) {
      return systemCategoryRepo.existsById(categoryId);
    }
    return userCategoryRepo.existsByAppUserIdAndCategoryId(appUserId, categoryId);
  }

  @Override
  public List<SystemCategory> getAllSystemCategories() {
    return systemCategoryRepo.findAll();
  }

  @Override
  public SystemCategory getSystemCategoryByName(String categoryName) {
    return systemCategoryRepo.findByName(categoryName)
        .orElseThrow(() -> new RuntimeException("System category not found with name: " + categoryName));
  }

  @Override
  public List<CategoryDto> getAllCategoriesForUser(String appUserId) {
    List<CategoryDto> result = new ArrayList<>();

    // Add all system categories (marked as system)
    systemCategoryRepo.findAll().forEach(sc ->
        result.add(new CategoryDto(sc.getId(), sc.getName(), true))
    );

    // Add user's custom categories (marked as user-owned)
    userCategoryRepo.findAllByAppUserId(appUserId).forEach(uc ->
        result.add(new CategoryDto(-uc.getId(), uc.getName(), false))
    );

    return result;
  }
}
