package com.example.et_core.repo;

import com.example.et_core.model.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRepo extends CrudRepository<Transaction, Long> {
  @Query("SELECT t " +
      "FROM Transaction t " +
      "WHERE t.appUser.id = :appUserId")
  List<Transaction> findAllByAppUser(String appUserId);

  @Modifying
  @Transactional
  @Query("DELETE " +
      "FROM Transaction t " +
      "WHERE t.appUser.id = :appUserId " +
      "AND t.id = :transactionId")
  void deleteByIdAndAppUserId(Long transactionId, String appUserId);
}
