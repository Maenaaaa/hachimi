package com.campus.exchange.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "avatar")
public class AvatarConfig {
    private List<Integer> thumbnailSizes;
    private double quality;
    private String maxFileSize;
}