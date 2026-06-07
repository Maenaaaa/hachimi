package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.entity.Follow;
import com.campus.exchange.entity.User;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.FollowMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.FollowService;
import com.campus.exchange.vo.UserPublicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @Override
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) throw new BusinessException("不能关注自己");
        if (followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId).eq(Follow::getFolloweeId, followeeId)) > 0) {
            throw new BusinessException("已关注该用户");
        }
        Follow f = new Follow();
        f.setFollowerId(followerId);
        f.setFolloweeId(followeeId);
        followMapper.insert(f);
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        Follow f = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId).eq(Follow::getFolloweeId, followeeId));
        if (f == null) throw new BusinessException("未关注该用户");
        followMapper.deleteById(f.getId());
    }

    @Override
    public List<UserPublicVO> getFollowers(Long userId, int page, int size) {
        Page<Follow> p = new Page<>(page, size);
        List<Follow> follows = followMapper.selectPage(p,
                new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, userId)).getRecords();
        return follows.stream().map(f -> toPublicVO(f.getFollowerId())).toList();
    }

    @Override
    public List<UserPublicVO> getFollowing(Long userId, int page, int size) {
        Page<Follow> p = new Page<>(page, size);
        List<Follow> follows = followMapper.selectPage(p,
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId)).getRecords();
        return follows.stream().map(f -> toPublicVO(f.getFolloweeId())).toList();
    }

    @Override
    public boolean isFollowed(Long followerId, Long followeeId) {
        return followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId).eq(Follow::getFolloweeId, followeeId)) > 0;
    }

    private UserPublicVO toPublicVO(Long userId) {
        User u = userMapper.selectById(userId);
        UserPublicVO vo = new UserPublicVO();
        if (u != null) {
            vo.setId(u.getId());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            vo.setSchool(u.getSchool());
            vo.setCreditScore(u.getCreditScore());
        }
        return vo;
    }
}
