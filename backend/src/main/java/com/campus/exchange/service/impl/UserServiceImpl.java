package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.dto.ChangePasswordDTO;
import com.campus.exchange.dto.RealNameVerifyDTO;
import com.campus.exchange.dto.UpdateProfileDTO;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.FileService;
import com.campus.exchange.service.UserService;
import com.campus.exchange.vo.UserProfileVO;
import com.campus.exchange.vo.UserPublicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final FollowMapper followMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setSchool(user.getSchool());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreditScore(user.getCreditScore());
        vo.setGoodsCount(goodsMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, userId).eq(Goods::getDeleted, 0)).intValue());
        vo.setOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().and(w ->
                        w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId))).intValue());
        vo.setFollowerCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, userId)).intValue());
        vo.setFollowingCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId)).intValue());
        vo.setIsVerified(user.getRealName() != null && !user.getRealName().isEmpty());
        return vo;
    }

    @Override
    public UserPublicVO getPublicProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        UserPublicVO vo = new UserPublicVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setSchool(user.getSchool());
        vo.setCreditScore(user.getCreditScore());
        vo.setRealName(user.getRealName());
        vo.setGoodsCount(goodsMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, userId).eq(Goods::getStatus, "ACTIVE")).intValue());
        vo.setFollowerCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, userId)).intValue());
        vo.setFollowingCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId)).intValue());
        return vo;
    }

    @Override
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserProfileVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getSchool() != null) user.setSchool(dto.getSchool());
        userMapper.updateById(user);
        return getProfile(userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("文件为空");
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.contains("jpeg") && !contentType.contains("png") && !contentType.contains("webp"))) {
            throw new BusinessException("仅支持 JPG/PNG/WebP 格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过 5MB");
        }
        
        // 删除旧头像
        User user = userMapper.selectById(userId);
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            fileService.deleteAvatar(userId);
        }
        
        // 上传新头像（返回相对路径）
        String avatarPath = fileService.uploadAvatar(file, userId);
        
        // 更新数据库
        user.setAvatar(avatarPath);
        userMapper.updateById(user);
        return avatarPath;
    }

    @Override
    public void submitVerification(Long userId, RealNameVerifyDTO dto, MultipartFile idCardImage) {
        long pendingCount = userAuthMapper.selectCount(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getUserId, userId)
                        .eq(UserAuth::getStatus, "PENDING"));
        if (pendingCount > 0) {
            throw new BusinessException("已有待审核的实名认证申请");
        }

        String imageUrl = fileService.upload(idCardImage, "verification");

        UserAuth auth = new UserAuth();
        auth.setUserId(userId);
        auth.setRealName(dto.getRealName());
        auth.setStudentId(dto.getStudentId());
        auth.setIdCardImage(imageUrl);
        auth.setStatus("PENDING");
        userAuthMapper.insert(auth);

        User user = userMapper.selectById(userId);
        user.setRealName(dto.getRealName());
        user.setStudentId(dto.getStudentId());
        userMapper.updateById(user);
    }
}
