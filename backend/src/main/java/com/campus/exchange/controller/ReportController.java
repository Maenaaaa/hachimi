package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.dto.CreateReportDTO;
import com.campus.exchange.entity.User;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.ReportService;
import com.campus.exchange.vo.ReportVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Result<Void> create(@CurrentUser User user, @Valid @RequestBody CreateReportDTO dto) {
        reportService.create(user.getId(), dto);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<ReportVO>> getMyReports(@CurrentUser User user,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reportService.getMyReports(user.getId(), page, size));
    }
}
