package com.campus.exchange.service;

import com.campus.exchange.dto.CreateAnnouncementDTO;
import com.campus.exchange.dto.UpdateAnnouncementDTO;
import com.campus.exchange.entity.Announcement;
import java.util.List;

public interface AnnouncementService {
    Announcement create(Long publisherId, CreateAnnouncementDTO dto);
    Announcement update(Long id, UpdateAnnouncementDTO dto);
    void delete(Long id);
    List<Announcement> listAll(int page, int size);
    Announcement getById(Long id);
}
