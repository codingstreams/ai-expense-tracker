package com.example.et_core.repo;


import com.example.et_core.model.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepo extends CrudRepository<Account, Long> {
    @Query("SELECT COUNT(*) > 0 " +
            "FROM Account a " +
            "JOIN a.appUser u " +
            "WHERE u.id = :appUserId " +
            "AND a.id = :accountId")
    boolean existsByAppUserIdAndAccountId(String appUserId, Long accountId);
}
