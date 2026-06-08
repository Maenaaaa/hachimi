package com.campus.exchange.service;

import com.campus.exchange.vo.DisputeVO;
import java.util.List;

public interface DisputeService {
    DisputeVO create(Long orderId, Long applicantId, String reason);
    DisputeVO handle(Long disputeId, Long handlerId, String status, String handleNote);
    List<DisputeVO> list(String status, int page, int size);
    DisputeVO getById(Long disputeId);
}
