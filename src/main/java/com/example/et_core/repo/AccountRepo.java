package com.example.et_core.repo;

import com.example.et_core.model.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepo extends CrudRepository<Account, Long> {
    @Query("SELECT COUNT(*) = :accountListCount " +
            "FROM Account a " +
            "JOIN a.appUser u " +
            "WHERE u.id = :appUserId " +
            "AND a.id IN :accounts")
    boolean existsByAppUserIdAndAccountId(String appUserId, List<Long> accounts, int accountListCount);

    @Query("SELECT a FROM Account a " +
            "JOIN a.appUser u " +
            "LEFT JOIN a.bank b " +
            "WHERE u.id = :appUserId")
    List<Account> findAllByAppUserId(String appUserId);
}
