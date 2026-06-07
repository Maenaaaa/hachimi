package com.campus.exchange.service;

import com.campus.exchange.vo.UserPublicVO;
import java.util.List;

public interface FollowService {
    void follow(Long followerId, Long followeeId);
    void unfollow(Long followerId, Long followeeId);
    List<UserPublicVO> getFollowers(Long userId, int page, int size);
    List<UserPublicVO> getFollowing(Long userId, int page, int size);
    boolean isFollowed(Long followerId, Long followeeId);
}
