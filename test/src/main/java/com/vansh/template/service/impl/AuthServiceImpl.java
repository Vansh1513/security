package com.vansh.template.service.impl;

import com.vansh.template.dto.request.LoginRequest;
import com.vansh.template.dto.request.RegisterRequest;
import com.vansh.template.dto.response.AuthResponse;
import com.vansh.template.entity.User;
import com.vansh.template.enums.Role;
import com.vansh.template.exception.BadRequestException;
import com.vansh.template.mapper.UserMapper;
import com.vansh.template.repository.UserRepository;
import com.vansh.template.security.CustomUserDetails;
import com.vansh.template.security.CustomUserDetailsService;
import com.vansh.template.security.JwtService;
import com.vansh.template.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        log.info(
                "Registration request received for email: {}",
                request.getEmail()
        );

        if (userRepository.existsByEmail(request.getEmail())) {

            log.warn(
                    "Registration failed. Email already exists: {}",
                    request.getEmail()
            );

            throw new BadRequestException(
                    "Email already exists"
            );
        }

        User user = userMapper.toEntity(request);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);

        user = userRepository.save(user);

        log.info(
                "User registered successfully. User ID: {}, Email: {}",
                user.getId(),
                user.getEmail()
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        user.getEmail()
                );

        String jwt = jwtService.generateToken(userDetails);

        log.info(
                "JWT generated successfully for user: {}",
                user.getEmail()
        );

        return AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info(
                "Login attempt for email: {}",
                request.getEmail()
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "User not found"
                        )
                );

        log.info(
                "User authenticated successfully: {}",
                user.getEmail()
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        user.getEmail()
                );

        String jwt = jwtService.generateToken(userDetails);

        log.info(
                "JWT generated successfully for user: {}",
                user.getEmail()
        );

        return AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
