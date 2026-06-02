package com.example.et_core.repo;

import com.example.et_core.model.Card;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CardRepo extends CrudRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.account.appUser.id = :userId")
    List<Card> findAllByAppUserId(String userId);
}
