package com.campus.exchange.service;

public interface CreditScoreService {
    void addScoreOnTradeComplete(Long userId);
    void deductScoreOnReportApproved(Long userId);
    void adjustScore(Long userId, double newScore, String reason);
}
