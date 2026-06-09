package com.campus.exchange.ai;

import java.util.List;

public interface AiGateway {

    AiJudgment analyze(AiJudgmentType type, String prompt);

    AiJudgment analyzeWithImages(AiJudgmentType type, String prompt, List<String> imageUrls);
}
