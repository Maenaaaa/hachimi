package com.campus.exchange.service;

import com.campus.exchange.dto.CreateReviewDTO;
import com.campus.exchange.vo.ReviewVO;
import java.util.List;

public interface ReviewService {
    ReviewVO create(Long reviewerId, CreateReviewDTO dto);
    List<ReviewVO> getUserReviews(Long userId, int page, int size);
    List<ReviewVO> getMyReceivedReviews(Long userId, int page, int size);
}
