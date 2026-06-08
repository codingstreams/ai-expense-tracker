package com.example.et_core.dto;

public interface MonthlyCashFlowProjection {
    Integer getYear();
    Integer getMonth();
    Double getIncome();
    Double getExpense();
}
