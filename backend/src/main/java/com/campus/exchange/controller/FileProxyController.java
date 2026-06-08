package com.campus.exchange.controller;

import com.campus.exchange.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileProxyController {

    private final FileService fileService;

    @GetMapping("/**")
    public ResponseEntity<InputStreamResource> proxyFile(HttpServletRequest request) {
        try {
            // 从请求URI中提取完整路径
            String requestUri = request.getRequestURI();
            log.info("File proxy request: requestUri={}", requestUri);
            
            String fullPath = requestUri.replace("/api/file/", "");
            fullPath = URLDecoder.decode(fullPath, StandardCharsets.UTF_8);
            
            log.info("File proxy decoded fullPath={}", fullPath);
            
            // 分离 bucket 和 objectKey
            int firstSlash = fullPath.indexOf('/');
            if (firstSlash == -1) {
                log.warn("No slash found in fullPath: {}", fullPath);
                return ResponseEntity.badRequest().build();
            }
            
            String bucket = fullPath.substring(0, firstSlash);
            String objectKey = fullPath.substring(firstSlash + 1);
            
            log.info("File proxy: bucket={}, objectKey={}", bucket, objectKey);
            
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
            log.error("Proxy file error: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
