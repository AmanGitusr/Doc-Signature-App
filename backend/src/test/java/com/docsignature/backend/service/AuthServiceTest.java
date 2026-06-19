package com.docsignature.backend.service;

import com.docsignature.backend.domain.Role;
import com.docsignature.backend.domain.UserEntity;
import com.docsignature.backend.dto.AuthDtos;
import com.docsignature.backend.repository.UserRepository;
import com.docsignature.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks AuthService authService;

    @Test
    void registerCreatesUserAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(jwtService.generateToken("alice@example.com")).thenReturn("jwt-token");

        AuthDtos.AuthResponse response = authService.register(new AuthDtos.RegisterRequest("Alice", "alice@example.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    void loginAuthenticatesWithCredentials() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setFullName("Alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.OWNER);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("alice@example.com")).thenReturn("jwt-token");

        AuthDtos.AuthResponse response = authService.login(new AuthDtos.LoginRequest("alice@example.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
