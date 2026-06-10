package com.example.et_core.service.transaction.strategy;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.exception.TransactionNotFoundException;
import com.example.et_core.mapper.TransactionMapper;
import com.example.et_core.model.Transaction;
import com.example.et_core.model.TransactionType;
import com.example.et_core.repo.TransactionRepo;
import com.example.et_core.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.UUID;

@Component("TransferStrategy")
@RequiredArgsConstructor
public class TransferStrategy implements TransactionTypeStrategy {
  private final AccountService accountService;
  private final TransactionMapper transactionMapper;
  private final TransactionRepo transactionRepo;

  @Override
  public TransactionDto process(String appUserId, TransactionRequestDto dto, OperationType type)
      throws InsufficientAccountBalanceException {
    Transaction debitTransaction;
    Transaction creditTransaction;
    String transferId;

    if (type == OperationType.UPDATE) {
      transferId = dto.transferId();

      if (Objects.isNull(transferId)) {
        throw new RuntimeException("Transfer ID is null");
      }

      final var transactions = transactionRepo.findAllByTransferIdAndAppUserId(transferId, appUserId);

      if (transactions.isEmpty() || transactions.size() < 2) {
        throw new RuntimeException("Unable to find any transactions for transferId: " + dto.transferId());
      }

      debitTransaction = transactions.stream()
          .filter(e -> e.getAmount() < 0)
          .findAny()
          .orElseThrow(() -> new TransactionNotFoundException(dto.accountId()));

      creditTransaction = transactions.stream()
          .filter(e -> e.getAmount() > 0)
          .findAny()
          .orElseThrow(() -> new TransactionNotFoundException(dto.accountId()));

      accountService.reverseBalance(debitTransaction.getAccount().getId(), Math.abs(debitTransaction.getAmount()),
          dto.paymentModeId(), dto.type(), true);

      accountService.reverseBalance(creditTransaction.getAccount().getId(), Math.abs(creditTransaction.getAmount()),
          dto.paymentModeId(), dto.type(), false);

    } else {
      debitTransaction = new Transaction();
      creditTransaction = new Transaction();
      transferId = UUID.randomUUID().toString();
    }

    accountService.updateBalance(dto.accountId(), dto.amount(), dto.paymentModeId(), dto.type(), true);

    accountService.updateBalance(dto.toAccountId(), dto.amount(), dto.paymentModeId(), dto.type(), false);

    transactionMapper.transactionFromRequestDto(dto, debitTransaction, appUserId, transferId, true);

    transactionMapper.transactionFromRequestDto(dto, creditTransaction, appUserId, transferId, false);

    transactionRepo.save(debitTransaction);
    final var savedTransaction = transactionRepo.save(creditTransaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  @Override
  public TransactionType getType() {
    return TransactionType.TRANSFER;
  }
}
