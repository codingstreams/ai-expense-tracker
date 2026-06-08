package com.example.et_core.service.appuser;

import com.example.et_core.dto.RegisterRequest;
import com.example.et_core.exception.UserAlreadyExistsException;
import com.example.et_core.model.AppUser;
import com.example.et_core.repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppUserService{
  private final AppUserRepo appUserRepo;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void registerUser(RegisterRequest request) {
    // Check whether user exists or not
    final var exists = appUserRepo.existsByEmail(request.email());

    if(exists) throw new UserAlreadyExistsException("Email/User: %s already present.".formatted(request.email()));

    var newUser = AppUser.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .build();

    appUserRepo.save(newUser);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final var appUser = appUserRepo.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("Username: %s not found.".formatted(username)));
    return new User(appUser.getId(), appUser.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
