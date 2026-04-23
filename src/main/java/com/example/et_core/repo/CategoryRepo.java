package com.example.et_core.repo;


import com.example.et_core.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    @Query("SELECT COUNT(*) > 0 " +
            "FROM Category c " +
            "JOIN c.appUser u " +
            "WHERE u.id = :appUserId " +
            "AND c.id = :categoryId")
    boolean existsByAppUserIdAndCategoryId(String appUserId, Long categoryId);

    List<Category> findAllByAppUserIsNull();

    Optional<Category> findByName(String category);
}
