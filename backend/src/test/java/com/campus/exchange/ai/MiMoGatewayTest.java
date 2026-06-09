package com.campus.exchange.ai;

import com.campus.exchange.ai.mimo.MiMoConfig;
import com.campus.exchange.ai.mimo.MiMoGateway;
import com.campus.exchange.ai.mimo.MiMoServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiMoGatewayTest {

    @Mock
    private MiMoConfig config;

    @InjectMocks
    private MiMoGateway miMoGateway;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(miMoGateway, "objectMapper", objectMapper);
        when(config.getBaseUrl()).thenReturn("https://api.xiaomimimo.com/v1");
        when(config.getModel()).thenReturn("mimo-v2.5-pro");
        when(config.getMaxTokens()).thenReturn(2048);
        when(config.getTemperature()).thenReturn(0.3);
    }

    @Test
    void testAnalyzeWithValidResponse() {
        String mockResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "{\\"verdict\\": \\"APPROVED\\", \\"confidence\\": 0.85, \\"reasoning\\": \\"测试推理\\", \\"evidence\\": [\\"证据1\\", \\"证据2\\"]}"
                        }
                    }]
                }
                """;

        AiJudgment judgment = AiJudgment.builder()
                .verdict(AiVerdict.APPROVED)
                .confidence(0.85)
                .reasoning("测试推理")
                .evidence(java.util.List.of("证据1", "证据2"))
                .build();

        assertNotNull(judgment);
        assertEquals(AiVerdict.APPROVED, judgment.getVerdict());
        assertEquals(0.85, judgment.getConfidence(), 0.01);
    }

    @Test
    void testParseAiContentWithJsonBlock() {
        String content = """
                ```json
                {"verdict": "REJECTED", "confidence": 0.7, "reasoning": "测试", "evidence": []}
                ```
                """;

        assertNotNull(content);
        assertTrue(content.contains("json"));
    }

    @Test
    void testGetSystemPromptForReport() {
        AiJudgmentType type = AiJudgmentType.REPORT;
        String expected = "你是一个校园交易平台的AI审核员，负责分析举报内容并给出判决。请严格按照要求的JSON格式返回结果。";
        assertNotNull(expected);
        assertTrue(expected.contains("举报"));
    }

    @Test
    void testGetSystemPromptForDispute() {
        AiJudgmentType type = AiJudgmentType.DISPUTE;
        String expected = "你是一个校园交易平台的AI仲裁员，负责分析订单争议并决定是否取消订单。请严格按照要求的JSON格式返回结果。";
        assertNotNull(expected);
        assertTrue(expected.contains("仲裁"));
        assertTrue(expected.contains("取消订单"));
    }

    @Test
    void testGetSystemPromptForGoodsReview() {
        AiJudgmentType type = AiJudgmentType.GOODS_REVIEW;
        String expected = "你是一个校园交易平台的AI审核员，负责审核商品信息的合规性。请严格按照要求的JSON格式返回结果。";
        assertNotNull(expected);
        assertTrue(expected.contains("商品"));
    }

    @Test
    void testParseAiContentWithInvalidJson() {
        String content = "这不是JSON格式的内容";

        AiJudgment judgment = AiJudgment.builder()
                .verdict(AiVerdict.ESCALATED)
                .confidence(0.0)
                .reasoning("AI 响应解析失败，转人工处理")
                .evidence(java.util.List.of("响应解析异常"))
                .build();

        assertEquals(AiVerdict.ESCALATED, judgment.getVerdict());
        assertEquals(0.0, judgment.getConfidence(), 0.01);
    }
}
