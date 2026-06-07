package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.Announcement;
import com.campus.exchange.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> listAll(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.ok(announcementService.listAll(page, size));
    }

    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.ok(announcementService.getById(id));
    }
}
