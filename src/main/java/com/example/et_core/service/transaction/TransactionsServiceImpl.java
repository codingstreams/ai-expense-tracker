package com.example.et_core.service.transaction;

import com.example.et_core.dto.CreateTransactionDto;
import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.UpdateTransactionDto;
import com.example.et_core.exception.AccountNotFoundException;
import com.example.et_core.exception.CategoryNotFoundException;
import com.example.et_core.exception.PaymentModeNotFoundException;
import com.example.et_core.exception.TransactionNotFoundException;
import com.example.et_core.mapper.TransactionMapper;
import com.example.et_core.model.*;
import com.example.et_core.repo.TransactionRepo;
import com.example.et_core.service.account.AccountService;
import com.example.et_core.service.appuser.AppUserService;
import com.example.et_core.service.category.CategoryService;
import com.example.et_core.service.paymentmode.PaymentModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {
  private final AppUserService appUserService;
  private final AccountService accountService;
  private final CategoryService categoryService;
  private final PaymentModeService paymentModeService;
  private final TransactionRepo transactionRepo;
  private final TransactionMapper transactionMapper;

  @Override
  public TransactionDto saveTransaction(String appUserId, CreateTransactionDto requestBody) {
    final var accountId = requestBody.accountId();
    final var categoryId = requestBody.categoryId();
    final var paymentModeId = requestBody.paymentModeId();

    validateAccountCategoryAndPaymentMode(appUserId, accountId, categoryId, paymentModeId);

    final var transaction = Transaction.builder()
        .appUser(AppUser.ofId((appUserId)))
        .account(Account.ofId(accountId))
        .category(Category.ofId(categoryId))
        .paymentMode(PaymentMode.ofId(paymentModeId))
        .amount(requestBody.amount())
        .transactionDate(requestBody.transactionDate())
        .description(requestBody.description())
        .type(TransactionType.valueOf(requestBody.type()))
        .build();

    final var savedTransaction = transactionRepo.save(transaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  private void validateAccountCategoryAndPaymentMode(String appUserId, Long accountId, Long categoryId, Long paymentModeId) {
    final var accountExists = accountService.existsByUserAndAccount(appUserId, accountId);

    if (!accountExists) {
      throw new AccountNotFoundException(accountId);
    }

    final var categoryExists = categoryService.existsByUserAndCategory(appUserId, categoryId);

    if (!categoryExists) {
      throw new CategoryNotFoundException(categoryId);
    }

    final var paymentModeExists = paymentModeService.existsById(paymentModeId);

    if (!paymentModeExists) {
      throw new PaymentModeNotFoundException(categoryId);
    }
  }

  @Override
  public List<TransactionDto> getAllTransactions(String appUserId) {
    final var transactions = transactionRepo.findAllByAppUser(appUserId);
    return transactionMapper.transactionDtosToTransactionDtos(transactions);
  }

  @Override
  public TransactionDto updateTransaction(String appUserId, UpdateTransactionDto requestBody) {
    final var accountId = requestBody.accountId();
    final var categoryId = requestBody.categoryId();
    final var paymentModeId = requestBody.paymentModeId();

    validateAccountCategoryAndPaymentMode(appUserId, accountId, categoryId, paymentModeId);

    final var transaction = transactionRepo.findById(requestBody.transactionId())
        .orElseThrow(()->new TransactionNotFoundException(requestBody.transactionId()));

    transactionMapper.updateTransactionFromDto(requestBody, transaction, appUserId);

    final var savedTransaction = transactionRepo.save(transaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  @Override
  public void deleteTransaction(String appUserId, Long transactionId) {
    transactionRepo.deleteByIdAndAppUserId(transactionId, appUserId);
  }
}
