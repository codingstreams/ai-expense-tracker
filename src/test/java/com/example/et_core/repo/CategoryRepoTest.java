package com.example.et_core.repo;

import com.example.et_core.model.AppUser;
import com.example.et_core.model.SystemCategory;
import com.example.et_core.model.UserCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepoTest {
    @Autowired
    private UserCategoryRepo userCategoryRepo;

    @Autowired
    private SystemCategoryRepo systemCategoryRepo;

    @Autowired
    private AppUserRepo appUserRepo;

    @Test
    void shouldReturnTrue_whenUserAndCategoryMatch() {
        final var appUser = AppUser.builder().build();
        final var savedAppUser = appUserRepo.save(appUser);

        final var category = UserCategory.builder()
                .appUser(savedAppUser)
                .name("Test User Category")
                .build();

        final var savedCategory = userCategoryRepo.save(category);

        final var appUserId = savedAppUser.getId();
        final var categoryId = savedCategory.getId();

        assertTrue(userCategoryRepo.existsByAppUserIdAndCategoryId(appUserId, categoryId));
    }

    @Test
    void shouldReturnTrue_whenSystemCategoryMatch() {
        final var category = SystemCategory.builder()
                .name("Test System Category")
                .build();

        final var savedCategory = systemCategoryRepo.save(category);

        final var categoryId = savedCategory.getId();

        assertTrue(systemCategoryRepo.existsById(categoryId));
    }

    @Test
    void shouldReturnFalse_whenUserAndCategoryNotMatch() {
        final var appUser = AppUser.builder().build();
        final var savedAppUser = appUserRepo.save(appUser);

        final var category = UserCategory.builder()
                .appUser(savedAppUser)
                .name("Test User Category")
                .build();

        final var savedCategory = userCategoryRepo.save(category);

        final var appUserId = UUID.randomUUID().toString();
        final var categoryId = savedCategory.getId();

        assertFalse(userCategoryRepo.existsByAppUserIdAndCategoryId(appUserId, categoryId));
    }
}