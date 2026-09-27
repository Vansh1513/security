package com.vansh.template.service;

import com.vansh.template.dto.request.LoginRequest;
import com.vansh.template.dto.request.RegisterRequest;
import com.vansh.template.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
