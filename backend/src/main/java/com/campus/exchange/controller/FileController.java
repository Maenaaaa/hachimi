package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
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
}
