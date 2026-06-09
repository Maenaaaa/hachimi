package com.campus.exchange.service;

import com.campus.exchange.ai.AiJudgment;
import com.campus.exchange.ai.AiJudgmentType;
import com.campus.exchange.entity.AiJudgmentRecord;

import java.util.List;
import java.util.Map;

public interface AiJudgeService {

    AiJudgment judge(AiJudgmentType type, Long sourceId, String prompt);

    AiJudgment judgeWithImages(AiJudgmentType type, Long sourceId, String prompt, List<String> imageUrls);

    void judgeAsync(AiJudgmentType type, Long sourceId, String prompt);

    void judgeAsyncWithImages(AiJudgmentType type, Long sourceId, String prompt, List<String> imageUrls);

    void executeVerdict(Long judgmentId);

    void appeal(Long judgmentId, Long userId, String reason);

    void handleAppeal(Long judgmentId, Long handlerId, boolean override, String note);

    AiJudgmentRecord getBySource(String sourceType, Long sourceId);

    AiJudgmentRecord getReportJudgmentByGoodsId(Long goodsId);

    Map<String, String> getConfig();

    void updateConfig(String key, String value);
}
