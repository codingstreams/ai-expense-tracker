package com.example.et_core.service.transaction;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.AccountNotOwnedByUserException;
import com.example.et_core.exception.CategoryNotFoundException;
import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.exception.PaymentModeNotFoundException;
import com.example.et_core.mapper.TransactionMapper;
import com.example.et_core.model.TransactionType;
import com.example.et_core.repo.TransactionRepo;
import com.example.et_core.service.account.AccountService;
import com.example.et_core.service.category.CategoryService;
import com.example.et_core.service.paymentmode.PaymentModeService;
import com.example.et_core.service.transaction.strategy.OperationType;
import com.example.et_core.service.transaction.strategy.TxnTypeStrategyFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.example.et_core.specification.TransactionSpecification;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {
  private final AccountService accountService;
  private final CategoryService categoryService;
  private final PaymentModeService paymentModeService;
  private final TransactionRepo transactionRepo;
  private final TransactionMapper transactionMapper;
  private final TxnTypeStrategyFactory txnTypeStrategyFactory;

  @Transactional
  @Override
  public TransactionDto saveTransaction(String appUserId, TransactionRequestDto requestBody)
      throws InsufficientAccountBalanceException {

    getAndValidateAccounts(requestBody, appUserId);

    final var transactionType = TransactionType.valueOf(requestBody.type()) == TransactionType.TRANSFER
        ? TransactionType.TRANSFER
        : TransactionType.INCOME;

    final var strategy = txnTypeStrategyFactory.getStrategy(transactionType);

    return strategy.process(appUserId, requestBody, OperationType.CREATE);
  }

  private void getAndValidateAccounts(TransactionRequestDto dto, String appUserId) {
    final var accountId = dto.accountId();
    final var categoryId = dto.categoryId();
    final var paymentModeId = dto.paymentModeId();
    final var toAccountId = dto.toAccountId();
    final var type = dto.type();

    final var accounts = getAccounts(accountId, toAccountId, type);

    validateAccountCategoryAndPaymentMode(appUserId, accounts, categoryId, paymentModeId);
  }

  private static List<Long> getAccounts(Long accountId, Long toAccountId, String type) {
    List<Long> accounts = new ArrayList<>();
    accounts.add(accountId);

    if (TransactionType.valueOf(type) == TransactionType.TRANSFER) {
      accounts.add(toAccountId);
    }
    return accounts;
  }

  private void validateAccountCategoryAndPaymentMode(String appUserId, List<Long> accounts, Long categoryId,
      Long paymentModeId) {
    final var accountExists = accountService.existsByUserAndAccount(appUserId, accounts);

    if (!accountExists) {
      throw new AccountNotOwnedByUserException(accounts, appUserId);
    }

    boolean isSystemCategory = categoryId == null || categoryId > 0;
    Long resolvedCategoryId = categoryId != null ? Math.abs(categoryId) : null;
    final var categoryExists = categoryService.existsByUserAndCategory(appUserId, resolvedCategoryId, isSystemCategory);

    if (!categoryExists) {
      throw new CategoryNotFoundException(categoryId);
    }

    final var paymentModeExists = paymentModeService.existsById(paymentModeId);

    if (!paymentModeExists) {
      throw new PaymentModeNotFoundException(categoryId);
    }
  }

  @Override
  public Page<TransactionDto> getAllTransactions(
      String appUserId,
      LocalDate startDate,
      LocalDate endDate,
      Double minAmount,
      Double maxAmount,
      List<TransactionType> types,
      List<Long> categoryIds,
      List<Long> accountIds,
      List<Long> paymentModeIds,
      String search,
      Pageable pageable
  ) {
    final var spec = TransactionSpecification.filterTransactions(
        appUserId, startDate, endDate, minAmount, maxAmount, types, categoryIds, accountIds, paymentModeIds, search
    );
    final var transactionPage = transactionRepo.findAll(spec, pageable);
    return transactionPage.map(transactionMapper::transactionDtoToTransactionDto);
  }

  @Override
  public TransactionDto updateTransaction(String appUserId, TransactionRequestDto requestBody)
      throws InsufficientAccountBalanceException {
    getAndValidateAccounts(requestBody, appUserId);

    final var transactionType = TransactionType.valueOf(requestBody.type()) == TransactionType.TRANSFER
        ? TransactionType.TRANSFER
        : TransactionType.INCOME;

    final var strategy = txnTypeStrategyFactory.getStrategy(transactionType);

    return strategy.process(appUserId, requestBody, OperationType.UPDATE);
  }

  @Override
  public void deleteTransaction(String appUserId, Long transactionId) {
    transactionRepo.deleteByIdAndAppUserId(transactionId, appUserId);
  }

  @Override
  public List<TransactionDto> getRecentTransactions(String userId) {
    var transactions = transactionRepo.findAllByAppUserIdOrderByTransactionDateDesc(userId, PageRequest.ofSize(5));

    return transactionMapper.transactionDtosToTransactionDtos(transactions);
  }
}
