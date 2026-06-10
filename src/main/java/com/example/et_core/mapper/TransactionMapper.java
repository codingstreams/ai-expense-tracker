package com.example.et_core.mapper;

import com.example.et_core.dto.CategoryDto;
import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.model.*;
import com.example.et_core.service.ai.AiParseResult;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
  TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

  @Mapping(target = "transactionId", source = "id")
  @Mapping(target = "category", expression = "java(transactionToCategoryDto(transaction))")
  TransactionDto transactionDtoToTransactionDto(Transaction transaction);

  /**
   * Maps the effective category from a Transaction to a CategoryDto.
   * UserCategory takes precedence; falls back to SystemCategory.
   */
  default CategoryDto transactionToCategoryDto(Transaction transaction) {
    if (transaction == null) return null;
    if (transaction.getUserCategory() != null) {
      return new CategoryDto(transaction.getUserCategory().getId(),
          transaction.getUserCategory().getName(), false);
    }
    if (transaction.getSystemCategory() != null) {
      return new CategoryDto(transaction.getSystemCategory().getId(),
          transaction.getSystemCategory().getName(), true);
    }
    return null;
  }

  @Named("convertStringToDate")
  default LocalDate convertStringToDate(String transactionDate) {
    return LocalDate.parse(transactionDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  }

  List<TransactionDto> transactionDtosToTransactionDtos(List<Transaction> transactions);

  TransactionRequestDto fromAiParseResult(AiParseResult aiParseResult);

  @Mapping(target = "transactionDate", source = "result.date")
  @Mapping(target = "paymentModeId", source = "paymentModeId")
  @Mapping(target = "accountId", source = "accountId")
  @Mapping(target = "categoryId", source = "categoryId")
  TransactionRequestDto fromAiParseTask(AiParseResult result, Long paymentModeId, Long accountId, Long categoryId);

  @Mapping(target = "appUser", source = "appUserId", qualifiedByName = "idToAppUser")
  @Mapping(target = "paymentMode", source = "dto.paymentModeId", qualifiedByName = "idToPaymentMode")
  @Mapping(target = "systemCategory", ignore = true)
  @Mapping(target = "userCategory", ignore = true)
  @Mapping(target = "amount", ignore = true)
  @Mapping(target = "account", ignore = true)
  @Mapping(target = "transferId", source = "transferId")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "transactionDate", source = "dto.transactionDate", qualifiedByName = "convertStringToDate")
  void transactionFromRequestDto(TransactionRequestDto dto, @MappingTarget Transaction entity, String appUserId, String transferId, boolean isSourceAccount);

  @AfterMapping
  default void mapCategory(TransactionRequestDto dto, @MappingTarget Transaction entity) {
    if (dto.categoryId() != null) {
      if (dto.categoryId() < 0) {
        entity.setUserCategory(UserCategory.ofId(Math.abs(dto.categoryId())));
        entity.setSystemCategory(null);
      } else {
        entity.setSystemCategory(SystemCategory.ofId(dto.categoryId()));
        entity.setUserCategory(null);
      }
    } else {
      entity.setSystemCategory(null);
      entity.setUserCategory(null);
    }
  }

  @AfterMapping
  default void mapAmountAndAccount(TransactionRequestDto dto, @MappingTarget Transaction entity, boolean isSourceAccount) {

    final var type = TransactionType.valueOf(dto.type());

    // Txn Type = TRANSFER
    if (type == TransactionType.TRANSFER) {
      if (isSourceAccount) {
        entity.setAccount(Account.ofId(dto.accountId()));
        entity.setAmount(-dto.amount());
      } else {
        entity.setAccount(Account.ofId(dto.toAccountId()));
        entity.setAmount(dto.amount());
      }

      return;
    }

    // Txn Type = EXPENSE / INCOME
    entity.setAccount(Account.ofId(dto.accountId()));
    entity.setAmount(type == TransactionType.EXPENSE ? -dto.amount() : dto.amount());
  }

  @Named("idToAppUser")
  default AppUser idToAppUser(String id) {
    return id != null ? AppUser.ofId(id) : null;
  }

  @Named("idToPaymentMode")
  default PaymentMode idToPaymentMode(Long id) {
    return id != null ? PaymentMode.ofId(id) : null;
  }

  @Named("idToAccount")
  default Account idToAccount(Long id) {
    return id != null ? Account.ofId(id) : null;
  }

  @Named("idToSystemCategory")
  default SystemCategory idToSystemCategory(Long id) {
    return id != null ? SystemCategory.ofId(id) : null;
  }

  @Named("idToUserCategory")
  default UserCategory idToUserCategory(Long id) {
    return id != null ? UserCategory.ofId(id) : null;
  }
}
