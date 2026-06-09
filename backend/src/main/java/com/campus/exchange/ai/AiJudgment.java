package com.campus.exchange.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiJudgment {

    private AiVerdict verdict;
    private double confidence;
    private String reasoning;
    private List<String> evidence;
}
