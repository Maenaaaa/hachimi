package com.campus.exchange.service;

import com.campus.exchange.dto.CreateCommentDTO;
import com.campus.exchange.vo.CommentVO;
import java.util.List;

public interface CommentService {
    CommentVO create(Long userId, CreateCommentDTO dto);
    List<CommentVO> getByGoodsId(Long goodsId);
}
