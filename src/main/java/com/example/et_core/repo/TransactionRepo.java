package com.example.et_core.repo;

import com.example.et_core.model.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.et_core.dto.DailyCashFlowProjection;
import com.example.et_core.dto.MonthlyCashFlowProjection;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepo extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
  List<Transaction> findAllByAppUserId(String appUserId);

  @Modifying
  @Transactional
  @Query("DELETE " +
      "FROM Transaction t " +
      "WHERE t.appUser.id = :appUserId " +
      "AND t.id = :transactionId")
  void deleteByIdAndAppUserId(Long transactionId, String appUserId);

  List<Transaction> findAllByTransferIdAndAppUserId(String transferId, String appUserId);

  // ALTER TABLE your_table
  // ALTER COLUMN your_column_name TYPE DATE
  // USING TO_DATE(your_column_name, 'YYYY-MM-DD');
  List<Transaction> findAllByAppUserIdOrderByTransactionDateDesc(String appUserId, Pageable pageable);

  @Query("SELECT t.transactionDate as transactionDate, " +
         "SUM(CASE WHEN t.type = com.example.et_core.model.TransactionType.INCOME THEN t.amount ELSE 0.0 END) as income, " +
         "SUM(CASE WHEN t.type = com.example.et_core.model.TransactionType.EXPENSE THEN t.amount ELSE 0.0 END) as expense " +
         "FROM Transaction t " +
         "WHERE t.appUser.id = :appUserId AND t.transactionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY t.transactionDate " +
         "ORDER BY t.transactionDate ASC")
  List<DailyCashFlowProjection> findCashFlowDaily(String appUserId, LocalDate startDate, LocalDate endDate);

  @Query("SELECT YEAR(t.transactionDate) as year, MONTH(t.transactionDate) as month, " +
         "SUM(CASE WHEN t.type = com.example.et_core.model.TransactionType.INCOME THEN t.amount ELSE 0.0 END) as income, " +
         "SUM(CASE WHEN t.type = com.example.et_core.model.TransactionType.EXPENSE THEN t.amount ELSE 0.0 END) as expense " +
         "FROM Transaction t " +
         "WHERE t.appUser.id = :appUserId AND t.transactionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
         "ORDER BY YEAR(t.transactionDate) ASC, MONTH(t.transactionDate) ASC")
  List<MonthlyCashFlowProjection> findCashFlowMonthly(String appUserId, LocalDate startDate, LocalDate endDate);

  @Query("SELECT t FROM Transaction t WHERE t.appUser.id = :appUserId AND t.transactionDate >= :startDate ORDER BY t.transactionDate DESC")
  List<Transaction> findRecentTransactions(@Param("appUserId") String appUserId, @Param("startDate") LocalDate startDate);

  @Query("SELECT COALESCE(t.userCategory.name, t.systemCategory.name, 'Uncategorized') as label, SUM(ABS(t.amount)) as amount " +
         "FROM Transaction t " +
         "WHERE t.appUser.id = :appUserId AND t.type = com.example.et_core.model.TransactionType.EXPENSE " +
         "AND t.transactionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY COALESCE(t.userCategory.name, t.systemCategory.name, 'Uncategorized')")
  List<Object[]> findCategorySpend(
      @Param("appUserId") String appUserId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
