package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(indexes = @Index(name = "idx_card_app_user_id", columnList = "app_user_id"))
public class Card extends TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    private String lastFourDigits;
    private Double creditLimit;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}
