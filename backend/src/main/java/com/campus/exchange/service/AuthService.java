package com.campus.exchange.service;

import com.campus.exchange.dto.LoginDTO;
import com.campus.exchange.dto.RefreshTokenDTO;
import com.campus.exchange.dto.RegisterDTO;

import java.util.Map;

public interface AuthService {
    Map<String, String> login(LoginDTO dto);
    Map<String, String> register(RegisterDTO dto);
    Map<String, String> refresh(RefreshTokenDTO dto);
    void logout(String refreshToken);
}
