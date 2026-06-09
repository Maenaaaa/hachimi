package com.campus.exchange.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeContext {

    private Long disputeId;
    private Long orderId;
    private String reason;
    private String orderInfo;
    private String goodsInfo;
    private String chatSummary;
    private double applicantCreditScore;
    private double respondentCreditScore;
    private Long applicantId;
    private Long respondentId;
}
