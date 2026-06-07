package com.campus.exchange.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long goodsId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long parentId;
    private String content;
    private LocalDateTime createTime;
    private List<CommentVO> replies;
}
