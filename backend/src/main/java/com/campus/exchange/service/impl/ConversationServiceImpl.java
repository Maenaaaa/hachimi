package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.ConversationService;
import com.campus.exchange.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    @Override
    public ConversationVO getOrCreate(Long goodsId, Long buyerId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        if (goods.getUserId().equals(buyerId)) throw new BusinessException("不能与自己聊天");

        Conversation conv = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getGoodsId, goodsId)
                .eq(Conversation::getBuyerId, buyerId)
                .eq(Conversation::getDeleted, 0));

        if (conv == null) {
            conv = new Conversation();
            conv.setGoodsId(goodsId);
            conv.setBuyerId(buyerId);
            conv.setSellerId(goods.getUserId());
            conversationMapper.insert(conv);
        }

        return toVO(conv, buyerId);
    }

    @Override
    public List<ConversationVO> getList(Long userId) {
        List<Conversation> convs = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .and(w -> w.eq(Conversation::getBuyerId, userId).or().eq(Conversation::getSellerId, userId))
                        .eq(Conversation::getDeleted, 0)
                        .orderByDesc(Conversation::getLastMessageTime));

        List<ConversationVO> vos = new ArrayList<>();
        for (Conversation conv : convs) {
            vos.add(toVO(conv, userId));
        }
        return vos;
    }

    private ConversationVO toVO(Conversation conv, Long currentUserId) {
        Long otherId = conv.getBuyerId().equals(currentUserId) ? conv.getSellerId() : conv.getBuyerId();
        User other = userMapper.selectById(otherId);
        Goods goods = goodsMapper.selectById(conv.getGoodsId());

        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setGoodsId(conv.getGoodsId());
        vo.setGoodsTitle(goods != null ? goods.getTitle() : "");
        vo.setOtherUserId(other != null ? other.getId() : null);
        vo.setOtherUserNickname(other != null ? other.getNickname() : "");
        vo.setOtherUserAvatar(other != null ? other.getAvatar() : "");

        GoodsImage firstImg = goodsImageMapper.selectOne(
                new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, conv.getGoodsId())
                        .orderByAsc(GoodsImage::getSortOrder).last("LIMIT 1"));
        vo.setGoodsCoverImage(firstImg != null ? firstImg.getImageUrl() : "");

        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setUnreadCount(messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conv.getId())
                        .eq(Message::getReceiverId, currentUserId)
                        .eq(Message::getIsRead, 0)).intValue());

        return vo;
    }
}
