package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.LoginDTO;
import com.campus.exchange.dto.RefreshTokenDTO;
import com.campus.exchange.dto.RegisterDTO;
import com.campus.exchange.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        return Result.ok(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        return Result.ok();
    }
}
