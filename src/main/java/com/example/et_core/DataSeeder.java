package com.example.et_core;

import com.example.et_core.model.Bank;
import com.example.et_core.model.PaymentMode;
import com.example.et_core.model.SystemCategory;
import com.example.et_core.repo.BankRepo;
import com.example.et_core.repo.PaymentModeRepo;
import com.example.et_core.repo.SystemCategoryRepo;
import com.example.et_core.service.transaction.TransactionBehavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

  private final PaymentModeRepo paymentModeRepo;
  private final BankRepo bankRepo;
  private final SystemCategoryRepo systemCategoryRepo;

  @Override
  public void run(String... args) throws Exception {
    log.info("Starting database seeding...");

    seedPaymentModes();
    seedBanks();
    seedSystemCategories();

    log.info("Database seeding completed.");
  }

  private void seedPaymentModes() {
    Map<String, TransactionBehavior> paymentModes = Map.of(
        "Credit Card", TransactionBehavior.LIABILITY,
        "Debit Card", TransactionBehavior.ASSET,
        "Net Banking", TransactionBehavior.ASSET,
        "UPI", TransactionBehavior.ASSET,
        "Cash", TransactionBehavior.ASSET,
        "Digital Wallet", TransactionBehavior.ASSET
    );

    paymentModes.forEach((name, type) -> {
      if (paymentModeRepo.findByName(name).isEmpty()) {
        paymentModeRepo.save(PaymentMode.builder()
            .name(name)
            .type(type)
            .build());
        log.info("Seeded payment mode: {} ({})", name, type);
      }
    });
  }

  private void seedBanks() {
    List<String> banks = List.of(
        "State Bank of India",
        "HDFC Bank",
        "ICICI Bank",
        "Axis Bank",
        "Punjab National Bank",
        "Bank of Baroda",
        "Canara Bank",
        "Central Bank of India",
        "Union Bank of India",
        "IndusInd Bank",
        "Kotak Mahindra Bank"
    );

    banks.forEach(name -> {
      if (bankRepo.findByName(name).isEmpty()) {
        bankRepo.save(Bank.builder()
            .name(name)
            .build());
        log.info("Seeded bank: {}", name);
      }
    });
  }

  private void seedSystemCategories() {
    List<String> categories = List.of(
        "Groceries",
        "Dining Out",
        "Rent/EMI",
        "Utilities (Electricity/Water)",
        "Fuel/Transportation",
        "Health & Medical",
        "Insurance",
        "Shopping (Clothing/Electronics)",
        "Entertainment & OTT",
        "Education",
        "Investments (SIP/Stocks)",
        "Gifts & Donations",
        "Travel & Vacation",
        "Maintenance & Repairs",
        "Miscellaneous"
    );

    categories.forEach(name -> {
      if (systemCategoryRepo.findByName(name).isEmpty()) {
        systemCategoryRepo.save(SystemCategory.builder()
            .name(name)
            .build());
        log.info("Seeded system category: {}", name);
      }
    });
  }
}
