package com.campus.exchange.ai.mimo;

import com.campus.exchange.ai.AiGateway;
import com.campus.exchange.ai.AiJudgment;
import com.campus.exchange.ai.AiJudgmentType;
import com.campus.exchange.ai.AiVerdict;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiMoGateway implements AiGateway {

    private final MiMoConfig config;
    private final ObjectMapper objectMapper;
    private final MinioClient minioClient;

    @Override
    public AiJudgment analyze(AiJudgmentType type, String prompt) {
        return analyze(type, prompt, List.of());
    }

    @Override
    public AiJudgment analyzeWithImages(AiJudgmentType type, String prompt, List<String> imageUrls) {
        return analyze(type, prompt, imageUrls);
    }

    private AiJudgment analyze(AiJudgmentType type, String prompt, List<String> imageUrls) {
        int maxRetries = config.getRetryCount() > 0 ? config.getRetryCount() : 2;
        long retryDelay = config.getRetryDelayMs() > 0 ? config.getRetryDelayMs() : 2000;

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return doAnalyze(type, prompt, imageUrls);
            } catch (MiMoServiceException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn("MiMo API 调用失败 (第{}/{}次), {}ms后重试: {}", attempt, maxRetries, retryDelay, e.getMessage());
                    sleep(retryDelay);
                    retryDelay = Math.min(retryDelay * 2, 10000);
                } else {
                    log.error("MiMo API 调用失败，已重试{}次，放弃", maxRetries, e);
                }
            }
        }

        if (lastException != null) {
            throw (lastException instanceof MiMoServiceException) ? (MiMoServiceException) lastException : new MiMoServiceException("AI 服务调用失败", lastException);
        }
        throw new MiMoServiceException("AI 服务调用失败: 未知错误");
    }

    private AiJudgment doAnalyze(AiJudgmentType type, String prompt, List<String> imageUrls) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = config.getBaseUrl() + "/chat/completions";

            Object userContent;
            if (imageUrls != null && !imageUrls.isEmpty()) {
                List<Map<String, Object>> contentParts = new ArrayList<>();
                contentParts.add(Map.of("type", "text", "text", prompt));
                for (String imageUrl : imageUrls) {
                    String base64Data = downloadAndEncodeBase64(imageUrl);
                    if (base64Data != null) {
                        String mediaType = getMediaType(imageUrl);
                        contentParts.add(Map.of(
                                "type", "image_url",
                                "image_url", Map.of("url", "data:" + mediaType + ";base64," + base64Data)
                        ));
                    }
                }
                userContent = contentParts;
            } else {
                userContent = prompt;
            }

            Map<String, Object> requestBody = Map.of(
                    "model", config.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", getSystemPrompt(type)),
                            Map.of("role", "user", "content", userContent)
                    ),
                    "max_completion_tokens", config.getMaxTokens(),
                    "temperature", config.getTemperature(),
                    "stream", false
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            return parseResponse(response.getBody());

        } catch (ResourceAccessException e) {
            log.error("MiMo API 调用超时或不可达", e);
            throw new MiMoServiceException("AI 服务暂不可用", e);
        } catch (MiMoServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("MiMo API 调用异常", e);
            throw new MiMoServiceException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String downloadAndEncodeBase64(String imageUrl) {
        try {
            String objectKey = extractObjectKey(imageUrl);
            String bucket = extractBucket(imageUrl);

            if (objectKey == null || bucket == null) {
                log.warn("无法解析图片 URL: {}", imageUrl);
                return null;
            }

            InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());

            byte[] imageBytes = is.readAllBytes();
            is.close();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            return null;
        }
    }

    private String extractObjectKey(String url) {
        try {
            String path = url;
            if (path.startsWith("/")) path = path.substring(1);
            if (path.contains("goods-images/")) {
                return path.substring(path.indexOf("goods-images/") + 13).split("\\?")[0];
            } else if (path.contains("avatars/")) {
                return path.substring(path.indexOf("avatars/") + 8).split("\\?")[0];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractBucket(String url) {
        String path = url;
        if (path.startsWith("/")) path = path.substring(1);
        if (path.contains("goods-images/")) return "goods-images";
        if (path.contains("avatars/")) return "avatars";
        if (path.contains("verification/")) return "verification";
        if (path.contains("chat-images/")) return "chat-images";
        return "goods-images";
    }

    private String getMediaType(String url) {
        if (url.toLowerCase().endsWith(".png")) return "image/png";
        if (url.toLowerCase().endsWith(".gif")) return "image/gif";
        if (url.toLowerCase().endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private RestTemplate createRestTemplate() {
        int timeout = config.getTimeout() > 0 ? config.getTimeout() : 30000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    private String getSystemPrompt(AiJudgmentType type) {
        return switch (type) {
            case REPORT -> "你是一个校园交易平台的AI审核员，负责分析举报内容并给出判决。请严格按照要求的JSON格式返回结果。";
            case DISPUTE -> "你是一个校园交易平台的AI仲裁员，负责分析订单争议并决定是否取消订单。请严格按照要求的JSON格式返回结果。";
            case GOODS_REVIEW -> "你是一个校园交易平台的AI审核员，负责审核商品信息的合规性。请严格按照要求的JSON格式返回结果。";
        };
    }

    private AiJudgment parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new MiMoServiceException("AI 响应格式异常: 无 choices");
            }

            String content = choices.get(0).get("message").get("content").asText();
            return parseAiContent(content);

        } catch (MiMoServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 AI 响应失败", e);
            throw new MiMoServiceException("解析 AI 响应失败: " + e.getMessage(), e);
        }
    }

    private AiJudgment parseAiContent(String content) {
        try {
            if (content == null || content.isBlank()) {
                return AiJudgment.builder()
                        .verdict(AiVerdict.ESCALATED)
                        .confidence(0.0)
                        .reasoning("AI 返回空响应，转人工处理")
                        .evidence(List.of("空响应"))
                        .build();
            }

            String trimmedContent = content.trim();
            
            // 处理 markdown 代码块包裹的 JSON
            String jsonContent = trimmedContent;
            if (trimmedContent.contains("```json")) {
                int start = trimmedContent.indexOf("```json") + 7;
                int end = trimmedContent.lastIndexOf("```");
                if (end > start) {
                    jsonContent = trimmedContent.substring(start, end).trim();
                }
            } else if (trimmedContent.contains("```")) {
                int start = trimmedContent.indexOf("```") + 3;
                int end = trimmedContent.lastIndexOf("```");
                if (end > start) {
                    jsonContent = trimmedContent.substring(start, end).trim();
                }
            }

            // 检查是否为 JSON 格式
            if (!jsonContent.startsWith("{") && !jsonContent.startsWith("[")) {
                log.warn("AI 返回非 JSON 响应: {}", content);
                return AiJudgment.builder()
                        .verdict(AiVerdict.REJECTED)
                        .confidence(0.95)
                        .reasoning("内容被AI安全过滤器拦截，判定为不合格: " + content)
                        .evidence(List.of("安全合规拦截: " + content))
                        .build();
            }

            JsonNode node = objectMapper.readTree(jsonContent);

            String verdictStr = node.get("verdict").asText();
            AiVerdict verdict = AiVerdict.valueOf(verdictStr);

            double confidence = node.get("confidence").asDouble();
            String reasoning = node.get("reasoning").asText();

            List<String> evidence = new ArrayList<>();
            JsonNode evidenceNode = node.get("evidence");
            if (evidenceNode != null && evidenceNode.isArray()) {
                evidenceNode.forEach(e -> evidence.add(e.asText()));
            }

            return AiJudgment.builder()
                    .verdict(verdict)
                    .confidence(confidence)
                    .reasoning(reasoning)
                    .evidence(evidence)
                    .build();

        } catch (Exception e) {
            log.error("解析 AI 判决内容失败: {}", content, e);
            return AiJudgment.builder()
                    .verdict(AiVerdict.ESCALATED)
                    .confidence(0.0)
                    .reasoning("AI 响应解析失败，转人工处理")
                    .evidence(List.of("响应解析异常"))
                    .build();
        }
    }
}
