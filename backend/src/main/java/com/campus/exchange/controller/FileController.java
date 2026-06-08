package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(defaultValue = "goods-images") String bucket) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.contains("jpeg") && !contentType.contains("png")
                && !contentType.contains("webp") && !contentType.contains("jpg"))) {
            return Result.fail("仅支持 JPG/PNG/WebP 格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("图片大小不能超过 5MB");
        }
        return Result.ok(fileService.upload(file, bucket));
    }

    @GetMapping("/file/{bucket}/{objectKey}")
    public ResponseEntity<InputStreamResource> proxyFile(@PathVariable String bucket,
                                                          @PathVariable String objectKey) {
        try {
            objectKey = URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
            InputStream is = fileService.getObject(bucket, objectKey);
            String contentType = "image/png";
            if (objectKey.endsWith(".jpg") || objectKey.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (objectKey.endsWith(".webp")) {
                contentType = "image/webp";
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(new InputStreamResource(is));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
