package com.campus.exchange.service;

import com.campus.exchange.common.PageResult;
import com.campus.exchange.vo.DisputeVO;
import java.util.List;

public interface DisputeService {
    DisputeVO create(Long orderId, Long applicantId, String reason, List<Long> selectedChatMessageIds);
    DisputeVO handle(Long disputeId, Long handlerId, String status, String handleNote);
    PageResult<DisputeVO> list(String status, int page, int size);
    DisputeVO getById(Long disputeId);
}
