package com.example.et_core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                          JwtAuthFilter jwtAuthFilter,
                                          AuthenticationEntryPoint authenticationEntryPoint) {

    httpSecurity.csrf(CsrfConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(http -> http
            .requestMatchers("/api/auth/**")
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
  AuthenticationManager authenticationManager(@Qualifier("daoAuthenticationProvider") AuthenticationProvider daoAuthenticationProvider,
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
