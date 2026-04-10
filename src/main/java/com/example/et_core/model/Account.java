package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lastFourDigits;
    private Double balance;
    private Long createdAt = System.currentTimeMillis();
    private Long updatedAt;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_name_id")
    private Bank bank;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Card> cardSet;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Transaction> transactionSet;

    public static Account ofId(Long accountId) {
        return Account.builder().id(accountId).build();
    }
}
