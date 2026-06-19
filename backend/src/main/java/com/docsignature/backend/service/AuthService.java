package com.docsignature.backend.service;

import com.docsignature.backend.domain.ActorType;
import com.docsignature.backend.domain.Role;
import com.docsignature.backend.domain.UserEntity;
import com.docsignature.backend.dto.AuthDtos;
import com.docsignature.backend.exception.ApiException;
import com.docsignature.backend.repository.UserRepository;
import com.docsignature.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }

        UserEntity user = new UserEntity();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.OWNER);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);
        return authResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return authResponse(user);
    }

    public AuthDtos.UserResponse me(String email) {
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return new AuthDtos.UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    public UserEntity requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private AuthDtos.AuthResponse authResponse(UserEntity user) {
        return new AuthDtos.AuthResponse(jwtService.generateToken(user.getEmail()), new AuthDtos.UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name()));
    }
}
