package com.campus.exchange.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportContext {

    private Long reportId;
    private String type;
    private String reason;
    private String description;
    private String targetInfo;
    private double targetCreditScore;
    private Long reporterId;
    private Long targetId;
}
