package com.example.et_core.dto;

import com.example.et_core.model.CardType;
import com.example.et_core.model.LanguagePreference;

public record OnboardingRequestDto(
    Long bankId,
    String lastFourDigits,
    Double bankBalance,
    CardType cardType,
    String cardLastFourDigits,
    Double cardLimit,
    Double cashBalance,
    Long defaultPaymentModeId,
    LanguagePreference languagePreference
) {}
