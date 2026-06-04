package com.streamingplatform.service;

import com.streamingplatform.config.JwtUtil;
import com.streamingplatform.dto.AuthResponse;
import com.streamingplatform.dto.LoginRequest;
import com.streamingplatform.dto.UserRegisterRequest;
import com.streamingplatform.entity.User;
import com.streamingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthResponse register(UserRegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User already exists with email: {}", request.getEmail());
            return AuthResponse.builder()
                    .success(false)
                    .message("Email already registered")
                    .build();
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .subscriptionType(User.SubscriptionType.FREE)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail());

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .token(token)
                .expiresIn(jwtExpiration)
                .success(true)
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for email: {}", request.getEmail());
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }

        if (!user.getActive()) {
            log.warn("Inactive user attempted to login: {}", request.getEmail());
            return AuthResponse.builder()
                    .success(false)
                    .message("User account is inactive")
                    .build();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .token(token)
                .expiresIn(jwtExpiration)
                .success(true)
                .message("Login successful")
                .build();
    }
}