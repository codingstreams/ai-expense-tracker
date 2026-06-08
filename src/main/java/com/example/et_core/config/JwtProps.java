package com.example.et_core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProps {
  private String secretKey;
  private long expirationTimeAccessToken;
}
