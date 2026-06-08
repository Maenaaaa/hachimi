package com.campus.exchange.migration;

import com.campus.exchange.config.AvatarConfig;
import com.campus.exchange.config.MinioConfig;
import com.campus.exchange.entity.User;
import com.campus.exchange.mapper.UserMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 头像迁移脚本
 * 为现有用户生成缩略图
 * 
 * 使用方法：启动应用时添加参数 --migrate.avatar
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarMigration implements CommandLineRunner {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final AvatarConfig avatarConfig;
    private final UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        boolean shouldMigrate = false;
        for (String arg : args) {
            if ("--migrate.avatar".equals(arg)) {
                shouldMigrate = true;
                break;
            }
        }

        if (!shouldMigrate) {
            return;
        }

        log.info("开始头像迁移...");
        String bucket = minioConfig.getBucketAvatar();

        // 确保bucket存在
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }

        // 获取所有有头像的用户
        List<User> users = userMapper.selectList(null);
        int successCount = 0;
        int failCount = 0;

        for (User user : users) {
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                continue;
            }

            try {
                migrateUserAvatar(user, bucket);
                successCount++;
                log.info("迁移成功: userId={}, username={}", user.getId(), user.getUsername());
            } catch (Exception e) {
                failCount++;
                log.error("迁移失败: userId={}, username={}, error={}", user.getId(), user.getUsername(), e.getMessage());
            }
        }

        log.info("头像迁移完成: 成功={}, 失败={}", successCount, failCount);
    }

    @SneakyThrows
    private void migrateUserAvatar(User user, String bucket) {
        String avatarPath = user.getAvatar();
        
        // 如果已经是新格式（avatars/{userId}/original.webp），跳过
        if (avatarPath.startsWith("avatars/" + user.getId() + "/")) {
            log.debug("用户 {} 已经是新格式，跳过", user.getId());
            return;
        }

        // 从旧路径中提取objectKey
        String objectKey;
        if (avatarPath.startsWith("avatars/")) {
            objectKey = avatarPath.substring("avatars/".length());
        } else if (avatarPath.contains("/avatars/")) {
            objectKey = avatarPath.substring(avatarPath.indexOf("/avatars/") + "/avatars/".length());
        } else {
            log.warn("无法解析用户 {} 的头像路径: {}", user.getId(), avatarPath);
            return;
        }

        // 获取原始图片
        InputStream is;
        try {
            is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.warn("无法获取用户 {} 的原始头像: {}", user.getId(), e.getMessage());
            return;
        }

        // 读取图片
        BufferedImage originalImage = ImageIO.read(is);
        if (originalImage == null) {
            log.warn("无法读取用户 {} 的头像图片", user.getId());
            return;
        }

        // 裁剪为正方形
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        BufferedImage croppedImage = originalImage.getSubimage(x, y, size, size);

        // 生成原图（WebP格式）
        String basePath = user.getId() + "/original.webp";
        byte[] originalBytes = convertToWebP(croppedImage, 1.0);
        uploadBytes(bucket, basePath, originalBytes, "image/webp");

        // 生成缩略图
        for (Integer thumbSize : avatarConfig.getThumbnailSizes()) {
            String thumbPath = user.getId() + "/thumb_" + thumbSize + ".webp";
            byte[] thumbBytes = generateThumbnail(croppedImage, thumbSize);
            uploadBytes(bucket, thumbPath, thumbBytes, "image/webp");
        }

        // 更新数据库
        String newAvatarPath = "avatars/" + basePath;
        user.setAvatar(newAvatarPath);
        userMapper.updateById(user);

        // 删除旧文件（可选）
        // deleteOldFile(bucket, objectKey);
    }

    @SneakyThrows
    private byte[] convertToWebP(BufferedImage image, double quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(image.getWidth(), image.getHeight())
                .outputFormat("webp")
                .outputQuality(quality)
                .toOutputStream(os);
        return os.toByteArray();
    }

    @SneakyThrows
    private byte[] generateThumbnail(BufferedImage image, int size) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(size, size)
                .outputFormat("webp")
                .outputQuality(avatarConfig.getQuality())
                .toOutputStream(os);
        return os.toByteArray();
    }

    @SneakyThrows
    private void uploadBytes(String bucket, String objectName, byte[] data, String contentType) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(is, data.length, -1)
                .contentType(contentType)
                .build());
    }
}
