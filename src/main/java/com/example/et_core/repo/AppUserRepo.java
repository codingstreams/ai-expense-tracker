package com.example.et_core.repo;


import com.example.et_core.model.AppUser;
import com.example.et_core.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface AppUserRepo extends JpaRepository<AppUser, Long> {
}
