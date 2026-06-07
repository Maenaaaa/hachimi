package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Update("UPDATE message SET is_read = 1 WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND is_read = 0")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
