package com.campus.exchange.service;

import com.campus.exchange.vo.ConversationVO;
import java.util.List;

public interface ConversationService {
    ConversationVO getOrCreate(Long goodsId, Long buyerId);
    List<ConversationVO> getList(Long userId);
}
