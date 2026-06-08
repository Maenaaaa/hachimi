package com.campus.exchange.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileService {
    String upload(MultipartFile file, String bucket);
    void delete(String objectKey, String bucket);
    String getUrl(String objectKey, String bucket);
    String getGoodsImageUrl(String objectKey);
    String getAvatarUrl(String objectKey);
    InputStream getObject(String bucket, String objectKey);
    
    /**
     * 上传头像并生成多尺寸缩略图
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 相对路径，如 avatars/{userId}/original.webp
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 删除用户的所有头像文件
     * @param userId 用户ID
     */
    void deleteAvatar(Long userId);
}
