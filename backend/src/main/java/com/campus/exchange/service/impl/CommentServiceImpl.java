package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.dto.CreateCommentDTO;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.entity.GoodsComment;
import com.campus.exchange.entity.User;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.GoodsCommentMapper;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.CommentService;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final GoodsCommentMapper commentMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentVO create(Long userId, CreateCommentDTO dto) {
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) throw new BusinessException("商品不存在");

        GoodsComment comment = new GoodsComment();
        comment.setGoodsId(dto.getGoodsId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        commentMapper.insert(comment);

        User commenter = userMapper.selectById(userId);

        // Notify: if reply, notify parent commenter; if new comment, notify seller
        if (dto.getParentId() != null) {
            GoodsComment parent = commentMapper.selectById(dto.getParentId());
            if (parent != null && !parent.getUserId().equals(userId)) {
                notificationService.create(parent.getUserId(), "SYSTEM",
                        "留言被回复", (commenter != null ? commenter.getNickname() : "用户") + "回复了你的留言", goods.getId());
            }
        } else {
            if (!goods.getUserId().equals(userId)) {
                notificationService.create(goods.getUserId(), "SYSTEM",
                        "商品有新留言", (commenter != null ? commenter.getNickname() : "用户") + "在你的商品下留言了", goods.getId());
            }
        }

        return toVO(comment);
    }

    @Override
    public List<CommentVO> getByGoodsId(Long goodsId) {
        List<GoodsComment> all = commentMapper.selectList(
                new LambdaQueryWrapper<GoodsComment>().eq(GoodsComment::getGoodsId, goodsId).orderByAsc(GoodsComment::getCreateTime));

        Map<Long, User> userMap = all.stream()
                .map(GoodsComment::getUserId)
                .distinct()
                .map(userMapper::selectById)
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CommentVO> vos = all.stream().map(c -> toVO(c, userMap)).collect(Collectors.toList());

        // Build tree: top-level comments with replies nested
        List<CommentVO> roots = new ArrayList<>();
        for (CommentVO vo : vos) {
            if (vo.getParentId() == null) {
                roots.add(vo);
            } else {
                for (CommentVO root : roots) {
                    if (root.getId().equals(vo.getParentId())) {
                        if (root.getReplies() == null) root.setReplies(new ArrayList<>());
                        root.getReplies().add(vo);
                        break;
                    }
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        GoodsComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("留言不存在");

        Goods goods = goodsMapper.selectById(comment.getGoodsId());
        if (goods == null) throw new BusinessException("商品不存在");

        boolean isCommentOwner = comment.getUserId().equals(userId);
        boolean isGoodsOwner = goods.getUserId().equals(userId);
        if (!isCommentOwner && !isGoodsOwner) {
            throw new BusinessException("无权删除该留言");
        }

        commentMapper.deleteById(commentId);

        List<GoodsComment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<GoodsComment>().eq(GoodsComment::getParentId, commentId));
        for (GoodsComment reply : replies) {
            commentMapper.deleteById(reply.getId());
        }
    }

    private CommentVO toVO(GoodsComment c) {
        return toVO(c, null);
    }

    private CommentVO toVO(GoodsComment c, Map<Long, User> userMap) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setGoodsId(c.getGoodsId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setCreateTime(c.getCreateTime());
        if (userMap != null) {
            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setUserNickname(u.getNickname());
                vo.setUserAvatar(u.getAvatar());
            }
        }
        return vo;
    }
}
