package com.example.et_core.mapper;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.model.*;
import com.example.et_core.service.ai.AiParseResult;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
  TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

  @Mapping(target = "transactionId", source = "id")
  TransactionDto transactionDtoToTransactionDto(Transaction transaction);

  List<TransactionDto> transactionDtosToTransactionDtos(List<Transaction> transactions);

  TransactionRequestDto fromAiParseResult(AiParseResult aiParseResult);

  @Mapping(target = "transactionDate", source = "result.date")
  @Mapping(target = "paymentModeId", source = "paymentModeId")
  @Mapping(target = "accountId", source = "accountId")
  @Mapping(target = "categoryId", source = "categoryId")
  TransactionRequestDto fromAiParseTask(AiParseResult result, Long paymentModeId, Long accountId, Long categoryId);

  @Mapping(target = "appUser", source = "appUserId", qualifiedByName = "idToAppUser")
  @Mapping(target = "paymentMode", source = "dto.paymentModeId", qualifiedByName = "idToPaymentMode")
//  @Mapping(target = "account", source = "dto.accountId", qualifiedByName = "idToAccount")
  @Mapping(target = "category", source = "dto.categoryId", qualifiedByName = "idToCategory")
//  @Mapping(target = "amount", source = "dto", qualifiedByName = "mapAmount")
  @Mapping(target = "amount", ignore = true)
  @Mapping(target = "account", ignore = true)
  @Mapping(target = "transferId", source = "transferId")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void transactionFromRequestDto(TransactionRequestDto dto, @MappingTarget Transaction entity, String appUserId, String transferId, boolean isSourceAccount);

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

  @Named("idToCategory")
  default Category idToCategory(Long id) {
    return id != null ? Category.ofId(id) : null;
  }
}
