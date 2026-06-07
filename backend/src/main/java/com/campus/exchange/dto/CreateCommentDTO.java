package com.campus.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentDTO {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    private Long parentId;
    @NotBlank(message = "留言内容不能为空")
    private String content;
}
