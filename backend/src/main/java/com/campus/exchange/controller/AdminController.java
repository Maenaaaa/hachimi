package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.CreateAnnouncementDTO;
import com.campus.exchange.dto.HandleReportDTO;
import com.campus.exchange.dto.UpdateAnnouncementDTO;
import com.campus.exchange.entity.Announcement;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.AdminService;
import com.campus.exchange.service.AnnouncementService;
import com.campus.exchange.service.ReportService;
import com.campus.exchange.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final AnnouncementService announcementService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.ok(adminService.getDashboard());
    }

    @GetMapping("/users")
    public Result<List<AdminUserVO>> listUsers(@RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return Result.ok(adminService.listUsers(keyword, page, size));
    }

    @GetMapping("/user/{id}")
    public Result<AdminUserVO> getUserDetail(@PathVariable Long id) {
        return Result.ok(adminService.getUserDetail(id));
    }

    @PutMapping("/user/{id}/disable")
    public Result<Void> disableUser(@PathVariable Long id) {
        adminService.disableUser(id);
        return Result.ok();
    }

    @PutMapping("/user/{id}/enable")
    public Result<Void> enableUser(@PathVariable Long id) {
        adminService.enableUser(id);
        return Result.ok();
    }

    @GetMapping("/goods")
    public Result<List<AdminGoodsVO>> listGoods(@RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return Result.ok(adminService.listGoods(status, keyword, page, size));
    }

    @PutMapping("/goods/{id}/approve")
    public Result<Void> approveGoods(@PathVariable Long id) {
        adminService.approveGoods(id);
        return Result.ok();
    }

    @PutMapping("/goods/{id}/reject")
    public Result<Void> rejectGoods(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.rejectGoods(id, body.get("reason"));
        return Result.ok();
    }

    @PutMapping("/goods/{id}/take-down")
    public Result<Void> takeDownGoods(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.takeDownGoods(id, body.get("reason"));
        return Result.ok();
    }

    @DeleteMapping("/goods/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        adminService.deleteGoods(id);
        return Result.ok();
    }

    @GetMapping("/report")
    public Result<List<ReportVO>> getReports(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reportService.getPendingReports(page, size));
    }

    @PutMapping("/report/{id}/handle")
    public Result<Void> handleReport(@CurrentUser User user, @PathVariable Long id,
                                      @Valid @RequestBody HandleReportDTO dto) {
        reportService.handle(id, user.getId(), dto);
        return Result.ok();
    }

    @GetMapping("/verifications")
    public Result<List<AdminUserVO>> listVerifications(@RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.ok(adminService.listVerifications(status, page, size));
    }

    @PutMapping("/verification/{id}/approve")
    public Result<Void> approveVerification(@PathVariable Long id) {
        adminService.approveVerification(id);
        return Result.ok();
    }

    @PutMapping("/verification/{id}/reject")
    public Result<Void> rejectVerification(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.rejectVerification(id, body.get("reason"));
        return Result.ok();
    }

    @PostMapping("/announcement")
    public Result<Announcement> createAnnouncement(@CurrentUser User user,
                                                    @Valid @RequestBody CreateAnnouncementDTO dto) {
        return Result.ok(announcementService.create(user.getId(), dto));
    }

    @PutMapping("/announcement/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody UpdateAnnouncementDTO dto) {
        return Result.ok(announcementService.update(id, dto));
    }

    @DeleteMapping("/announcement/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.ok();
    }
}
