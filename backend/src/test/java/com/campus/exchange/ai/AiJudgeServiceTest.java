package com.campus.exchange.ai;

import com.campus.exchange.ai.mimo.MiMoServiceException;
import com.campus.exchange.entity.AiConfig;
import com.campus.exchange.entity.AiJudgmentRecord;
import com.campus.exchange.mapper.AiConfigMapper;
import com.campus.exchange.mapper.AiJudgmentMapper;
import com.campus.exchange.service.impl.AiJudgeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiJudgeServiceTest {

    @Mock
    private AiGateway aiGateway;

    @Mock
    private AiJudgmentMapper judgmentMapper;

    @Mock
    private AiConfigMapper configMapper;

    @InjectMocks
    private AiJudgeServiceImpl aiJudgeService;

    @BeforeEach
    void setUp() {
        AiConfig autoThresholdConfig = new AiConfig();
        autoThresholdConfig.setConfigValue("0.8");
        when(configMapper.selectOne(any())).thenReturn(autoThresholdConfig);
    }

    @Test
    void testJudgeWithHighConfidence() {
        AiJudgment mockJudgment = AiJudgment.builder()
                .verdict(AiVerdict.APPROVED)
                .confidence(0.9)
                .reasoning("高置信度判决")
                .evidence(java.util.List.of("证据1"))
                .build();

        when(aiGateway.analyze(any(AiJudgmentType.class), anyString())).thenReturn(mockJudgment);
        when(judgmentMapper.insert(any(AiJudgmentRecord.class))).thenReturn(1);

        AiJudgment result = aiJudgeService.judge(AiJudgmentType.REPORT, 1L, "测试prompt");

        assertEquals(AiVerdict.APPROVED, result.getVerdict());
        assertEquals(0.9, result.getConfidence(), 0.01);
    }

    @Test
    void testJudgeWithLowConfidence() {
        AiJudgment mockJudgment = AiJudgment.builder()
                .verdict(AiVerdict.APPROVED)
                .confidence(0.3)
                .reasoning("低置信度判决")
                .evidence(java.util.List.of("证据1"))
                .build();

        when(aiGateway.analyze(any(AiJudgmentType.class), anyString())).thenReturn(mockJudgment);
        when(judgmentMapper.insert(any(AiJudgmentRecord.class))).thenReturn(1);

        AiJudgment result = aiJudgeService.judge(AiJudgmentType.REPORT, 1L, "测试prompt");

        assertEquals(AiVerdict.ESCALATED, result.getVerdict());
    }

    @Test
    void testJudgeWithAiServiceFailure() {
        when(aiGateway.analyze(any(AiJudgmentType.class), anyString()))
                .thenThrow(new MiMoServiceException("AI 服务不可用"));
        when(judgmentMapper.insert(any(AiJudgmentRecord.class))).thenReturn(1);

        AiJudgment result = aiJudgeService.judge(AiJudgmentType.REPORT, 1L, "测试prompt");

        assertEquals(AiVerdict.ESCALATED, result.getVerdict());
        assertEquals(0.0, result.getConfidence(), 0.01);
    }

    @Test
    void testAppealSuccess() {
        AiJudgmentRecord record = new AiJudgmentRecord();
        record.setId(1L);
        record.setStatus("PROCESSED");

        when(judgmentMapper.selectById(1L)).thenReturn(record);
        when(judgmentMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> aiJudgeService.appeal(1L, 100L, "判决有误"));
    }

    @Test
    void testAppealOnPendingStatus() {
        AiJudgmentRecord record = new AiJudgmentRecord();
        record.setId(1L);
        record.setStatus("PENDING");

        when(judgmentMapper.selectById(1L)).thenReturn(record);

        assertThrows(Exception.class, () -> aiJudgeService.appeal(1L, 100L, "判决有误"));
    }

    @Test
    void testGetConfig() {
        AiConfig config1 = new AiConfig();
        config1.setConfigKey("report_auto_review");
        config1.setConfigValue("true");

        when(configMapper.selectList(any())).thenReturn(java.util.List.of(config1));

        var result = aiJudgeService.getConfig();

        assertEquals("true", result.get("report_auto_review"));
    }
}
