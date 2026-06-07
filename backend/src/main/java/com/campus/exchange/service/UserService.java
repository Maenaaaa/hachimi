package com.campus.exchange.service;

import com.campus.exchange.dto.ChangePasswordDTO;
import com.campus.exchange.dto.RealNameVerifyDTO;
import com.campus.exchange.dto.UpdateProfileDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.vo.UserProfileVO;
import com.campus.exchange.vo.UserPublicVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileVO getProfile(Long userId);
    UserPublicVO getPublicProfile(Long userId);
    UserProfileVO updateProfile(Long userId, UpdateProfileDTO dto);
    void changePassword(Long userId, ChangePasswordDTO dto);
    String uploadAvatar(Long userId, MultipartFile file);
    void submitVerification(Long userId, RealNameVerifyDTO dto, MultipartFile idCardImage);
}
