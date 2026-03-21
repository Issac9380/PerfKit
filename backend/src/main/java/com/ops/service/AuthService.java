package com.ops.service;

import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;

public interface AuthService {
    String login(LoginRequest request);
    void register(RegisterRequest request);
}
