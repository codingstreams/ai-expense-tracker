package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(indexes = {
    @Index(name = "idx_transaction_app_user_id", columnList = "app_user_id"),
    @Index(name = "idx_transaction_date_user", columnList = "app_user_id, transaction_date")
})
public class Transaction extends TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Double amount;
    private LocalDate transactionDate;
    private String transferId;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_category_id")
    private SystemCategory systemCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_category_id")
    private UserCategory userCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_mode_id")
    private PaymentMode paymentMode;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private Set<AiParsingTask> aiParsingTaskSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    /**
     * Returns the effective category name from whichever category is set.
     * UserCategory takes precedence over SystemCategory.
     */
    @Transient
    public String getCategoryName() {
        if (userCategory != null) return userCategory.getName();
        if (systemCategory != null) return systemCategory.getName();
        return null;
    }

    /**
     * Returns the effective category ID. UserCategory IDs are returned as negative
     * to distinguish from SystemCategory IDs in the API layer.
     */
    @Transient
    public Long getCategoryId() {
        if (userCategory != null) return -userCategory.getId();
        if (systemCategory != null) return systemCategory.getId();
        return null;
    }
}
