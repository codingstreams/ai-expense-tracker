package com.example.et_core.repo;

import com.example.et_core.model.Card;
import org.springframework.data.repository.CrudRepository;

public interface CardRepo extends CrudRepository<Card, Long> {
}
