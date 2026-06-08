package com.example.et_core.dto;

import java.time.LocalDate;

public interface DailyCashFlowProjection {
    LocalDate getTransactionDate();
    Double getIncome();
    Double getExpense();
}
