package com.example.et_core.service.userconfig;

import com.example.et_core.dto.UserConfigDto;
import com.example.et_core.model.LanguagePreference;
import com.example.et_core.model.PaymentMode;
import com.example.et_core.model.UserConfig;
import com.example.et_core.repo.UserConfigRepo;
import com.example.et_core.service.paymentmode.PaymentModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConfigServiceImpl implements UserConfigService {
  private final UserConfigRepo userConfigRepo;
  private final PaymentModeService paymentModeService;

  @Override
  public UserConfig getByUserId(String appUserId) {
    return userConfigRepo.findByAppUserId(appUserId)
        .orElseThrow(() -> new RuntimeException("UserConfig not found for appUserId:" + appUserId));
  }

  @Override
  public UserConfigDto getConfig(String userId) {
    UserConfig config = getByUserId(userId);
    String languageStr = mapLanguageToString(config.getLanguagePreference());
    Long defaultPaymentModeId = config.getDefaultPaymentMode() != null ? config.getDefaultPaymentMode().getId() : null;
    return new UserConfigDto(languageStr, defaultPaymentModeId);
  }

  @Override
  @Transactional
  public UserConfigDto updateConfig(String userId, UserConfigDto dto) {
    UserConfig config = getByUserId(userId);

    if (dto.language() != null) {
      config.setLanguagePreference(mapStringToLanguage(dto.language()));
    }

    if (dto.defaultPaymentModeId() != null) {
      PaymentMode paymentMode = paymentModeService.get(dto.defaultPaymentModeId());
      config.setDefaultPaymentMode(paymentMode);
    }

    config.setUpdatedAt(System.currentTimeMillis());
    UserConfig saved = userConfigRepo.save(config);

    String languageStr = mapLanguageToString(saved.getLanguagePreference());
    Long defaultPaymentModeId = saved.getDefaultPaymentMode() != null ? saved.getDefaultPaymentMode().getId() : null;
    return new UserConfigDto(languageStr, defaultPaymentModeId);
  }

  private String mapLanguageToString(LanguagePreference pref) {
    if (pref == null) return "English";
    return switch (pref) {
      case ENGLISH -> "English";
      case HINDI -> "Hindi";
    };
  }

  private LanguagePreference mapStringToLanguage(String lang) {
    if (lang == null) return LanguagePreference.ENGLISH;
    try {
      return LanguagePreference.valueOf(lang.toUpperCase());
    } catch (IllegalArgumentException e) {
      if ("Hindi".equalsIgnoreCase(lang)) {
        return LanguagePreference.HINDI;
      }
      return LanguagePreference.ENGLISH;
    }
  }
}
