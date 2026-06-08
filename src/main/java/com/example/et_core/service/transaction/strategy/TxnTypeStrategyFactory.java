package com.example.et_core.service.transaction.strategy;

import com.example.et_core.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TxnTypeStrategyFactory {
  public Map<TransactionType, TransactionTypeStrategy> strategies;

  @Autowired
  public TxnTypeStrategyFactory(List<TransactionTypeStrategy> strategies) {
    this.strategies = strategies
        .stream()
        .collect(Collectors.toMap(TransactionTypeStrategy::getType, e -> e));
  }

  public TransactionTypeStrategy getStrategy(TransactionType type) {
    return strategies.get(type);
  }
}
