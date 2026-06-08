package com.campus.exchange.service.impl;

import com.campus.exchange.config.MinioConfig;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

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

        return getUrl(objectName, bucket);
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
}
