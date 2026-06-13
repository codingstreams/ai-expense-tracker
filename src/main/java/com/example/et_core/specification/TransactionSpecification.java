package com.example.et_core.specification;

import com.example.et_core.model.Transaction;
import com.example.et_core.model.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> filterTransactions(
            String appUserId,
            LocalDate startDate,
            LocalDate endDate,
            Double minAmount,
            Double maxAmount,
            List<TransactionType> types,
            List<Long> categoryIds,
            List<Long> accountIds,
            List<Long> paymentModeIds,
            String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory: filter by appUserId to maintain tenant isolation
            predicates.add(cb.equal(root.get("appUser").get("id"), appUserId));

            // Date range filter
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }

            // Amount range filter (absolute amount)
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(cb.abs(root.get("amount")), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(cb.abs(root.get("amount")), maxAmount));
            }

            // Types filter
            if (types != null && !types.isEmpty()) {
                predicates.add(root.get("type").in(types));
            }

            // Category IDs filter (positive for system category, negative for user category)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                List<Long> systemCategoryIds = new ArrayList<>();
                List<Long> userCategoryIds = new ArrayList<>();
                for (Long id : categoryIds) {
                    if (id != null) {
                        if (id > 0) {
                            systemCategoryIds.add(id);
                        } else if (id < 0) {
                            userCategoryIds.add(Math.abs(id));
                        }
                    }
                }

                List<Predicate> categoryPredicates = new ArrayList<>();
                if (!systemCategoryIds.isEmpty()) {
                    categoryPredicates.add(root.get("systemCategory").get("id").in(systemCategoryIds));
                }
                if (!userCategoryIds.isEmpty()) {
                    categoryPredicates.add(root.get("userCategory").get("id").in(userCategoryIds));
                }

                if (!categoryPredicates.isEmpty()) {
                    predicates.add(cb.or(categoryPredicates.toArray(new Predicate[0])));
                }
            }

            // Account IDs filter
            if (accountIds != null && !accountIds.isEmpty()) {
                predicates.add(root.get("account").get("id").in(accountIds));
            }

            // Payment Mode IDs filter
            if (paymentModeIds != null && !paymentModeIds.isEmpty()) {
                predicates.add(root.get("paymentMode").get("id").in(paymentModeIds));
            }

            // Search (case-insensitive description search)
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("description")),
                        "%" + search.trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
