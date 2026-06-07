package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.dto.LoginDTO;
import com.campus.exchange.dto.RefreshTokenDTO;
import com.campus.exchange.dto.RegisterDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.security.JwtUtil;
import com.campus.exchange.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Map<String, String> login(LoginDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (user == null || "DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    @Override
    public Map<String, String> register(RegisterDTO dto) {
        if (userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), "USER");
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    @Override
    public Map<String, String> refresh(RefreshTokenDTO dto) {
        if (!jwtUtil.validateToken(dto.getRefreshToken()) || !jwtUtil.isRefreshToken(dto.getRefreshToken())) {
            throw new BusinessException(401, "无效的refreshToken");
        }

        Long userId = jwtUtil.getUserIdFromToken(dto.getRefreshToken());
        String storedToken = redisTemplate.opsForValue().get("refresh:" + userId);
        if (storedToken == null || !storedToken.equals(dto.getRefreshToken())) {
            throw new BusinessException(401, "refreshToken已失效，请重新登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null || "DISABLED".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set("refresh:" + user.getId(), newRefreshToken, 7, TimeUnit.DAYS);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        return tokens;
    }

    @Override
    public void logout(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            redisTemplate.delete("refresh:" + userId);
        }
    }
}
