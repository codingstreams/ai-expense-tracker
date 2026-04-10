package com.example.et_core.mapper;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.UpdateTransactionDto;
import com.example.et_core.model.*;
import com.example.et_core.repo.PaymentModeRepo;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionDto transactionDtoToTransactionDto(Transaction transaction);

    List<TransactionDto> transactionDtosToTransactionDtos(List<Transaction> transactions);

    @Mapping(target = "appUser", source = "appUserId", qualifiedByName = "idToAppUser")
    @Mapping(target = "paymentMode", source = "dto.paymentModeId", qualifiedByName = "idToPaymentMode")
    @Mapping(target = "account", source = "dto.accountId", qualifiedByName = "idToAccount")
    @Mapping(target = "category", source = "dto.categoryId", qualifiedByName = "idToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTransactionFromDto(UpdateTransactionDto dto, @MappingTarget Transaction entity, String appUserId);

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
