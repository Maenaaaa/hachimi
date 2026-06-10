package com.campus.exchange.ai.mimo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.mimo")
public class MiMoConfig {

    private String apiKey;
    private String baseUrl;
    private String model;
    private int maxTokens;
    private double temperature;
    private int timeout;
    private int retryCount;
    private long retryDelayMs;
}
