package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.ChangePasswordDTO;
import com.campus.exchange.dto.RealNameVerifyDTO;
import com.campus.exchange.dto.UpdateProfileDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.UserService;
import com.campus.exchange.vo.UserProfileVO;
import com.campus.exchange.vo.UserPublicVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(@CurrentUser User user) {
        return Result.ok(userService.getProfile(user.getId()));
    }

    @GetMapping("/profile/{id}")
    public Result<UserPublicVO> getPublicProfile(@PathVariable Long id) {
        return Result.ok(userService.getPublicProfile(id));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@CurrentUser User user, @RequestBody UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(user.getId(), dto));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@CurrentUser User user, @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(user.getId(), dto);
        return Result.ok();
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@CurrentUser User user, @RequestParam("file") MultipartFile file) {
        return Result.ok(userService.uploadAvatar(user.getId(), file));
    }

    @PostMapping("/verify")
    public Result<Void> submitVerification(@CurrentUser User user,
                                            @Valid @RequestPart("dto") RealNameVerifyDTO dto,
                                            @RequestPart("image") MultipartFile image) {
        userService.submitVerification(user.getId(), dto, image);
        return Result.ok();
    }
}
