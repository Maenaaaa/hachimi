package com.campus.exchange.service.impl;

import com.campus.exchange.config.AvatarConfig;
import com.campus.exchange.config.MinioConfig;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final AvatarConfig avatarConfig;

    @Override
    @SneakyThrows
    public String upload(MultipartFile file, String bucket) {
        ensureBucketExists(bucket);

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString() + ext;

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        return bucket + "/" + objectName;
    }

    @Override
    @SneakyThrows
    public void delete(String objectKey, String bucket) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete object {} from bucket {}: {}", objectKey, bucket, e.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public String getUrl(String objectKey, String bucket) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(objectKey)
                .expiry(7, TimeUnit.DAYS)
                .build());
    }

    @Override
    public String getGoodsImageUrl(String objectKey) {
        return getUrl(objectKey, minioConfig.getBucketGoods());
    }

    @Override
    public String getAvatarUrl(String objectKey) {
        return getUrl(objectKey, minioConfig.getBucketAvatar());
    }

    @SneakyThrows
    private void ensureBucketExists(String bucket) {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    @Override
    @SneakyThrows
    public InputStream getObject(String bucket, String objectKey) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build());
    }

    @Override
    @SneakyThrows
    public String uploadAvatar(MultipartFile file, Long userId) {
        String bucket = minioConfig.getBucketAvatar();
        ensureBucketExists(bucket);

        // 先读取文件内容到字节数组
        byte[] fileBytes = file.getBytes();
        String originalPath = userId + "/original." + getFileExtension(file.getOriginalFilename());
        
        log.info("上传头像: userId={}, originalPath={}, fileSize={}, contentType={}", 
                userId, originalPath, fileBytes.length, file.getContentType());
        
        // 上传原始文件
        uploadBytes(bucket, originalPath, fileBytes, file.getContentType());

        // 尝试生成缩略图
        try (ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes)) {
            BufferedImage originalImage = ImageIO.read(bis);
            log.info("ImageIO.read result: {}", originalImage != null ? "success (width=" + originalImage.getWidth() + ", height=" + originalImage.getHeight() + ")" : "null");
            
            if (originalImage != null) {
                // 裁剪为正方形
                BufferedImage croppedImage = cropToSquare(originalImage);
                log.info("裁剪完成: size={}", croppedImage.getWidth());

                // 生成缩略图（使用 PNG 格式）
                List<Integer> sizes = avatarConfig.getThumbnailSizes();
                log.info("开始生成缩略图: sizes={}", sizes);
                
                for (Integer size : sizes) {
                    String thumbPath = userId + "/thumb_" + size + ".png";
                    byte[] thumbBytes = generateThumbnail(croppedImage, size);
                    log.info("缩略图生成: path={}, size={}bytes", thumbPath, thumbBytes.length);
                    uploadBytes(bucket, thumbPath, thumbBytes, "image/png");
                }

                // 转换原图为 PNG 格式
                String pngPath = userId + "/original.png";
                byte[] pngBytes = convertToPNG(croppedImage);
                log.info("原图转换完成: path={}, size={}bytes", pngPath, pngBytes.length);
                uploadBytes(bucket, pngPath, pngBytes, "image/png");
                
                // 如果原始文件扩展名不是 .png，删除原始格式文件
                if (!originalPath.equals(pngPath)) {
                    delete(originalPath, bucket);
                }
                
                return "avatars/" + pngPath;
            } else {
                log.warn("ImageIO.read 返回 null，无法生成缩略图");
            }
        } catch (Exception e) {
            log.error("生成缩略图失败", e);
        }

        // 如果缩略图生成失败，直接返回原始文件路径
        return "avatars/" + originalPath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    @Override
    @SneakyThrows
    public void deleteAvatar(Long userId) {
        String bucket = minioConfig.getBucketAvatar();
        String prefix = userId + "/";

        try {
            // 列出用户目录下的所有文件
            ListObjectsArgs listArgs = ListObjectsArgs.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .recursive(true)
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(listArgs);
            List<String> objectsToDelete = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                objectsToDelete.add(item.objectName());
            }

            // 批量删除
            for (String objectName : objectsToDelete) {
                delete(objectName, bucket);
            }
        } catch (Exception e) {
            log.warn("Failed to delete avatar for user {}: {}", userId, e.getMessage());
        }
    }

    private BufferedImage cropToSquare(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int size = Math.min(width, height);

        int x = (width - size) / 2;
        int y = (height - size) / 2;

        return image.getSubimage(x, y, size, size);
    }

    @SneakyThrows
    private byte[] convertToPNG(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(image.getWidth(), image.getHeight())
                .outputFormat("png")
                .toOutputStream(os);
        return os.toByteArray();
    }

    @SneakyThrows
    private byte[] generateThumbnail(BufferedImage image, int size) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(size, size)
                .outputFormat("png")
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
