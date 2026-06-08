package com.example.et_core.model;

import com.example.et_core.service.transaction.TransactionBehavior;
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
public class PaymentMode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @OneToMany(mappedBy = "defaultPaymentMode", fetch = FetchType.LAZY)
  private Set<UserConfig> userConfigSet;

  @OneToMany(mappedBy = "paymentMode", fetch = FetchType.LAZY)
  private Set<Transaction> transactionSet;

  @Enumerated(EnumType.STRING)
  private TransactionBehavior type;

  public static PaymentMode ofId(Long paymentModeId) {
    return PaymentMode.builder().id(paymentModeId).build();
  }
}
