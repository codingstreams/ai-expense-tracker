package com.example.et_core.config;

import com.example.et_core.security.BearerAuthProvider;
import com.example.et_core.security.JwtAuthFilter;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
      JwtAuthFilter jwtAuthFilter,
      AuthenticationEntryPoint authenticationEntryPoint,
      UrlBasedCorsConfigurationSource corsConfig) {

    httpSecurity
        .cors(cors -> cors.configurationSource(corsConfig))
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(http -> http
            .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
            .requestMatchers("/api/auth/**", "/error")
            .permitAll()
            .anyRequest()
            .authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint));

    return httpSecurity.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(
      @Qualifier("daoAuthenticationProvider") AuthenticationProvider daoAuthenticationProvider,
      @Qualifier("bearerAuthProvider") BearerAuthProvider bearerAuthProvider) {
    final List<AuthenticationProvider> authenticationProvider = List.of(daoAuthenticationProvider, bearerAuthProvider);
    final var providerManager = new ProviderManager(authenticationProvider);
    return providerManager;
  }

  @Bean("daoAuthenticationProvider")
  AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    final var authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  SecretKey secretKey(JwtProps jwtProps) {
    return Keys.hmacShaKeyFor(jwtProps.getSecretKey()
        .getBytes(StandardCharsets.UTF_8));
  }
}
