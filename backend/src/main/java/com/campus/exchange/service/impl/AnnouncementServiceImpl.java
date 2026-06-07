package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.CreateAnnouncementDTO;
import com.campus.exchange.dto.UpdateAnnouncementDTO;
import com.campus.exchange.entity.Announcement;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.AnnouncementMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.AnnouncementService;
import com.campus.exchange.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Announcement create(Long publisherId, CreateAnnouncementDTO dto) {
        Announcement a = new Announcement();
        a.setTitle(dto.getTitle());
        a.setContent(dto.getContent());
        a.setPublisherId(publisherId);
        announcementMapper.insert(a);

        List<Long> userIds = userMapper.selectList(null).stream()
                .filter(u -> "USER".equals(u.getRole()))
                .map(u -> u.getId()).toList();
        for (Long uid : userIds) {
            notificationService.create(uid, "SYSTEM", dto.getTitle(), dto.getContent(), a.getId());
        }

        return a;
    }

    @Override
    public Announcement update(Long id, UpdateAnnouncementDTO dto) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        if (dto.getTitle() != null) a.setTitle(dto.getTitle());
        if (dto.getContent() != null) a.setContent(dto.getContent());
        announcementMapper.updateById(a);
        return a;
    }

    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public List<Announcement> listAll(int page, int size) {
        Page<Announcement> p = new Page<>(page, size);
        return announcementMapper.selectPage(p,
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getDeleted, 0)
                        .orderByDesc(Announcement::getCreateTime)).getRecords();
    }

    @Override
    public Announcement getById(Long id) {
        return announcementMapper.selectById(id);
    }
}
