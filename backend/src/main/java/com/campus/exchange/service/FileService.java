package com.campus.exchange.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String upload(MultipartFile file, String bucket);
    void delete(String objectKey, String bucket);
    String getUrl(String objectKey, String bucket);
    String getGoodsImageUrl(String objectKey);
    String getAvatarUrl(String objectKey);
}
