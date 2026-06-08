package com.campus.exchange.service.impl;

import com.campus.exchange.entity.User;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.CreditScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CreditScoreServiceImpl implements CreditScoreService {

    private static final BigDecimal SCORE_INCREMENT = new BigDecimal("0.1");
    private static final BigDecimal SCORE_DEDUCT = new BigDecimal("0.5");
    private static final BigDecimal MIN_SCORE = new BigDecimal("1.0");
    private static final BigDecimal MAX_SCORE = new BigDecimal("5.0");
    private static final BigDecimal DEFAULT_SCORE = new BigDecimal("5.0");

    private final UserMapper userMapper;

    @Override
    @Transactional
    public void addScoreOnTradeComplete(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        BigDecimal currentScore = user.getCreditScore() != null ? user.getCreditScore() : DEFAULT_SCORE;
        BigDecimal newScore = currentScore.add(SCORE_INCREMENT).min(MAX_SCORE).setScale(1, RoundingMode.HALF_UP);

        user.setCreditScore(newScore);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deductScoreOnReportApproved(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        BigDecimal currentScore = user.getCreditScore() != null ? user.getCreditScore() : DEFAULT_SCORE;
        BigDecimal newScore = currentScore.subtract(SCORE_DEDUCT).max(MIN_SCORE).setScale(1, RoundingMode.HALF_UP);

        user.setCreditScore(newScore);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void adjustScore(Long userId, double newScore, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        BigDecimal score = BigDecimal.valueOf(newScore);
        if (score.compareTo(MIN_SCORE) < 0 || score.compareTo(MAX_SCORE) > 0) {
            throw new BusinessException("信用分必须在1.0-5.0之间");
        }

        user.setCreditScore(score.setScale(1, RoundingMode.HALF_UP));
        userMapper.updateById(user);
    }
}
